package org.vivias.domain.casierie;

/**
 * Value Object: plafoanele de numerar ale casei (APOO, Figura 6.b).
 * Sunt parametri ai aplicatiei, nu valori scrise in cod (APOO, cap. 1.1):
 * instanta folosita e definita in AppScanConfig.
 *
 * Deocamdata contine doar plafonul intern de casa stabilit de administrator
 * (2.000 lei); plafoanele din Legea 70/2015 tin de VerificarePlafoaneService.
 */
public final class PlafoaneNumerar {

	private final Suma plafonIntern;

	public PlafoaneNumerar(Suma plafonIntern) {
		if (plafonIntern == null || plafonIntern.semn() < 0)
			throw new IllegalArgumentException("Plafonul intern de casa trebuie sa fie >= 0!");
		this.plafonIntern = plafonIntern;
	}

	public Suma getPlafonIntern() {
		return plafonIntern;
	}

	// Suma propusa spre depunere la banca la inchiderea zilei: ce trece de plafonul intern (CU5, alternativa 2)
	public Suma sumaDeDepus(Suma soldCasa) {
		Suma surplus = soldCasa.scade(plafonIntern);
		return surplus.semn() > 0 ? surplus : Suma.zero(soldCasa.getMoneda());
	}

	@Override
	public String toString() {
		return "PlafoaneNumerar [plafonIntern=" + plafonIntern + "]";
	}
}
