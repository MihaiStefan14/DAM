package org.vivias.domain.casierie;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain Event (APOO, Figura 6.b): ziua de casa s-a inchis. Casierie il publica
 * la inchiderea registrului (Open Host Service + Published Language "RealizatZilnic"),
 * iar Prognoza il foloseste pentru vanzarile si soldul realizat ale zilei.
 *
 * Creat doar de RegistruCasa.inchide(...); imutabil, fara setteri.
 */
public final class ZiCasaInchisaEvent implements EvenimentDomeniu {

	private final UUID eventId = UUID.randomUUID();
	private final Instant dataProducerii = Instant.now();

	private final Long idRegistru;
	private final LocalDate data;
	private final Suma vanzariCard;
	private final Suma vanzariNumerar;
	private final Suma soldCasa;
	// suma care trece de plafonul intern: devine propunere de depunere la banca
	private final Suma propunereDepunere;

	ZiCasaInchisaEvent(Long idRegistru, LocalDate data, Suma vanzariCard, Suma vanzariNumerar, Suma soldCasa,
			Suma propunereDepunere) {
		this.idRegistru = idRegistru;
		this.data = data;
		this.vanzariCard = vanzariCard;
		this.vanzariNumerar = vanzariNumerar;
		this.soldCasa = soldCasa;
		this.propunereDepunere = propunereDepunere;
	}

	@Override
	public UUID getEventId() {
		return eventId;
	}

	@Override
	public Instant getDataProducerii() {
		return dataProducerii;
	}

	public Long getIdRegistru() {
		return idRegistru;
	}

	public LocalDate getData() {
		return data;
	}

	public Suma getVanzariCard() {
		return vanzariCard;
	}

	public Suma getVanzariNumerar() {
		return vanzariNumerar;
	}

	public Suma getSoldCasa() {
		return soldCasa;
	}

	public Suma getPropunereDepunere() {
		return propunereDepunere;
	}

	@Override
	public String toString() {
		return "ZiCasaInchisaEvent [eventId=" + eventId + ", idRegistru=" + idRegistru + ", data=" + data
				+ ", vanzariCard=" + vanzariCard + ", vanzariNumerar=" + vanzariNumerar + ", soldCasa=" + soldCasa
				+ ", propunereDepunere=" + propunereDepunere + "]";
	}
}
