package org.vivias.domain.prognoza;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.vivias.domain.casierie.Suma;

/*
 * Value Object (APOO, Figura 6.a): abaterea vanzarilor realizate cumulate fata de cele prognozate.
 * procent [0..1]: daca prognozatul cumulat e zero, procentul nu se poate calcula si conteaza doar suma.
 */
public record Abatere(Suma diferenta, BigDecimal procent) {

	public static Abatere intre(Suma realizat, Suma prognozat) {
		Suma diferenta = realizat.scade(prognozat).absoluta();
		BigDecimal procent = prognozat.semn() == 0 ? null
				: diferenta.getValoare().multiply(BigDecimal.valueOf(100)).divide(prognozat.getValoare(), 2,
						RoundingMode.HALF_UP);
		return new Abatere(diferenta, procent);
	}

	public boolean depaseste(PragAbatere prag) {
		boolean pesteSuma = prag.sumaMinima().esteMaiMicaDecat(diferenta);
		if (procent == null)
			return pesteSuma;
		return pesteSuma && procent.compareTo(prag.procent()) > 0;
	}
}
