package org.vivias.domain.plati;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.Embeddable;

// Value Object (APOO, Figura 6.c): IBAN-ul isi verifica cifra de control (mod 97) la creare
@Embeddable
public class IBAN implements Serializable {

	private String valoare;

	// necesar doar pentru JPA (@Embeddable)
	protected IBAN() {
		super();
	}

	public IBAN(String text) {
		String iban = text == null ? "" : text.replace(" ", "").toUpperCase();
		if (!iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}") || !cifraDeControlValida(iban))
			throw new IllegalArgumentException("IBAN invalid: " + text);
		this.valoare = iban;
	}

	private static boolean cifraDeControlValida(String iban) {
		String rearanjat = iban.substring(4) + iban.substring(0, 4);
		StringBuilder cifre = new StringBuilder();
		for (char c : rearanjat.toCharArray())
			cifre.append(Character.getNumericValue(c)); // A=10 ... Z=35
		return new BigInteger(cifre.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
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
		return obj instanceof IBAN other && valoare.equals(other.valoare);
	}

	@Override
	public String toString() {
		return valoare;
	}
}
