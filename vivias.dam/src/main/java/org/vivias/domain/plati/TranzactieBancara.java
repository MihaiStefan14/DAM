package org.vivias.domain.plati;

import java.io.Serializable;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// Component Entity (APOO, Figura 6.c): o linie din extras, cautata la reconciliere dupa suma, referinta si data
@Entity
public class TranzactieBancara implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	private String referinta;

	@Enumerated(EnumType.STRING)
	private SensTranzactie sens;

	@Enumerated(EnumType.STRING)
	private StareReconciliere stare = StareReconciliere.NERECONCILIATA;

	// Back-reference spre radacina agregatului
	@ManyToOne
	private ExtrasCont extras;

	// necesar doar pentru JPA
	protected TranzactieBancara() {
		super();
	}

	TranzactieBancara(Suma suma, String referinta, SensTranzactie sens, ExtrasCont extras) {
		this.suma = suma;
		this.referinta = referinta;
		this.sens = sens;
		this.extras = extras;
	}

	// apelate doar de radacina (vizibilitate de pachet, "~" in diagrama)
	void marcheazaReconciliata() {
		this.stare = StareReconciliere.RECONCILIATA;
	}

	void marcheazaDiscrepanta() {
		this.stare = StareReconciliere.DISCREPANTA;
	}

	public Long getId() {
		return id;
	}

	public Suma getSuma() {
		return suma;
	}

	public String getReferinta() {
		return referinta;
	}

	public SensTranzactie getSens() {
		return sens;
	}

	public StareReconciliere getStare() {
		return stare;
	}

	// fara "extras" in toString: ar intra in recursivitate cu ExtrasCont.toString()
	@Override
	public String toString() {
		return "TranzactieBancara [suma=" + suma + ", referinta=" + referinta + ", sens=" + sens + ", stare=" + stare
				+ "]";
	}
}
