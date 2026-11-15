package org.vivias.domain.prognoza;

import java.io.Serializable;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

// Value Object (APOO, Figura 6.a): intervalul in care se asteapta soldul unei zile
@Embeddable
public class IntervalSold implements Serializable {

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "interval_min_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "interval_min_moneda")) })
	private Suma min;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "interval_max_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "interval_max_moneda")) })
	private Suma max;

	// necesar doar pentru JPA (@Embeddable)
	protected IntervalSold() {
		super();
	}

	public IntervalSold(Suma min, Suma max) {
		if (max.esteMaiMicaDecat(min))
			throw new IllegalArgumentException("Intervalul de sold are maximul sub minim.");
		this.min = min;
		this.max = max;
	}

	public boolean contine(Suma s) {
		return !s.esteMaiMicaDecat(min) && !max.esteMaiMicaDecat(s);
	}

	public Suma getMin() {
		return min;
	}

	public Suma getMax() {
		return max;
	}

	@Override
	public String toString() {
		return "[" + min + ", " + max + "]";
	}
}
