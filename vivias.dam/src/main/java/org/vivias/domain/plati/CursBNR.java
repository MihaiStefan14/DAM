package org.vivias.domain.plati;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.Suma.Moneda;

// Value Object (APOO, Figura 6.c): cursul BNR al unei zile, ex. pentru chiria in euro
public record CursBNR(Moneda moneda, BigDecimal valoare, LocalDate data) {

	public Suma converteste(Suma s) {
		if (s.getMoneda() != moneda)
			throw new IllegalArgumentException("Cursul este pentru " + moneda + ", suma este in " + s.getMoneda() + ".");
		return new Suma(s.getValoare().multiply(valoare).setScale(2, RoundingMode.HALF_UP), Moneda.RON);
	}
}
