package org.vivias.domain.plati;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

// Value Object (APOO, Figura 6.c): codul fiscal isi verifica cifra de control la creare (cheia 753217532)
@Embeddable
public class CIF implements Serializable {

	private static final String CHEIE = "753217532";

	private String valoare;

	// necesar doar pentru JPA (@Embeddable)
	protected CIF() {
		super();
	}

	public CIF(String text) {
		String cif = text == null ? "" : text.replace(" ", "").toUpperCase();
		if (cif.startsWith("RO"))
			cif = cif.substring(2);
		if (!cif.matches("[0-9]{2,10}") || !cifraDeControlValida(cif))
			throw new IllegalArgumentException("CIF invalid: " + text);
		this.valoare = cif;
	}

	private static boolean cifraDeControlValida(String cif) {
		String corp = String.format("%9s", cif.substring(0, cif.length() - 1)).replace(' ', '0');
		int suma = 0;
		for (int i = 0; i < 9; i++)
			suma += (corp.charAt(i) - '0') * (CHEIE.charAt(i) - '0');
		int control = suma * 10 % 11;
		if (control == 10)
			control = 0;
		return control == cif.charAt(cif.length() - 1) - '0';
	}

	public String getValoare() {
		return valoare;
	}

	@Override
	public int hashCode() {
		return valoare.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CIF other && valoare.equals(other.valoare);
	}

	@Override
	public String toString() {
		return valoare;
	}
}
