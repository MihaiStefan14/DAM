package org.vivias.domain.plati;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.c): numarul facturii e unic doar la acelasi furnizor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "idFurnizor", "numar" }))
public class FacturaFurnizor implements Serializable {

	@Id
	@GeneratedValue
	private Long idFactura;

	@NotNull(message = "Furnizorul este obligatoriu!")
	private Long idFurnizor; // referinta prin identitate spre agregatul Furnizor

	@NotNull(message = "Numarul facturii este obligatoriu!")
	private String numar;

	@NotNull
	private LocalDate dataEmitere;

	@NotNull(message = "Scadenta este obligatorie!")
	private LocalDate scadenta;

	@NotNull
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@NotNull
	@Enumerated(EnumType.STRING)
	private StareFactura stare = StareFactura.NEACHITATA;

	@Enumerated(EnumType.STRING)
	private SursaPlata sursaPlatii; // [0..1]

	private UUID eventAchitareId; // [0..1]: evenimentul care a achitat factura (unul primit de doua ori se ignora)

	// necesar doar pentru JPA
	protected FacturaFurnizor() {
		super();
	}

	public FacturaFurnizor(Long idFurnizor, String numar, LocalDate dataEmitere, LocalDate scadenta, Suma suma) {
		if (scadenta.isBefore(dataEmitere))
			throw new IllegalArgumentException("Scadenta facturii " + numar + " este inainte de data emiterii.");
		this.idFurnizor = idFurnizor;
		this.numar = numar;
		this.dataEmitere = dataEmitere;
		this.scadenta = scadenta;
		this.suma = suma;
	}

	public boolean esteNeachitata() {
		return stare == StareFactura.NEACHITATA;
	}

	// prin banca (PlataExecutataEvent) sau in numerar (PlataNumerarEfectuataEvent)
	public void marcheazaAchitata(SursaPlata sursa, UUID eventId) {
		if (eventId != null && eventId.equals(eventAchitareId))
			return;
		if (!esteNeachitata())
			throw new IllegalStateException("Factura " + numar + " este deja achitata (" + sursaPlatii + ").");
		this.stare = StareFactura.ACHITATA;
		this.sursaPlatii = sursa;
		this.eventAchitareId = eventId;
	}

	public Long getIdFactura() {
		return idFactura;
	}

	public Long getIdFurnizor() {
		return idFurnizor;
	}

	public String getNumar() {
		return numar;
	}

	public LocalDate getDataEmitere() {
		return dataEmitere;
	}

	public LocalDate getScadenta() {
		return scadenta;
	}

	public Suma getSuma() {
		return suma;
	}

	public StareFactura getStare() {
		return stare;
	}

	public SursaPlata getSursaPlatii() {
		return sursaPlatii;
	}

	@Override
	public String toString() {
		return "FacturaFurnizor [idFactura=" + idFactura + ", numar=" + numar + ", scadenta=" + scadenta + ", suma="
				+ suma + ", stare=" + stare + "]";
	}
}
