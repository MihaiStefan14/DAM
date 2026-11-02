package org.vivias.domain.casierie;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Aggregate Root: Registrul de casa zilnic (APOO: Figura 6.b, CU5 InchideZiuaDeCasa;
 * DAM Sprint 1: User Story 5 - "Inchiderea zilei in registrul de casa").
 *
 * Components: OperatiuneCasa (0..n). Value Objects: Suma, RaportZ, StareRegistru.
 *
 * Radacina pazeste regulile registrului:
 * - operatiunile se adauga doar prin radacina, doar cat timp registrul e DESCHIS
 *   si doar in moneda registrului; incasarile in numerar vin din raportul Z;
 * - soldul scriptic nu se salveaza: se calculeaza mereu din operatiunile zilei;
 * - ziua se inchide doar cu raportul Z atasat si cu diferenta justificata si aprobata:
 *   DESCHIS -> IN_VERIFICARE -> INCHIS
 *                            -> CU_DIFERENTE -> REGULARIZAT -> INCHIS
 */
@Entity
public class RegistruCasa implements Serializable, Comparable<RegistruCasa> {

	@Min(1)
	@NotNull(message = "RegistruId este obligatoriu!")
	@Id
	@GeneratedValue
	private Long idRegistru;

	// identitatea de business: un singur registru pe zi (verificat la deschidere, in workflow)
	@NotNull(message = "Data registrului este obligatorie!")
	@Column(unique = true)
	private LocalDate data;

	@NotNull(message = "Soldul de deschidere este obligatoriu!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_deschidere_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_deschidere_moneda")) })
	private Suma soldDeschidere;

	// @Valid: regulile JSR-380 ale componentelor fac parte din validarea agregatului
	@Valid
	@OneToMany(mappedBy = "registru", cascade = ALL, fetch = EAGER, orphanRemoval = true)
	private List<OperatiuneCasa> operatii = new ArrayList<>();

	@Valid
	@Embedded
	private RaportZ raportZ; // [0..1]: importat seara, de la casa de marcat

	// suma numarata de vanzator la inchiderea zilei
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_faptic_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_faptic_moneda")) })
	private Suma soldFaptic; // [0..1]

	private String notaExplicativa; // [0..1]: obligatorie daca exista diferenta

	private Long aprobatDe; // [0..1]: administratorul care a aprobat regularizarea

	@NotNull(message = "Starea registrului este obligatorie!")
	@Enumerated(EnumType.STRING)
	private StareRegistru stare = StareRegistru.DESCHIS;

	/*
	 * Computed fields: soldScriptic, diferenta*
	 * - transient, encapsulate-only-getters
	 * - computing rule: CalculatingRegistruCasaDomainService
	 */
	@Transient
	protected Suma soldScriptic;

	@Transient
	protected Suma diferentaValoare;

	@Transient
	protected TipDiferenta diferentaTip;

	public RegistruCasa() {
		super();
	}

	public RegistruCasa(Long idRegistru, LocalDate data, Suma soldDeschidere) {
		super();
		this.idRegistru = idRegistru;
		this.data = data;
		this.soldDeschidere = soldDeschidere;
	}

	/* Operatiunile zilei: adaugate doar prin radacina */

	// Importul raportului Z: vanzarile in numerar devin o operatiune INCASARE_NUMERAR
	public void ataseazaRaportZ(RaportZ raport) {
		verificaStare(StareRegistru.DESCHIS, "atasa raportul Z");
		if (raport == null)
			throw new IllegalArgumentException("Raportul Z lipseste!");
		if (this.raportZ != null)
			throw new IllegalStateException("Raportul Z al zilei " + data + " a fost deja importat.");
		verificaMonedaRegistrului(raport.getVanzariCard(), "Vanzarile pe card");
		verificaMonedaRegistrului(raport.getVanzariNumerar(), "Vanzarile in numerar");

		this.raportZ = raport;
		if (raport.getVanzariNumerar().semn() > 0)
			adauga(TipOperatiune.INCASARE_NUMERAR, raport.getVanzariNumerar(), "Raport Z nr. " + raport.getNumar(),
					null, null);
	}

	public OperatiuneCasa inregistreazaOperatiune(TipOperatiune tip, Suma suma, String document) {
		return inregistreazaOperatiune(tip, suma, document, null, null);
	}

	// idFactura doar pentru PLATA_FURNIZOR, idAvans doar pentru AVANS / RESTITUIRE (referinte prin id)
	public OperatiuneCasa inregistreazaOperatiune(TipOperatiune tip, Suma suma, String document, Long idFactura,
			Long idAvans) {
		verificaStare(StareRegistru.DESCHIS, "inregistra o operatiune");
		if (tip == null)
			throw new IllegalArgumentException("Tipul operatiunii este obligatoriu!");
		if (tip == TipOperatiune.INCASARE_NUMERAR)
			throw new IllegalArgumentException("Incasarile in numerar se preiau din raportul Z.");
		verificaMonedaRegistrului(suma, "Operatiunea");
		if (suma.semn() <= 0)
			throw new IllegalArgumentException("Suma operatiunii trebuie sa fie pozitiva.");
		return adauga(tip, suma, document, idFactura, idAvans);
	}

	private OperatiuneCasa adauga(TipOperatiune tip, Suma suma, String document, Long idFactura, Long idAvans) {
		OperatiuneCasa operatiune = new OperatiuneCasa(operatii.size() + 1, tip, suma, document, idFactura, idAvans,
				this);
		operatii.add(operatiune);
		return operatiune;
	}

	// sold scriptic = sold de deschidere +/- operatiunile zilei (nu se salveaza)
	public Suma calculeazaSoldScriptic() {
		Suma sold = soldDeschidere;
		for (OperatiuneCasa operatiune : operatii)
			sold = operatiune.getTip().aplica(sold, operatiune.getSuma());
		return sold;
	}

	/* Inchiderea zilei (CU5 InchideZiuaDeCasa) */

	// pasii 2-3: fara raport Z inchiderea e refuzata si registrul ramane DESCHIS
	public void incepeInchiderea() {
		verificaStare(StareRegistru.DESCHIS, "inchide ziua");
		if (raportZ == null)
			throw new IllegalStateException("Lipsește raportul Z");
		this.stare = StareRegistru.IN_VERIFICARE;
	}

	// pasii 4-5: vanzatorul introduce suma numarata; o diferenta trece registrul in CU_DIFERENTE
	public void introduceSoldFaptic(Suma soldNumarat) {
		verificaStare(StareRegistru.IN_VERIFICARE, "introduce soldul faptic");
		verificaMonedaRegistrului(soldNumarat, "Soldul faptic");
		this.soldFaptic = soldNumarat;
		if (!soldNumarat.equals(calculeazaSoldScriptic()))
			this.stare = StareRegistru.CU_DIFERENTE;
	}

	// alternativa 1: vanzatorul scrie nota explicativa
	public void justificaDiferenta(String nota) {
		verificaStare(StareRegistru.CU_DIFERENTE, "justifica diferenta");
		if (nota == null || nota.isBlank())
			throw new IllegalArgumentException("Nota explicativa nu poate fi goala.");
		this.notaExplicativa = nota;
	}

	// alternativa 1: administratorul aproba regularizarea -> REGULARIZAT
	public void aprobaRegularizare(Long idAdministrator) {
		verificaStare(StareRegistru.CU_DIFERENTE, "aproba regularizarea");
		if (notaExplicativa == null)
			throw new IllegalStateException(mesajDiferentaNejustificata());
		if (idAdministrator == null)
			throw new IllegalArgumentException("Regularizarea trebuie aprobata de un administrator.");
		this.aprobatDe = idAdministrator;
		this.stare = StareRegistru.REGULARIZAT;
	}

	// pasul 6: inchiderea, intr-o singura operatie, cu evenimentul catre Prognoza
	public ZiCasaInchisaEvent inchide(PlafoaneNumerar plafoane) {
		if (stare == StareRegistru.CU_DIFERENTE)
			throw new IllegalStateException(notaExplicativa == null ? mesajDiferentaNejustificata()
					: "Diferenta trebuie aprobata de administrator inainte de inchidere.");
		verificaStare(new StareRegistru[] { StareRegistru.IN_VERIFICARE, StareRegistru.REGULARIZAT }, "inchide ziua");
		if (soldFaptic == null)
			throw new IllegalStateException("Soldul faptic nu a fost introdus.");

		this.stare = StareRegistru.INCHIS;
		return new ZiCasaInchisaEvent(idRegistru, data, raportZ.getVanzariCard(), raportZ.getVanzariNumerar(),
				soldFaptic, plafoane.sumaDeDepus(soldFaptic));
	}

	private String mesajDiferentaNejustificata() {
		Suma diferenta = soldFaptic.scade(calculeazaSoldScriptic());
		return "Diferența de " + diferenta.formatata() + " trebuie justificată";
	}

	private void verificaStare(StareRegistru stareAsteptata, String actiune) {
		verificaStare(new StareRegistru[] { stareAsteptata }, actiune);
	}

	private void verificaStare(StareRegistru[] permise, String actiune) {
		for (StareRegistru permisa : permise)
			if (this.stare == permisa)
				return;
		throw new IllegalStateException("Nu se poate " + actiune + " pentru registrul din " + data
				+ ": starea este " + stare + ".");
	}

	private void verificaMonedaRegistrului(Suma suma, String ceAnume) {
		if (suma == null)
			throw new IllegalArgumentException(ceAnume + " trebuie sa aiba o suma!");
		if (soldDeschidere != null && !soldDeschidere.areAceeasiMoneda(suma))
			throw new IllegalArgumentException(ceAnume + " este in " + suma.getMoneda() + ", dar registrul din "
					+ data + " este tinut in " + soldDeschidere.getMoneda() + ".");
	}

	/* JSR-380: reguli pe starea agregatului */

	@AssertTrue(message = "Un registru care a trecut de DESCHIS trebuie sa aiba raportul Z atasat!")
	public boolean isRaportZValid() {
		return stare == StareRegistru.DESCHIS || raportZ != null;
	}

	@AssertTrue(message = "Soldul faptic trebuie completat pentru un registru cu diferente/regularizat/inchis!")
	public boolean isSoldFapticValid() {
		return stare == StareRegistru.DESCHIS || stare == StareRegistru.IN_VERIFICARE || soldFaptic != null;
	}

	@AssertTrue(message = "Un registru regularizat trebuie sa aiba nota explicativa si aprobarea administratorului!")
	public boolean isRegularizareValida() {
		return stare != StareRegistru.REGULARIZAT || (notaExplicativa != null && aprobatDe != null);
	}

	/* Getters (+ setteri doar pentru reconstituire de catre Repository/Factory si teste) */

	public Long getIdRegistru() {
		return idRegistru;
	}

	public void setIdRegistru(Long idRegistru) {
		this.idRegistru = idRegistru;
	}

	public LocalDate getData() {
		return data;
	}

	public Suma getSoldDeschidere() {
		return soldDeschidere;
	}

	public void setSoldDeschidere(Suma soldDeschidere) {
		this.soldDeschidere = soldDeschidere;
	}

	public List<OperatiuneCasa> getOperatii() {
		return Collections.unmodifiableList(operatii);
	}

	public RaportZ getRaportZ() {
		return raportZ;
	}

	public Suma getSoldFaptic() {
		return soldFaptic;
	}

	public void setSoldFaptic(Suma soldFaptic) {
		this.soldFaptic = soldFaptic;
	}

	public String getNotaExplicativa() {
		return notaExplicativa;
	}

	public Long getAprobatDe() {
		return aprobatDe;
	}

	public StareRegistru getStare() {
		return stare;
	}

	public void setStare(StareRegistru stare) {
		this.stare = stare;
	}

	public Suma getSoldScriptic() {
		return soldScriptic;
	}

	public void setSoldScriptic(Suma soldScriptic) {
		this.soldScriptic = soldScriptic;
	}

	public Suma getDiferentaValoare() {
		return diferentaValoare;
	}

	public void setDiferentaValoare(Suma diferentaValoare) {
		this.diferentaValoare = diferentaValoare;
	}

	public TipDiferenta getDiferentaTip() {
		return diferentaTip;
	}

	public void setDiferentaTip(TipDiferenta diferentaTip) {
		this.diferentaTip = diferentaTip;
	}

	@Override
	public int compareTo(RegistruCasa alt) {
		return this.data.compareTo(alt.getData());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idRegistru == null) ? 0 : idRegistru.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		RegistruCasa other = (RegistruCasa) obj;
		if (idRegistru == null)
			return other.idRegistru == null;
		return idRegistru.equals(other.idRegistru);
	}

	@Override
	public String toString() {
		return "\nRegistruCasa [idRegistru=" + idRegistru + ", data=" + data + ", stare=" + stare
				+ ", soldDeschidere=" + soldDeschidere + ", raportZ=" + raportZ + ", operatii=" + operatii + "]";
	}
}
