package org.vivias.domain.prognoza;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.vivias.domain.casierie.EvenimentDomeniu;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.shared.Perioada;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
 * Aggregate Root (APOO, Figura 6.a, Tabelul 2.1): prognoza de trezorerie pe 7, 14 sau 30 de zile.
 * Components: ZiPrognozata (1..*), RecomandarePlata (0..*).
 * CIORNA -> APROBATA -> IN_URMARIRE -> INCHISA; APROBATA / IN_URMARIRE -> INVALIDATA
 */
@Entity
public class Prognoza implements Serializable {

	@Id
	@GeneratedValue
	private Long idPrognoza;

	@NotNull(message = "Perioada prognozei este obligatorie!")
	@Valid
	@Embedded
	private Perioada perioada;

	@NotNull(message = "Metoda de prognoza este obligatorie!")
	@Enumerated(EnumType.STRING)
	private MetodaPrognoza metoda;

	@NotNull(message = "Soldul initial este obligatoriu!")
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_initial_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_initial_moneda")) })
	private Suma soldInitial;

	@NotNull(message = "Soldul minim de siguranta este obligatoriu!")
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_minim_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_minim_moneda")) })
	private Suma soldMinim;

	@NotNull
	@Enumerated(EnumType.STRING)
	private StarePrognoza stare = StarePrognoza.CIORNA;

	// @OrderColumn: doua liste EAGER in acelasi agregat trebuie sa fie indexate
	@OneToMany(mappedBy = "prognoza", cascade = ALL, fetch = EAGER, orphanRemoval = true)
	@OrderColumn(name = "pozitie")
	private List<ZiPrognozata> zile = new ArrayList<>();

	@OneToMany(mappedBy = "prognoza", cascade = ALL, fetch = EAGER, orphanRemoval = true)
	@OrderColumn(name = "pozitie")
	private List<RecomandarePlata> recomandari = new ArrayList<>();

	private BigDecimal mape; // [0..1]: eroarea medie procentuala, calculata la inchidere

	// necesar doar pentru JPA
	protected Prognoza() {
		super();
	}

	private Prognoza(Perioada perioada, MetodaPrognoza metoda, Suma soldInitial, Suma soldMinim) {
		this.perioada = perioada;
		this.metoda = metoda;
		this.soldInitial = soldInitial;
		this.soldMinim = soldMinim;
	}

	// Factory: singurul drum spre o prognoza noua, cu cate o zi pentru fiecare data din perioada
	public static Prognoza creeaza(Perioada perioada, MetodaPrognoza metoda, Suma soldInitial, Suma soldMinim,
			List<EstimareZi> estimari, List<Scadenta> scadente) {
		Prognoza prognoza = new Prognoza(perioada, metoda, soldInitial, soldMinim);
		for (LocalDate data = perioada.getInceput(); !data.isAfter(perioada.getSfarsit()); data = data.plusDays(1))
			prognoza.zile.add(new ZiPrognozata(data, prognoza.platiDin(scadente, data), prognoza));
		prognoza.aplicaEstimari(estimari);
		return prognoza;
	}

	public void schimbaMetoda(MetodaPrognoza metodaNoua, List<EstimareZi> estimari) {
		verificaStare("schimba metoda", StarePrognoza.CIORNA);
		this.metoda = metodaNoua;
		aplicaEstimari(estimari);
	}

	// CU2: recomandarile si evenimentele se salveaza impreuna cu prognoza aprobata
	public List<EvenimentDomeniu> aproba(List<Scadenta> scadente) {
		verificaStare("aproba", StarePrognoza.CIORNA);
		calculeazaRecomandari(scadente);
		this.stare = StarePrognoza.APROBATA;

		List<EvenimentDomeniu> evenimente = new ArrayList<>();
		evenimente.add(new RecomandariPlataEvent(idPrognoza,
				recomandari.stream().map(RecomandarePlata::catreDto).toList()));
		for (ZiPrognozata zi : zile)
			if (zi.isDeficit())
				evenimente.add(new DeficitPrognozatEvent(idPrognoza, zi.getData(), soldMinim.scade(zi.getSoldEstimat())));
		return evenimente;
	}

	// CU3: primul realizat din perioada trece prognoza "In urmarire"
	public Optional<AbatereDepasitaEvent> inregistreazaRealizat(LocalDate data, Suma vanzari, Suma sold,
			boolean incompleta, PragAbatere prag) {
		if (stare == StarePrognoza.APROBATA)
			this.stare = StarePrognoza.IN_URMARIRE;
		verificaStare("inregistra realizatul", StarePrognoza.IN_URMARIRE);
		zi(data).inregistreazaRealizat(vanzari, sold, incompleta);

		if (sold != null && sold.esteMaiMicaDecat(soldMinim))
			return Optional.of(new AbatereDepasitaEvent(idPrognoza, data, null, "Sold sub soldul minim"));
		if (metoda == MetodaPrognoza.DOAR_OBLIGATII) // fara vanzari prognozate: conteaza doar soldul
			return Optional.empty();
		Abatere abatere = calculeazaAbatere(data);
		if (abatere.depaseste(prag))
			return Optional.of(new AbatereDepasitaEvent(idPrognoza, data, abatere, "Abatere peste prag"));
		return Optional.empty();
	}

	// cand soldul nu ajunge, plata unei facturi nescadente se poate muta, dar nu dupa scadenta
	public void amanaRecomandarea(Long idFactura, LocalDate dataNoua) {
		verificaStare("amana o recomandare", StarePrognoza.APROBATA, StarePrognoza.IN_URMARIRE);
		recomandari.stream().filter(r -> r.getIdFactura().equals(idFactura)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Factura " + idFactura + " nu are recomandare."))
				.amana(dataNoua);
	}

	public PrognozaInvalidataEvent invalideaza(String motiv) {
		verificaStare("invalida", StarePrognoza.APROBATA, StarePrognoza.IN_URMARIRE);
		this.stare = StarePrognoza.INVALIDATA;
		return new PrognozaInvalidataEvent(idPrognoza, motiv);
	}

	// CU4: inchiderea dupa ultima zi monitorizata, cu MAPE-ul calculat pentru fiecare metoda
	public void inchide(Map<MetodaPrognoza, BigDecimal> mapePeMetode) {
		verificaStare("inchide", StarePrognoza.IN_URMARIRE);
		if (zile.get(zile.size() - 1).getVanzariRealizate() == null)
			throw new IllegalStateException("Prognoza " + idPrognoza + " se inchide doar dupa ultima zi monitorizata.");
		this.mape = mapePeMetode.get(metoda);
		this.stare = StarePrognoza.INCHISA;
	}

	public boolean seSuprapuneCu(Perioada alta) {
		return perioada.seSuprapuneCu(alta);
	}

	// sold(zi) = sold(zi anterioara) + incasari(zi) - plati(zi), pornind de la soldInitial
	private void recalculeazaSolduri() {
		Suma sold = soldInitial;
		for (ZiPrognozata zi : zile)
			sold = zi.actualizeazaSold(sold, soldMinim);
	}

	// recomandare doar pentru facturile cu scadenta in perioada, cel tarziu la scadenta
	private void calculeazaRecomandari(List<Scadenta> scadente) {
		recomandari.clear();
		for (Scadenta scadenta : scadente)
			if (scadenta.idFactura() != null && perioada.contine(scadenta.data()))
				recomandari.add(new RecomandarePlata(scadenta.idFactura(), scadenta.suma(), scadenta.data(),
						scadenta.data(), this));
	}

	// vanzari realizate cumulate vs. prognozate cumulate, pana la data inclusiv
	private Abatere calculeazaAbatere(LocalDate data) {
		Suma realizat = Suma.zero(soldInitial.getMoneda());
		Suma prognozat = Suma.zero(soldInitial.getMoneda());
		for (ZiPrognozata zi : zile)
			if (!zi.getData().isAfter(data) && zi.getVanzariRealizate() != null) {
				realizat = realizat.aduna(zi.getVanzariRealizate());
				prognozat = prognozat.aduna(zi.getVanzariEstimate());
			}
		return Abatere.intre(realizat, prognozat);
	}

	private void aplicaEstimari(List<EstimareZi> estimari) {
		for (ZiPrognozata zi : zile)
			zi.aplicaEstimare(estimari.stream().filter(e -> e.data().equals(zi.getData())).findFirst()
					.orElse(new EstimareZi(zi.getData(), Suma.zero(soldInitial.getMoneda()),
							Suma.zero(soldInitial.getMoneda()), null)));
		recalculeazaSolduri();
	}

	private Suma platiDin(List<Scadenta> scadente, LocalDate data) {
		return scadente.stream().filter(s -> s.data().equals(data)).map(Scadenta::suma)
				.reduce(Suma.zero(soldInitial.getMoneda()), Suma::aduna);
	}

	private ZiPrognozata zi(LocalDate data) {
		return zile.stream().filter(z -> z.getData().equals(data)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Data " + data + " nu este in perioada " + perioada + "."));
	}

	private void verificaStare(String actiune, StarePrognoza... permise) {
		for (StarePrognoza permisa : permise)
			if (stare == permisa)
				return;
		throw new IllegalStateException("Nu se poate " + actiune + " prognoza " + idPrognoza + ": starea este " + stare + ".");
	}

	public Long getIdPrognoza() {
		return idPrognoza;
	}

	public Perioada getPerioada() {
		return perioada;
	}

	public MetodaPrognoza getMetoda() {
		return metoda;
	}

	public Suma getSoldInitial() {
		return soldInitial;
	}

	public Suma getSoldMinim() {
		return soldMinim;
	}

	public StarePrognoza getStare() {
		return stare;
	}

	public List<ZiPrognozata> getZile() {
		return Collections.unmodifiableList(zile);
	}

	public List<RecomandarePlata> getRecomandari() {
		return Collections.unmodifiableList(recomandari);
	}

	public BigDecimal getMape() {
		return mape;
	}

	@Override
	public String toString() {
		return "Prognoza [idPrognoza=" + idPrognoza + ", perioada=" + perioada + ", metoda=" + metoda + ", stare="
				+ stare + ", zile=" + zile.size() + ", recomandari=" + recomandari.size() + "]";
	}
}
