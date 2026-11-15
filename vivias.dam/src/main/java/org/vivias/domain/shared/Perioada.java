package org.vivias.domain.shared;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

// Shared Kernel (APOO, Figura 6.a): un interval de zile, cu ambele capete incluse
@Embeddable
public class Perioada implements Serializable {

	@NotNull(message = "Inceputul perioadei este obligatoriu!")
	private LocalDate inceput;

	@NotNull(message = "Sfarsitul perioadei este obligatoriu!")
	private LocalDate sfarsit;

	// necesar doar pentru JPA (@Embeddable)
	protected Perioada() {
		super();
	}

	public Perioada(LocalDate inceput, LocalDate sfarsit) {
		if (inceput == null || sfarsit == null)
			throw new IllegalArgumentException("Perioada trebuie sa aiba inceput si sfarsit.");
		if (sfarsit.isBefore(inceput))
			throw new IllegalArgumentException("Perioada se termina (" + sfarsit + ") inainte sa inceapa (" + inceput + ").");
		this.inceput = inceput;
		this.sfarsit = sfarsit;
	}

	public boolean contine(LocalDate data) {
		return !data.isBefore(inceput) && !data.isAfter(sfarsit);
	}

	// regula "o singura prognoza activa pe aceleasi zile"
	public boolean seSuprapuneCu(Perioada alta) {
		return !alta.sfarsit.isBefore(this.inceput) && !alta.inceput.isAfter(this.sfarsit);
	}

	public int numarZile() {
		return (int) ChronoUnit.DAYS.between(inceput, sfarsit) + 1;
	}

	public LocalDate getInceput() {
		return inceput;
	}

	public LocalDate getSfarsit() {
		return sfarsit;
	}

	@Override
	public int hashCode() {
		return 31 * inceput.hashCode() + sfarsit.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Perioada other = (Perioada) obj;
		return inceput.equals(other.inceput) && sfarsit.equals(other.sfarsit);
	}

	@Override
	public String toString() {
		return inceput + " - " + sfarsit;
	}
}
