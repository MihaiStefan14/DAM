package org.vivias.domain.plati;

import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Aggregate Root (APOO, Figura 6.c): identitatea e IdPartener, ca o greseala in CIF sa poata fi corectata
@Entity
public class Furnizor implements Serializable {

	@Id
	@GeneratedValue
	private Long idPartener;

	@NotNull(message = "CIF-ul furnizorului este obligatoriu!")
	@Embedded
	@AttributeOverride(name = "valoare", column = @Column(name = "cif", unique = true))
	private CIF cif;

	@NotNull(message = "Denumirea furnizorului este obligatorie!")
	@Size(min = 1, message = "Denumirea furnizorului nu poate fi goala!")
	private String denumire;

	@NotNull(message = "IBAN-ul furnizorului este obligatoriu!")
	@Embedded
	@AttributeOverride(name = "valoare", column = @Column(name = "iban"))
	private IBAN iban;

	@Min(value = 0, message = "Termenul de plata nu poate fi negativ!")
	private int termenPlataZile;

	// necesar doar pentru JPA
	protected Furnizor() {
		super();
	}

	public Furnizor(CIF cif, String denumire, IBAN iban, int termenPlataZile) {
		this.cif = cif;
		this.denumire = denumire;
		this.iban = iban;
		this.termenPlataZile = termenPlataZile;
	}

	public void schimbaIban(IBAN ibanNou) {
		if (ibanNou == null)
			throw new IllegalArgumentException("IBAN-ul nou lipseste.");
		this.iban = ibanNou;
	}

	public Long getIdPartener() {
		return idPartener;
	}

	public CIF getCif() {
		return cif;
	}

	public String getDenumire() {
		return denumire;
	}

	public IBAN getIban() {
		return iban;
	}

	public int getTermenPlataZile() {
		return termenPlataZile;
	}

	@Override
	public String toString() {
		return "Furnizor [idPartener=" + idPartener + ", cif=" + cif + ", denumire=" + denumire + ", iban=" + iban + "]";
	}
}
