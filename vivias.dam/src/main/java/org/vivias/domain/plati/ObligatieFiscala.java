package org.vivias.domain.plati;

import java.io.Serializable;
import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;
import org.vivias.domain.shared.Perioada;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.c): D112, TVA sau impozit micro; suma e estimata pana la depunerea declaratiei
@Entity
public class ObligatieFiscala implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull(message = "Tipul obligatiei este obligatoriu!")
	@Enumerated(EnumType.STRING)
	private TipObligatie tip;

	@NotNull(message = "Perioada fiscala este obligatorie!")
	@Valid
	@Embedded
	private Perioada perioada;

	@NotNull
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@NotNull(message = "Scadenta este obligatorie!")
	private LocalDate scadenta;

	private boolean estimata = true;

	// necesar doar pentru JPA
	protected ObligatieFiscala() {
		super();
	}

	public ObligatieFiscala(TipObligatie tip, Perioada perioada, Suma sumaEstimata, LocalDate scadenta) {
		this.tip = tip;
		this.perioada = perioada;
		this.suma = sumaEstimata;
		this.scadenta = scadenta;
	}

	public void confirmaSuma(Suma sumaExacta) {
		if (sumaExacta == null || sumaExacta.semn() < 0)
			throw new IllegalArgumentException("Suma obligatiei nu poate fi negativa.");
		this.suma = sumaExacta;
		this.estimata = false;
	}

	public Long getId() {
		return id;
	}

	public TipObligatie getTip() {
		return tip;
	}

	public Perioada getPerioada() {
		return perioada;
	}

	public Suma getSuma() {
		return suma;
	}

	public LocalDate getScadenta() {
		return scadenta;
	}

	public boolean isEstimata() {
		return estimata;
	}

	@Override
	public String toString() {
		return "ObligatieFiscala [tip=" + tip + ", perioada=" + perioada + ", suma=" + suma + ", scadenta=" + scadenta
				+ ", estimata=" + estimata + "]";
	}
}
