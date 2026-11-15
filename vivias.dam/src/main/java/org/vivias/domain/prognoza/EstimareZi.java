package org.vivias.domain.prognoza;

import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;

// Value Object (APOO, Figura 6.a): estimarea unei zile, produsa de CalculPrognozaService
public record EstimareZi(LocalDate data, Suma vanzari, Suma incasariNete, IntervalSold interval) {
}
