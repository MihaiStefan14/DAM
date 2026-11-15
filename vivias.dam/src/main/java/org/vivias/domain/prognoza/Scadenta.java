package org.vivias.domain.prognoza;

import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;

// Value Object (APOO, Figura 6.a): o plata cunoscuta din perioada, primita de la contextul Plati
// idFactura [0..1]: doar scadentele de tip FACTURA primesc recomandare de plata
public record Scadenta(Long idFactura, TipScadenta tip, Suma suma, LocalDate data) {
}
