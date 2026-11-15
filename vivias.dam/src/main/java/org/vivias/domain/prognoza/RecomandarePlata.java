package org.vivias.domain.prognoza;

import java.io.Serializable;
import java.time.LocalDate;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// Component Entity (APOO, Figura 6.a): cand sa fie platita o factura; tine IdFactura, nu factura din Plati
@Entity
public class RecomandarePlata implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	private Long idFactura;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	private LocalDate scadenta;

	private LocalDate dataRecomandata;

	// Back-reference spre radacina agregatului
	@ManyToOne
	private Prognoza prognoza;

	// necesar doar pentru JPA
	protected RecomandarePlata() {
		super();
	}

	RecomandarePlata(Long idFactura, Suma suma, LocalDate scadenta, LocalDate dataRecomandata, Prognoza prognoza) {
		this.idFactura = idFactura;
		this.suma = suma;
		this.scadenta = scadenta;
		this.dataRecomandata = dataRecomandata;
		this.prognoza = prognoza;
	}

	// o plata recomandata dupa scadenta ar aduce penalitati (Tabelul 2.1)
	void amana(LocalDate dataNoua) {
		if (dataNoua.isAfter(scadenta))
			throw new IllegalArgumentException("Plata facturii " + idFactura + " nu poate fi amanata dupa scadenta "
					+ scadenta + ".");
		this.dataRecomandata = dataNoua;
	}

	RecomandareDTO catreDto() {
		return new RecomandareDTO(idFactura, suma, scadenta, dataRecomandata);
	}

	public Long getId() {
		return id;
	}

	public Long getIdFactura() {
		return idFactura;
	}

	public Suma getSuma() {
		return suma;
	}

	public LocalDate getScadenta() {
		return scadenta;
	}

	public LocalDate getDataRecomandata() {
		return dataRecomandata;
	}

	@Override
	public String toString() {
		return "RecomandarePlata [idFactura=" + idFactura + ", suma=" + suma + ", dataRecomandata=" + dataRecomandata
				+ "]";
	}
}
