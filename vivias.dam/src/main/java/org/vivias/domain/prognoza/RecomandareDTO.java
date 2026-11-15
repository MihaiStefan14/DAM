package org.vivias.domain.prognoza;

import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;

// DTO trimis contextului Plati in RecomandariPlataEvent (fara referinte la entitati)
public record RecomandareDTO(Long idFactura, Suma suma, LocalDate scadenta, LocalDate dataRecomandata) {
}
