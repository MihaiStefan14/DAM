package org.vivias.domain.casierie;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Embeddable;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * Value Object (Shared Kernel in Tabelul 1.2 din Cap.1): o sumă de bani
 * exprimată printr-o valoare și o monedă. Două sume se adună doar dacă au
 * aceeași monedă.
 */
@Embeddable
public class Suma implements Serializable, Comparable<Suma> {

	@NotNull(message = "Valoarea sumei este obligatorie!")
	@DecimalMin(value = "0.0", message = "Suma nu poate fi negativă!")
	private BigDecimal valoare;

	private Moneda moneda = Moneda.RON;

	public Suma() {
		super();
	}

	public Suma(BigDecimal valoare) {
		this.valoare = valoare;
		this.moneda = Moneda.RON;
	}

	public Suma(BigDecimal valoare, Moneda moneda) {
		this.valoare = valoare;
		this.moneda = moneda;
	}

	public Suma(double valoare) {
		this(BigDecimal.valueOf(valoare));
	}

	public BigDecimal getValoare() {
		return valoare;
	}

	public void setValoare(BigDecimal valoare) {
		this.valoare = valoare;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public Suma adauga(Suma alta) {
		if (alta == null)
			return this;
		if (this.moneda != alta.moneda)
			throw new IllegalArgumentException("Nu se pot aduna sume in monede diferite: " + this.moneda + " vs " + alta.moneda);
		return new Suma(this.valoare.add(alta.valoare), this.moneda);
	}

	public Suma scade(Suma alta) {
		if (alta == null)
			return this;
		if (this.moneda != alta.moneda)
			throw new IllegalArgumentException("Nu se pot scadea sume in monede diferite: " + this.moneda + " vs " + alta.moneda);
		return new Suma(this.valoare.subtract(alta.valoare), this.moneda);
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
