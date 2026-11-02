package org.vivias.domain.casierie;

import java.time.LocalDate;

/**
 * Domain Entity View (analog ProjectCurrentReleaseView din modelul
 * msd.t2_c4.spring_domain): structura tuplu, read-only, consolidata
 * pentru ecranul de inchidere a zilei (APOO CU5, pasul 3).
 */
public class RegistruCasaSumarView {
	private Long idRegistru;
	private LocalDate data;
	private StareRegistru stare;

	private Suma soldDeschidere;
	private Suma soldScriptic;
	private Suma soldFaptic;

	private Suma diferentaValoare;
	private TipDiferenta diferentaTip;

	// din raportul Z (cardul nu intra in casa, dar apare in raport)
	private Suma vanzariCard;
	private Suma vanzariNumerar;

	private Integer numarOperatiuni = 0;
	private Suma propunereDepunere;

	public RegistruCasaSumarView(Long idRegistru, LocalDate data, StareRegistru stare, Suma soldDeschidere,
			Suma soldScriptic, Suma soldFaptic, Suma diferentaValoare, TipDiferenta diferentaTip, Suma vanzariCard,
			Suma vanzariNumerar, Integer numarOperatiuni, Suma propunereDepunere) {
		super();
		this.idRegistru = idRegistru;
		this.data = data;
		this.stare = stare;
		this.soldDeschidere = soldDeschidere;
		this.soldScriptic = soldScriptic;
		this.soldFaptic = soldFaptic;
		this.diferentaValoare = diferentaValoare;
		this.diferentaTip = diferentaTip;
		this.vanzariCard = vanzariCard;
		this.vanzariNumerar = vanzariNumerar;
		this.numarOperatiuni = numarOperatiuni;
		this.propunereDepunere = propunereDepunere;
	}

	public Long getIdRegistru() {
		return idRegistru;
	}

	public LocalDate getData() {
		return data;
	}

	public StareRegistru getStare() {
		return stare;
	}

	public Suma getSoldDeschidere() {
		return soldDeschidere;
	}

	public Suma getSoldScriptic() {
		return soldScriptic;
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

	public Suma getVanzariCard() {
		return vanzariCard;
	}

	public Suma getVanzariNumerar() {
		return vanzariNumerar;
	}

	public Integer getNumarOperatiuni() {
		return numarOperatiuni;
	}

	public Suma getPropunereDepunere() {
		return propunereDepunere;
	}

	@Override
	public String toString() {
		return "RegistruCasaSumarView [idRegistru=" + idRegistru + ", data=" + data + ", stare=" + stare
				+ ", soldDeschidere=" + soldDeschidere + ", soldScriptic=" + soldScriptic + ", soldFaptic="
				+ soldFaptic + ", diferentaValoare=" + diferentaValoare + ", diferentaTip=" + diferentaTip
				+ ", vanzariCard=" + vanzariCard + ", vanzariNumerar=" + vanzariNumerar + ", numarOperatiuni="
				+ numarOperatiuni + ", propunereDepunere=" + propunereDepunere + "]";
	}
}
