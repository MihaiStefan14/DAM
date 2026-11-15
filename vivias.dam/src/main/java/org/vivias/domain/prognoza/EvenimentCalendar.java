package org.vivias.domain.prognoza;

import java.io.Serializable;
import java.time.LocalDate;

import org.vivias.domain.shared.Perioada;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.a): zile cu mallul inchis, program redus sau campanii (corecteaza estimarile)
@Entity
public class EvenimentCalendar implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull(message = "Perioada evenimentului este obligatorie!")
	@Valid
	@Embedded
	private Perioada perioada;

	@NotNull(message = "Tipul evenimentului este obligatoriu!")
	@Enumerated(EnumType.STRING)
	private TipEveniment tip;

	private String descriere;

	// necesar doar pentru JPA
	protected EvenimentCalendar() {
		super();
	}

	public EvenimentCalendar(Perioada perioada, TipEveniment tip, String descriere) {
		this.perioada = perioada;
		this.tip = tip;
		this.descriere = descriere;
	}

	public boolean afecteaza(LocalDate data) {
		return perioada.contine(data);
	}

	public Long getId() {
		return id;
	}

	public Perioada getPerioada() {
		return perioada;
	}

	public TipEveniment getTip() {
		return tip;
	}

	public String getDescriere() {
		return descriere;
	}

	@Override
	public String toString() {
		return "EvenimentCalendar [perioada=" + perioada + ", tip=" + tip + ", descriere=" + descriere + "]";
	}
}
