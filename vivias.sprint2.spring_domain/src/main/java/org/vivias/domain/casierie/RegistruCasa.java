package org.vivias.domain.casierie;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
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
 * Aggregate Root: Registrul de casa zilnic (User Story 5, Sprint 1 -
 * "Inchiderea zilei in registrul de casa"; Cap.1 Glosar - "Registru de
 * casa zilnic", subdomeniul Casierie si incasari).
 *
 * Components: Incasare (0..n), Plata (0..n).
 * Value Objects: Suma (soldInitial/soldFaptic), StareRegistru, TipDiferenta.
 */
@Entity
public class RegistruCasa implements Serializable, Comparable<RegistruCasa> {

	@Min(1)
	@NotNull(message = "RegistruId este obligatoriu!")
	@Id
	@GeneratedValue
	private Integer registruId;

	@NotNull(message = "Data registrului este obligatorie!")
	private LocalDate data;

	@NotNull(message = "Soldul initial este obligatoriu!")
	@Valid
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "valoare", column = @Column(name = "sold_initial_valoare")) })
	private Suma soldInitial;

	// numarul faptic (fizic) din casa; se completeaza doar la inchiderea zilei
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "valoare", column = @Column(name = "sold_faptic_valoare")) })
	private Suma soldFaptic;

	@NotNull(message = "Starea registrului este obligatorie!")
	@Enumerated
	private StareRegistru stare = StareRegistru.DESCHIS;

	@OneToMany(cascade = ALL, fetch = EAGER, orphanRemoval = true)
	private List<Incasare> incasari = new ArrayList<>();

	@OneToMany(cascade = ALL, fetch = EAGER, orphanRemoval = true)
	private List<Plata> plati = new ArrayList<>();

	/*
	 * Computed fields: soldCalculat, diferenta*
	 * - transient, encapsulate-only-getters
	 * - computing rule: CalculatingRegistruCasaDomainService (inchidere zi)
	 */
	@Transient
	protected Suma soldCalculat;

	@Transient
	protected Suma diferentaValoare;

	@Transient
	protected TipDiferenta diferentaTip;

	public RegistruCasa() {
		super();
	}

	public RegistruCasa(Integer registruId, LocalDate data, Suma soldInitial) {
		super();
		this.registruId = registruId;
		this.data = data;
		this.soldInitial = soldInitial;
	}

	public Integer getRegistruId() {
		return registruId;
	}

	public void setRegistruId(Integer registruId) {
		this.registruId = registruId;
	}

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		this.data = data;
	}

	public Suma getSoldInitial() {
		return soldInitial;
	}

	public void setSoldInitial(Suma soldInitial) {
		this.soldInitial = soldInitial;
	}

	public Suma getSoldFaptic() {
		return soldFaptic;
	}

	public void setSoldFaptic(Suma soldFaptic) {
		this.soldFaptic = soldFaptic;
	}

	public StareRegistru getStare() {
		return stare;
	}

	public void setStare(StareRegistru stare) {
		this.stare = stare;
	}

	public List<Incasare> getIncasari() {
		return incasari;
	}

	public void setIncasari(List<Incasare> incasari) {
		this.incasari = incasari;
	}

	public List<Plata> getPlati() {
		return plati;
	}

	public void setPlati(List<Plata> plati) {
		this.plati = plati;
	}

	public Suma getSoldCalculat() {
		return soldCalculat;
	}

	public void setSoldCalculat(Suma soldCalculat) {
		this.soldCalculat = soldCalculat;
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

	public void adaugaIncasare(Incasare incasare) {
		this.incasari.add(incasare);
	}

	public void adaugaPlata(Plata plata) {
		this.plati.add(plata);
	}

	@AssertTrue(message = "Soldul faptic trebuie completat pentru un registru cu ziua inchisa/regularizata!")
	public boolean isSoldFapticValid() {
		if (stare == StareRegistru.DESCHIS)
			return true;
		return soldFaptic != null;
	}

	@Override
	public int compareTo(RegistruCasa alt) {
		return this.data.compareTo(alt.getData());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((registruId == null) ? 0 : registruId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		RegistruCasa other = (RegistruCasa) obj;
		if (registruId == null)
			return other.registruId == null;
		return registruId.equals(other.registruId);
	}

	@Override
	public String toString() {
		return "\nRegistruCasa [registruId=" + registruId + ", data=" + data + ", stare=" + stare + ", soldInitial="
				+ soldInitial + ", incasari=" + incasari + ", plati=" + plati + "]";
	}
}
