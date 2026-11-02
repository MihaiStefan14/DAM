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
import javax.validation.constraints.Size;

/**
 * Component Entity in cadrul agregatului RegistruCasa.
 * Corespunde conceptului "Platile mici in numerar" / "Dispozitie de plata"
 * din Cap.1, subdomeniul Casierie si incasari (Supporting Subdomain).
 */
@Entity
public class Plata implements Serializable, Comparable<Plata> {

	@Id
	@GeneratedValue
	private Integer plataId;

	@NotNull(message = "Suma platii este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "valoare", column = @Column(name = "plata_valoare")) })
	private Suma suma;

	@NotNull(message = "Destinatia platii este obligatorie!")
	@Size(min = 1, message = "Destinatia platii nu poate fi goala!")
	private String destinatie; // ex: furnizor, salariu, avans

	@Enumerated
	private TipDestinatie tipDestinatie = TipDestinatie.FURNIZOR;

	private LocalDateTime oraInregistrarii = LocalDateTime.now();

	public Plata() {
		super();
	}

	public Plata(Suma suma, String destinatie, TipDestinatie tipDestinatie) {
		this.suma = suma;
		this.destinatie = destinatie;
		this.tipDestinatie = tipDestinatie;
	}

	public Integer getPlataId() {
		return plataId;
	}

	public void setPlataId(Integer plataId) {
		this.plataId = plataId;
	}

	public Suma getSuma() {
		return suma;
	}

	public void setSuma(Suma suma) {
		this.suma = suma;
	}

	public String getDestinatie() {
		return destinatie;
	}

	public void setDestinatie(String destinatie) {
		this.destinatie = destinatie;
	}

	public TipDestinatie getTipDestinatie() {
		return tipDestinatie;
	}

	public void setTipDestinatie(TipDestinatie tipDestinatie) {
		this.tipDestinatie = tipDestinatie;
	}

	public LocalDateTime getOraInregistrarii() {
		return oraInregistrarii;
	}

	public void setOraInregistrarii(LocalDateTime oraInregistrarii) {
		this.oraInregistrarii = oraInregistrarii;
	}

	@Override
	public int compareTo(Plata alta) {
		return this.oraInregistrarii.compareTo(alta.oraInregistrarii);
	}

	@Override
	public String toString() {
		return "\n\t\tPlata [plataId=" + plataId + ", suma=" + suma + ", destinatie=" + destinatie + "]";
	}

	/* Value Object: Enum */
	public enum TipDestinatie {
		FURNIZOR, SALARIU, AVANS, TAXA;
	}
}
