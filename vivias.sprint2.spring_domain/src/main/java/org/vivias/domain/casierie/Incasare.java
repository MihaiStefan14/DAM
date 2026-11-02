package org.vivias.domain.casierie;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Component Entity in cadrul agregatului RegistruCasa.
 * Corespunde conceptului "Incasare cu cardul"/"Platile mici in numerar"
 * din Cap.1, subdomeniul Casierie si incasari (Supporting Subdomain).
 */
@Entity
public class Incasare implements Serializable, Comparable<Incasare> {

	@Id
	@GeneratedValue
	private Integer incasareId;

	@NotNull(message = "Suma incasarii este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "valoare", column = @Column(name = "incasare_valoare")) })
	private Suma suma;

	@NotNull(message = "Tipul incasarii este obligatoriu!")
	@Enumerated
	private TipIncasare tip;

	private String clientSauObservatii;

	private LocalDateTime oraInregistrarii = LocalDateTime.now();

	public Incasare() {
		super();
	}

	public Incasare(Suma suma, TipIncasare tip, String clientSauObservatii) {
		this.suma = suma;
		this.tip = tip;
		this.clientSauObservatii = clientSauObservatii;
	}

	public Integer getIncasareId() {
		return incasareId;
	}

	public void setIncasareId(Integer incasareId) {
		this.incasareId = incasareId;
	}

	public Suma getSuma() {
		return suma;
	}

	public void setSuma(Suma suma) {
		this.suma = suma;
	}

	public TipIncasare getTip() {
		return tip;
	}

	public void setTip(TipIncasare tip) {
		this.tip = tip;
	}

	public String getClientSauObservatii() {
		return clientSauObservatii;
	}

	public void setClientSauObservatii(String clientSauObservatii) {
		this.clientSauObservatii = clientSauObservatii;
	}

	public LocalDateTime getOraInregistrarii() {
		return oraInregistrarii;
	}

	public void setOraInregistrarii(LocalDateTime oraInregistrarii) {
		this.oraInregistrarii = oraInregistrarii;
	}

	@Override
	public int compareTo(Incasare alta) {
		return this.oraInregistrarii.compareTo(alta.oraInregistrarii);
	}

	@Override
	public String toString() {
		return "\n\t\tIncasare [incasareId=" + incasareId + ", suma=" + suma + ", tip=" + tip + "]";
	}

	/* Value Object: Enum */
	public enum TipIncasare {
		NUMERAR, CARD;
	}
}
