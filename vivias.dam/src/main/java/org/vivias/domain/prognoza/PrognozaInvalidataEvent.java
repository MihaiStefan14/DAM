package org.vivias.domain.prognoza;

import java.time.Instant;
import java.util.UUID;

import org.vivias.domain.casierie.EvenimentDomeniu;

// Domain Event: prognoza invalidata; Platile anuleaza platile propuse din ea
public record PrognozaInvalidataEvent(UUID eventId, Instant dataProducerii, Long idPrognoza, String motiv) implements EvenimentDomeniu {

	public PrognozaInvalidataEvent(Long idPrognoza, String motiv) {
		this(UUID.randomUUID(), Instant.now(), idPrognoza, motiv);
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
