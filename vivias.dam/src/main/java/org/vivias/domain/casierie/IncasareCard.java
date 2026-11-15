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

// Aggregate Root (APOO, Figura 6.b): vanzarile pe card ale unei zile, "In decontare" pana sosesc banii in extras
@Entity
public class IncasareCard implements Serializable {

	@Id
	@GeneratedValue
	private Long idIncasare;

	@NotNull(message = "Data vanzarii este obligatorie!")
	private LocalDate dataVanzare;

	@NotNull(message = "Suma bruta este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_bruta_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_bruta_moneda")) })
	private Suma sumaBruta;

	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_primita_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_primita_moneda")) })
	private Suma sumaPrimita; // [0..1]: suma neta decontata de banca

	@NotNull
	@Enumerated(EnumType.STRING)
	private StareCard stare = StareCard.IN_DECONTARE;

	// necesar doar pentru JPA
	protected IncasareCard() {
		super();
	}

	public IncasareCard(LocalDate dataVanzare, Suma sumaBruta) {
		this.dataVanzare = dataVanzare;
		this.sumaBruta = sumaBruta;
	}

	// intoarce comisionul bancii (suma bruta - suma primita)
	public Suma confirmaDecontarea(Suma sumaPrimita) {
		if (stare != StareCard.IN_DECONTARE)
			throw new IllegalStateException("Incasarea cu cardul din " + dataVanzare + " este deja decontata.");
		if (sumaPrimita == null || sumaPrimita.semn() < 0 || sumaBruta.esteMaiMicaDecat(sumaPrimita))
			throw new IllegalArgumentException("Suma primita trebuie sa fie intre 0 si suma bruta.");
		this.sumaPrimita = sumaPrimita;
		this.stare = StareCard.DECONTATA;
		return sumaBruta.scade(sumaPrimita);
	}

	public boolean esteIntarziata(LocalDate azi, int termenZile) {
		return stare == StareCard.IN_DECONTARE && azi.isAfter(dataVanzare.plusDays(termenZile));
	}

	public Long getIdIncasare() {
		return idIncasare;
	}

	public LocalDate getDataVanzare() {
		return dataVanzare;
	}

	public Suma getSumaBruta() {
		return sumaBruta;
	}

	public Suma getSumaPrimita() {
		return sumaPrimita;
	}

	public StareCard getStare() {
		return stare;
	}

	@Override
	public String toString() {
		return "IncasareCard [idIncasare=" + idIncasare + ", dataVanzare=" + dataVanzare + ", sumaBruta=" + sumaBruta
				+ ", stare=" + stare + "]";
	}
}
