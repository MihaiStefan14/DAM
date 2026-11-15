package org.vivias.domain.plati;

import java.io.Serializable;
import java.time.LocalDate;

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
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.c): o depunere sau o decontare card anuntata de Casierie, asteptata in extras
@Entity
public class IncasareAsteptata implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private SursaIncasare sursa;

	@NotNull
	private String idSursa; // id-ul depunerii / incasarii card din Casierie

	@NotNull
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@NotNull
	private LocalDate data;

	private boolean confirmata;

	// necesar doar pentru JPA
	protected IncasareAsteptata() {
		super();
	}

	public IncasareAsteptata(SursaIncasare sursa, String idSursa, Suma suma, LocalDate data) {
		this.sursa = sursa;
		this.idSursa = idSursa;
		this.suma = suma;
		this.data = data;
	}

	// la decontarea cardului banca retine comisionul: suma primita poate fi mai mica
	public void confirma(Suma sumaPrimita) {
		if (confirmata)
			throw new IllegalStateException("Incasarea " + sursa + " " + idSursa + " este deja confirmata.");
		if (sursa == SursaIncasare.DEPUNERE && !suma.equals(sumaPrimita))
			throw new IllegalArgumentException("Depunerea " + idSursa + " trebuie sa apara in extras cu suma " + suma + ".");
		this.suma = sumaPrimita;
		this.confirmata = true;
	}

	public Long getId() {
		return id;
	}

	public SursaIncasare getSursa() {
		return sursa;
	}

	public String getIdSursa() {
		return idSursa;
	}

	public Suma getSuma() {
		return suma;
	}

	public LocalDate getData() {
		return data;
	}

	public boolean isConfirmata() {
		return confirmata;
	}

	@Override
	public String toString() {
		return "IncasareAsteptata [sursa=" + sursa + ", idSursa=" + idSursa + ", suma=" + suma + ", confirmata="
				+ confirmata + "]";
	}
}
