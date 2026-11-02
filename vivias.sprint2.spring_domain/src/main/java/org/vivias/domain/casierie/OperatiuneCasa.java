package org.vivias.domain.casierie;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Component Entity in cadrul agregatului RegistruCasa (APOO, Figura 6.b):
 * o miscare de numerar din casa insulei - incasare din raportul Z, plata
 * catre furnizor, depunere la banca, avans sau restituire.
 *
 * Se creeaza doar prin RegistruCasa.inregistreazaOperatiune(...) /
 * ataseazaRaportZ(...), de aceea constructorul nu e public.
 */
@Entity
public class OperatiuneCasa implements Serializable, Comparable<OperatiuneCasa> {

	// ID local, in namespace-ul agregatului: atribuit de radacina
	@Id
	private Integer operatiuneId;

	@NotNull(message = "Tipul operatiunii este obligatoriu!")
	@Enumerated(EnumType.STRING)
	private TipOperatiune tip;

	@NotNull(message = "Suma operatiunii este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "operatiune_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "operatiune_moneda")) })
	private Suma suma;

	// documentul justificativ: raport Z, dispozitie de plata/incasare, foaie de varsamant
	@NotNull(message = "Documentul justificativ este obligatoriu!")
	@Size(min = 1, message = "Documentul justificativ nu poate fi gol!")
	private String document;

	// [0..1]: doar pentru PLATA_FURNIZOR (referinta prin id spre agregatul FacturaFurnizor)
	private Long idFactura;

	// [0..1]: doar pentru AVANS / RESTITUIRE (referinta prin id spre agregatul AvansSpreDecontare)
	private Long idAvans;

	private LocalDateTime oraInregistrarii = LocalDateTime.now();

	// Back-reference spre radacina agregatului (owner-ul relatiei: mappedBy="registru")
	@ManyToOne
	private RegistruCasa registru;

	// necesar doar pentru JPA
	protected OperatiuneCasa() {
		super();
	}

	OperatiuneCasa(Integer operatiuneId, TipOperatiune tip, Suma suma, String document, Long idFactura,
			Long idAvans, RegistruCasa registru) {
		this.operatiuneId = operatiuneId;
		this.tip = tip;
		this.suma = suma;
		this.document = document;
		this.idFactura = idFactura;
		this.idAvans = idAvans;
		this.registru = registru;
	}

	public Integer getOperatiuneId() {
		return operatiuneId;
	}

	public TipOperatiune getTip() {
		return tip;
	}

	public Suma getSuma() {
		return suma;
	}

	public String getDocument() {
		return document;
	}

	public Long getIdFactura() {
		return idFactura;
	}

	public Long getIdAvans() {
		return idAvans;
	}

	public LocalDateTime getOraInregistrarii() {
		return oraInregistrarii;
	}

	public RegistruCasa getRegistru() {
		return registru;
	}

	@Override
	public int compareTo(OperatiuneCasa alta) {
		return this.oraInregistrarii.compareTo(alta.oraInregistrarii);
	}

	// fara "registru" in toString: ar intra in recursivitate cu RegistruCasa.toString()
	@Override
	public String toString() {
		return "\n\t\tOperatiuneCasa [operatiuneId=" + operatiuneId + ", tip=" + tip + ", suma=" + suma
				+ ", document=" + document + "]";
	}
}
