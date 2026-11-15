package org.vivias.domain.prognoza;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.vivias.domain.casierie.EvenimentDomeniu;

// Domain Event: realizatul s-a abatut de la prognoza peste prag (sau soldul e sub minim)
public record AbatereDepasitaEvent(UUID eventId, Instant dataProducerii, Long idPrognoza, LocalDate data, Abatere abatere, String motiv) implements EvenimentDomeniu {

	public AbatereDepasitaEvent(Long idPrognoza, LocalDate data, Abatere abatere, String motiv) {
		this(UUID.randomUUID(), Instant.now(), idPrognoza, data, abatere, motiv);
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
