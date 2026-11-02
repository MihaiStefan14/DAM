package org.vivias.domain.casierie;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfata comuna a evenimentelor de domeniu (APOO, cap. 2): un fapt petrecut,
 * imutabil, cu identitate proprie (eventId), ca destinatarul sa poata ignora
 * un eveniment primit a doua oara.
 */
public interface EvenimentDomeniu {
	UUID getEventId();

	Instant getDataProducerii();
}
