package org.vivias.domain.casierie;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Domain Event: numerarul a plecat spre banca; Platile asteapta depunerea in extras
public record DepunereNumerarEvent(UUID eventId, Instant dataProducerii, Long idDepunere, Suma suma, LocalDate data)
		implements EvenimentDomeniu {

	public DepunereNumerarEvent(Long idDepunere, Suma suma, LocalDate data) {
		this(UUID.randomUUID(), Instant.now(), idDepunere, suma, data);
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
