package org.vivias.domain.casierie;

import java.time.LocalDate;

/**
 * Domain Entity View (analog ProjectCurrentReleaseView din modelul
 * msd.t2_c4.spring_domain): structura tuplu, read-only, consolidata
 * pentru raportarea starii unui registru de casa zilnic.
 */
public class RegistruCasaSumarView {
	private Integer registruId;
	private LocalDate data;
	private StareRegistru stare;

	private Suma soldInitial;
	private Suma soldCalculat;
	private Suma soldFaptic;

	private Suma diferentaValoare;
	private TipDiferenta diferentaTip;

	private Integer numarIncasari = 0;
	private Integer numarPlati = 0;

	public RegistruCasaSumarView(Integer registruId, LocalDate data, StareRegistru stare, Suma soldInitial,
			Suma soldCalculat, Suma soldFaptic, Suma diferentaValoare, TipDiferenta diferentaTip,
			Integer numarIncasari, Integer numarPlati) {
		super();
		this.registruId = registruId;
		this.data = data;
		this.stare = stare;
		this.soldInitial = soldInitial;
		this.soldCalculat = soldCalculat;
		this.soldFaptic = soldFaptic;
		this.diferentaValoare = diferentaValoare;
		this.diferentaTip = diferentaTip;
		this.numarIncasari = numarIncasari;
		this.numarPlati = numarPlati;
	}

	public Integer getRegistruId() {
		return registruId;
	}

	public LocalDate getData() {
		return data;
	}

	public StareRegistru getStare() {
		return stare;
	}

	public Suma getSoldInitial() {
		return soldInitial;
	}

	public Suma getSoldCalculat() {
		return soldCalculat;
	}

	public Suma getSoldFaptic() {
		return soldFaptic;
	}

	public Suma getDiferentaValoare() {
		return diferentaValoare;
	}

	public TipDiferenta getDiferentaTip() {
		return diferentaTip;
	}

	public Integer getNumarIncasari() {
		return numarIncasari;
	}

	public Integer getNumarPlati() {
		return numarPlati;
	}

	@Override
	public String toString() {
		return "RegistruCasaSumarView [registruId=" + registruId + ", data=" + data + ", stare=" + stare
				+ ", soldInitial=" + soldInitial + ", soldCalculat=" + soldCalculat + ", soldFaptic=" + soldFaptic
				+ ", diferentaValoare=" + diferentaValoare + ", diferentaTip=" + diferentaTip + ", numarIncasari="
				+ numarIncasari + ", numarPlati=" + numarPlati + "]";
	}
}
