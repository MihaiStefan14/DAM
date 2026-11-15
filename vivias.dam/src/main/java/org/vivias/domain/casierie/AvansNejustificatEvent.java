package org.vivias.domain.casierie;

import java.time.Instant;
import java.util.UUID;

// Domain Event: avansul nu a fost justificat in termen (alerta catre administrator)
public record AvansNejustificatEvent(UUID eventId, Instant dataProducerii, Long idAvans, Long idAngajat)
		implements EvenimentDomeniu {

	public AvansNejustificatEvent(Long idAvans, Long idAngajat) {
		this(UUID.randomUUID(), Instant.now(), idAvans, idAngajat);
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
