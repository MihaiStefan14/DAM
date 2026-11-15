package org.vivias.domain.prognoza;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.vivias.domain.casierie.EvenimentDomeniu;

// Domain Event: la aprobare, recomandarile de plata pleaca spre contextul Plati
public record RecomandariPlataEvent(UUID eventId, Instant dataProducerii, Long idPrognoza, List<RecomandareDTO> recomandari) implements EvenimentDomeniu {

	public RecomandariPlataEvent(Long idPrognoza, List<RecomandareDTO> recomandari) {
		this(UUID.randomUUID(), Instant.now(), idPrognoza, recomandari);
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
