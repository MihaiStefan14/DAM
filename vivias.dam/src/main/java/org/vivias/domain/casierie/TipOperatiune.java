package org.vivias.domain.casierie;

/**
 * Value Object (Enum): tipul unei operatiuni din registrul de casa (APOO, Figura 6.b).
 * Fiecare tip stie in ce sens misca numerarul din casa.
 */
public enum TipOperatiune {
	INCASARE_NUMERAR(+1), // din raportul Z
	PLATA_FURNIZOR(-1), // dispozitie de plata (Legea 70/2015)
	DEPUNERE(-1), // numerar dus la banca
	AVANS(-1), // avans spre decontare
	RESTITUIRE(+1); // rest de avans adus inapoi

	private final int sens;

	TipOperatiune(int sens) {
		this.sens = sens;
	}

	public boolean esteIntrare() {
		return sens > 0;
	}

	// soldul casei dupa aceasta operatiune
	public Suma aplica(Suma sold, Suma suma) {
		return esteIntrare() ? sold.aduna(suma) : sold.scade(suma);
	}
}
