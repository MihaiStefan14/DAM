package org.vivias.domain.plati;

import java.io.Serializable;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// Component Entity (APOO, Figura 6.c): salariul net al unui angajat in statul de plata
@Entity
public class LinieStat implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	private Long idAngajat;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "salariu_net_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "salariu_net_moneda")) })
	private Suma salariuNet;

	@Embedded
	@AttributeOverride(name = "valoare", column = @Column(name = "iban"))
	private IBAN iban;

	// Back-reference spre radacina agregatului
	@ManyToOne
	private StatPlata statPlata;

	// necesar doar pentru JPA
	protected LinieStat() {
		super();
	}

	LinieStat(Long idAngajat, Suma salariuNet, IBAN iban, StatPlata statPlata) {
		this.idAngajat = idAngajat;
		this.salariuNet = salariuNet;
		this.iban = iban;
		this.statPlata = statPlata;
	}

	public Long getId() {
		return id;
	}

	public Long getIdAngajat() {
		return idAngajat;
	}

	public Suma getSalariuNet() {
		return salariuNet;
	}

	public IBAN getIban() {
		return iban;
	}

	@Override
	public String toString() {
		return "LinieStat [idAngajat=" + idAngajat + ", salariuNet=" + salariuNet + "]";
	}
}
