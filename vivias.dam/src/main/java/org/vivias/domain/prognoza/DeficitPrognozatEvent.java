package org.vivias.domain.prognoza;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.vivias.domain.casierie.EvenimentDomeniu;
import org.vivias.domain.casierie.Suma;

// Domain Event: o zi din prognoza aprobata are soldul estimat sub soldul minim
public record DeficitPrognozatEvent(UUID eventId, Instant dataProducerii, Long idPrognoza, LocalDate data, Suma sumaLipsa) implements EvenimentDomeniu {

	public DeficitPrognozatEvent(Long idPrognoza, LocalDate data, Suma sumaLipsa) {
		this(UUID.randomUUID(), Instant.now(), idPrognoza, data, sumaLipsa);
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
