package org.vivias.domain.prognoza;

import java.io.Serializable;
import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// Component Entity (APOO, Figura 6.a): o zi a prognozei; identitatea este data, in interiorul prognozei
@Entity
public class ZiPrognozata implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	private LocalDate data;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "vanzari_estimate_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "vanzari_estimate_moneda")) })
	private Suma vanzariEstimate;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "incasari_estimate_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "incasari_estimate_moneda")) })
	private Suma incasariEstimate;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "plati_cunoscute_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "plati_cunoscute_moneda")) })
	private Suma platiCunoscute;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_estimat_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_estimat_moneda")) })
	private Suma soldEstimat;

	@Embedded
	private IntervalSold interval; // [0..1]: lipseste la metoda DOAR_OBLIGATII

	private boolean deficit;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "vanzari_realizate_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "vanzari_realizate_moneda")) })
	private Suma vanzariRealizate; // [0..1]

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_realizat_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_realizat_moneda")) })
	private Suma soldRealizat; // [0..1]

	private boolean incompleta;

	// Back-reference spre radacina agregatului
	@ManyToOne
	private Prognoza prognoza;

	// necesar doar pentru JPA
	protected ZiPrognozata() {
		super();
	}

	ZiPrognozata(LocalDate data, Suma platiCunoscute, Prognoza prognoza) {
		this.data = data;
		this.platiCunoscute = platiCunoscute;
		this.prognoza = prognoza;
	}

	// apelat doar de radacina (vizibilitate de pachet, "~" in diagrama)
	Suma actualizeazaSold(Suma soldAnterior, Suma soldMinim) {
		this.soldEstimat = soldAnterior.aduna(incasariEstimate).scade(platiCunoscute);
		this.deficit = soldEstimat.esteMaiMicaDecat(soldMinim);
		return soldEstimat;
	}

	void inregistreazaRealizat(Suma vanzari, Suma sold, boolean incompleta) {
		this.vanzariRealizate = vanzari;
		this.soldRealizat = sold;
		this.incompleta = incompleta;
	}

	void aplicaEstimare(EstimareZi estimare) {
		this.vanzariEstimate = estimare.vanzari();
		this.incasariEstimate = estimare.incasariNete();
		this.interval = estimare.interval();
	}

	public Long getId() {
		return id;
	}

	public LocalDate getData() {
		return data;
	}

	public Suma getVanzariEstimate() {
		return vanzariEstimate;
	}

	public Suma getIncasariEstimate() {
		return incasariEstimate;
	}

	public Suma getPlatiCunoscute() {
		return platiCunoscute;
	}

	public Suma getSoldEstimat() {
		return soldEstimat;
	}

	public IntervalSold getInterval() {
		return interval;
	}

	public boolean isDeficit() {
		return deficit;
	}

	public Suma getVanzariRealizate() {
		return vanzariRealizate;
	}

	public Suma getSoldRealizat() {
		return soldRealizat;
	}

	public boolean isIncompleta() {
		return incompleta;
	}

	// fara "prognoza" in toString: ar intra in recursivitate cu Prognoza.toString()
	@Override
	public String toString() {
		return "ZiPrognozata [data=" + data + ", soldEstimat=" + soldEstimat + ", deficit=" + deficit + "]";
	}
}
