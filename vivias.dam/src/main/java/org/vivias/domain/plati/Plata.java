package org.vivias.domain.plati;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.Suma.Moneda;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
 * Aggregate Root (APOO, Figura 6.c, Figura 17.d): plata prin banca.
 * PROPUSA -> APROBATA -> TRIMISA_LA_BANCA -> EXECUTATA | RESPINSA; PROPUSA / APROBATA -> ANULATA
 * O plata nu se sterge, se anuleaza, ca sa ramana urma pentru audit.
 */
@Entity
public class Plata implements Serializable {

	@Id
	@GeneratedValue
	private Long idPlata;

	private Long idFactura; // [0..1]: platile fara factura (chirie, salarii, taxe) nu au

	@NotNull(message = "Beneficiarul este obligatoriu!")
	private String beneficiar;

	@NotNull(message = "IBAN-ul beneficiarului este obligatoriu!")
	@Embedded
	@AttributeOverride(name = "valoare", column = @Column(name = "iban"))
	private IBAN iban;

	@NotNull(message = "Suma platii este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@NotNull(message = "Data platii este obligatorie!")
	private LocalDate dataPlatii;

	@NotNull
	@Enumerated(EnumType.STRING)
	private StarePlata stare = StarePlata.PROPUSA;

	private String motiv; // [0..1]: la respingere sau anulare

	private Long idPrognozaSursa; // [0..1]: prognoza din care a venit recomandarea

	private UUID eventSursaId; // [0..1]: RecomandariPlataEvent-ul care a creat plata

	private Long aprobataDe; // [0..1]: administratorul care a aprobat

	// necesar doar pentru JPA
	protected Plata() {
		super();
	}

	private Plata(Long idFactura, String beneficiar, IBAN iban, Suma suma, LocalDate dataPlatii,
			Long idPrognozaSursa, UUID eventSursaId) {
		this.idFactura = idFactura;
		this.beneficiar = beneficiar;
		this.iban = iban;
		this.suma = suma;
		this.dataPlatii = dataPlatii;
		this.idPrognozaSursa = idPrognozaSursa;
		this.eventSursaId = eventSursaId;
	}

	// Factory (CU7 PropunePlata)
	public static Plata propune(Long idFactura, String beneficiar, IBAN iban, Suma suma, LocalDate dataPlatii,
			Long idPrognozaSursa, UUID eventSursaId) {
		if (suma == null || suma.semn() <= 0)
			throw new IllegalArgumentException("Suma platii trebuie sa fie pozitiva.");
		return new Plata(idFactura, beneficiar, iban, suma, dataPlatii, idPrognozaSursa, eventSursaId);
	}

	public void aproba(Long idAdministrator) {
		verificaStare("aproba", StarePlata.PROPUSA);
		if (idAdministrator == null)
			throw new IllegalArgumentException("Plata trebuie aprobata de un administrator.");
		if (suma.getMoneda() != Moneda.RON)
			throw new IllegalStateException("Plata in " + suma.getMoneda() + " trebuie intai recalculata in lei.");
		this.aprobataDe = idAdministrator;
		this.stare = StarePlata.APROBATA;
	}

	public void trimiteLaBanca() {
		verificaStare("trimite la banca", StarePlata.APROBATA);
		this.stare = StarePlata.TRIMISA_LA_BANCA;
	}

	public PlataExecutataEvent executa() {
		verificaStare("executa", StarePlata.TRIMISA_LA_BANCA);
		this.stare = StarePlata.EXECUTATA;
		return new PlataExecutataEvent(idPlata, idFactura, suma, dataPlatii);
	}

	public void respinge(String motivRespingere) {
		verificaStare("respinge", StarePlata.TRIMISA_LA_BANCA);
		this.motiv = motivRespingere;
		this.stare = StarePlata.RESPINSA;
	}

	public void anuleaza(String motivAnulare) {
		verificaStare("anula", StarePlata.PROPUSA, StarePlata.APROBATA);
		this.motiv = motivAnulare;
		this.stare = StarePlata.ANULATA;
	}

	// chiria in euro se transforma in lei cu cursul BNR al zilei, inainte de aprobare
	public void recalculeazaInLei(CursBNR curs) {
		verificaStare("recalcula", StarePlata.PROPUSA);
		this.suma = curs.converteste(suma);
	}

	private void verificaStare(String actiune, StarePlata... permise) {
		for (StarePlata permisa : permise)
			if (stare == permisa)
				return;
		throw new IllegalStateException("Nu se poate " + actiune + " plata " + idPlata + ": starea este " + stare + ".");
	}

	public Long getIdPlata() {
		return idPlata;
	}

	public Long getIdFactura() {
		return idFactura;
	}

	public String getBeneficiar() {
		return beneficiar;
	}

	public IBAN getIban() {
		return iban;
	}

	public Suma getSuma() {
		return suma;
	}

	public LocalDate getDataPlatii() {
		return dataPlatii;
	}

	public StarePlata getStare() {
		return stare;
	}

	public String getMotiv() {
		return motiv;
	}

	public Long getIdPrognozaSursa() {
		return idPrognozaSursa;
	}

	public UUID getEventSursaId() {
		return eventSursaId;
	}

	public Long getAprobataDe() {
		return aprobataDe;
	}

	@Override
	public String toString() {
		return "Plata [idPlata=" + idPlata + ", idFactura=" + idFactura + ", beneficiar=" + beneficiar + ", suma="
				+ suma + ", stare=" + stare + "]";
	}
}
