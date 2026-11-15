package org.vivias.domain.plati;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.vivias.domain.casierie.EvenimentDomeniu;
import org.vivias.domain.casierie.Suma;

// Domain Event: banca a executat plata; factura devine "Achitata", Prognoza primeste realizatul
public record PlataExecutataEvent(UUID eventId, Instant dataProducerii, Long idPlata, Long idFactura, Suma suma,
		LocalDate data) implements EvenimentDomeniu {

	public PlataExecutataEvent(Long idPlata, Long idFactura, Suma suma, LocalDate data) {
		this(UUID.randomUUID(), Instant.now(), idPlata, idFactura, suma, data);
	}

	@Override
	public UUID getEventId() {
		return eventId;
	}

	@Override
	public Instant getDataProducerii() {
		return dataProducerii;
	}
}
