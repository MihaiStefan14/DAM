package org.vivias.domain.prognoza;

import java.math.BigDecimal;

import org.vivias.domain.casierie.Suma;

// Value Object (APOO, Figura 6.a): alerta se da peste 15% si peste 500 lei
public record PragAbatere(BigDecimal procent, Suma sumaMinima) {

	public static PragAbatere implicit() {
		return new PragAbatere(BigDecimal.valueOf(15), new Suma(BigDecimal.valueOf(500)));
	}
}
