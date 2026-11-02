package org.vivias.domain.casierie;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * Value Object (Shared Kernel in Tabelul 1.2 din Cap.1): o sumă de bani
 * exprimată printr-o valoare și o monedă. Două sume se adună doar dacă au
 * aceeași monedă.
 *
 * Imutabil: fara setteri, operatiile (adauga/scade/abs) intorc o noua Suma.
 */
@Embeddable
public class Suma implements Serializable, Comparable<Suma> {

	@NotNull(message = "Valoarea sumei este obligatorie!")
	@DecimalMin(value = "0.0", message = "Suma nu poate fi negativă!")
	private BigDecimal valoare;

	// VO Custom Data Type: persistat prin MonedaConverter (cod ISO)
	@Convert(converter = MonedaConverter.class)
	private Moneda moneda = Moneda.RON;

	// necesar doar pentru JPA (@Embeddable)
	protected Suma() {
		super();
	}

	public Suma(BigDecimal valoare) {
		this(valoare, Moneda.RON);
	}

	public Suma(BigDecimal valoare, Moneda moneda) {
		this.valoare = valoare;
		this.moneda = moneda;
	}

	public Suma(double valoare) {
		this(BigDecimal.valueOf(valoare));
	}

	public static Suma zero(Moneda moneda) {
		return new Suma(BigDecimal.ZERO, moneda);
	}

	public BigDecimal getValoare() {
		return valoare;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public Suma aduna(Suma alta) {
		if (alta == null)
			return this;
		verificaAceeasiMoneda(alta, "aduna");
		return new Suma(this.valoare.add(alta.valoare), this.moneda);
	}

	public Suma scade(Suma alta) {
		if (alta == null)
			return this;
		verificaAceeasiMoneda(alta, "scadea");
		return new Suma(this.valoare.subtract(alta.valoare), this.moneda);
	}

	public Suma absoluta() {
		return new Suma(this.valoare.abs(), this.moneda);
	}

	public boolean esteMaiMicaDecat(Suma alta) {
		verificaAceeasiMoneda(alta, "compara");
		return this.valoare.compareTo(alta.valoare) < 0;
	}

	// -1, 0 sau 1
	public int semn() {
		return this.valoare.signum();
	}

	// ex: "-50 lei", "1200 lei", "20 EUR" (pentru mesajele catre utilizator)
	public String formatata() {
		String unitate = (moneda == Moneda.RON) ? "lei" : moneda.name();
		return valoare.stripTrailingZeros().toPlainString() + " " + unitate;
	}

	public boolean areAceeasiMoneda(Suma alta) {
		return alta != null && this.moneda == alta.moneda;
	}

	private void verificaAceeasiMoneda(Suma alta, String operatie) {
		if (!areAceeasiMoneda(alta))
			throw new IllegalArgumentException(
					"Nu se pot " + operatie + " sume in monede diferite: " + this.moneda + " vs " + alta.moneda);
	}

	@Override
	public int compareTo(Suma alta) {
		return this.valoare.compareTo(alta.valoare);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moneda == null) ? 0 : moneda.hashCode());
		result = prime * result + ((valoare == null) ? 0 : valoare.stripTrailingZeros().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Suma other = (Suma) obj;
		if (moneda != other.moneda)
			return false;
		if (valoare == null)
			return other.valoare == null;
		return valoare.compareTo(other.valoare) == 0;
	}

	@Override
	public String toString() {
		return valoare + " " + moneda;
	}

	/* Value Object: Enum */
	public enum Moneda {
		RON, EUR, USD;
	}
}
