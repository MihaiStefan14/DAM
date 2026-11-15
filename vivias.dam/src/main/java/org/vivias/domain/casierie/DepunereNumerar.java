package org.vivias.domain.casierie;

import java.io.Serializable;
import java.time.LocalDate;

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

/*
 * Aggregate Root (APOO, Figura 6.b): numerarul de dus la banca.
 * PROPUSA (la inchiderea zilei) -> IN_TRANZIT (dus la banca) -> CONFIRMATA (din extras)
 */
@Entity
public class DepunereNumerar implements Serializable {

	@Id
	@GeneratedValue
	private Long idDepunere;

	@NotNull(message = "Suma depunerii este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@NotNull(message = "Data depunerii este obligatorie!")
	private LocalDate data;

	@NotNull
	@Enumerated(EnumType.STRING)
	private StareDepunere stare = StareDepunere.PROPUSA;

	// necesar doar pentru JPA
	protected DepunereNumerar() {
		super();
	}

	private DepunereNumerar(Suma suma, LocalDate data) {
		this.suma = suma;
		this.data = data;
	}

	// Factory: propunerea de depunere de la inchiderea zilei (ce trece de plafonul intern)
	public static DepunereNumerar propune(Suma suma, LocalDate data) {
		if (suma == null || suma.semn() <= 0)
			throw new IllegalArgumentException("Suma propusa spre depunere trebuie sa fie pozitiva.");
		return new DepunereNumerar(suma, data);
	}

	// propunerea de ieri, nedusa la banca, se actualizeaza (aceiasi bani nu se propun de doua ori)
	public void actualizeazaSuma(Suma sumaNoua) {
		verificaStare(StareDepunere.PROPUSA, "actualiza suma");
		if (sumaNoua == null || sumaNoua.semn() <= 0)
			throw new IllegalArgumentException("Suma propusa spre depunere trebuie sa fie pozitiva.");
		this.suma = sumaNoua;
	}

	public DepunereNumerarEvent marcheazaDepusa() {
		verificaStare(StareDepunere.PROPUSA, "marca depusa");
		this.stare = StareDepunere.IN_TRANZIT;
		return new DepunereNumerarEvent(idDepunere, suma, data);
	}

	public void confirma() {
		verificaStare(StareDepunere.IN_TRANZIT, "confirma");
		this.stare = StareDepunere.CONFIRMATA;
	}

	private void verificaStare(StareDepunere asteptata, String actiune) {
		if (stare != asteptata)
			throw new IllegalStateException("Nu se poate " + actiune + " depunerea " + idDepunere + ": starea este "
					+ stare + ".");
	}

	public Long getIdDepunere() {
		return idDepunere;
	}

	public Suma getSuma() {
		return suma;
	}

	public LocalDate getData() {
		return data;
	}

	public StareDepunere getStare() {
		return stare;
	}

	@Override
	public String toString() {
		return "DepunereNumerar [idDepunere=" + idDepunere + ", suma=" + suma + ", data=" + data + ", stare=" + stare
				+ "]";
	}
}
