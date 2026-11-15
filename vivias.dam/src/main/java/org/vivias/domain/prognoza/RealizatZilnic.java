package org.vivias.domain.prognoza;

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
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.a): vanzarile si soldurile unei zile incheiate (istoricul prognozei)
@Entity
public class RealizatZilnic implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull(message = "Data este obligatorie!")
	@Column(unique = true)
	private LocalDate data;

	@NotNull
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "vanzari_card_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "vanzari_card_moneda")) })
	private Suma vanzariCard;

	@NotNull
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "vanzari_numerar_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "vanzari_numerar_moneda")) })
	private Suma vanzariNumerar;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_casa_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_casa_moneda")) })
	private Suma soldCasa; // [0..1]: din ZiCasaInchisaEvent

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_banca_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_banca_moneda")) })
	private Suma soldBanca; // [0..1]: din ExtrasReconciliatEvent

	@NotNull
	@Enumerated(EnumType.STRING)
	private SursaRealizat sursa;

	private UUID eventSursaId; // [0..1]: un eveniment primit a doua oara este ignorat

	// necesar doar pentru JPA
	protected RealizatZilnic() {
		super();
	}

	public RealizatZilnic(LocalDate data, Suma vanzariCard, Suma vanzariNumerar, SursaRealizat sursa,
			UUID eventSursaId) {
		this.data = data;
		this.vanzariCard = vanzariCard;
		this.vanzariNumerar = vanzariNumerar;
		this.sursa = sursa;
		this.eventSursaId = eventSursaId;
	}

	public Suma totalVanzari() {
		return vanzariCard.aduna(vanzariNumerar);
	}

	// ziua e completa cand are si soldul casei, si soldul bancii
	public boolean esteComplet() {
		return soldCasa != null && soldBanca != null;
	}

	public void inregistreazaSoldCasa(Suma sold) {
		this.soldCasa = sold;
	}

	public void inregistreazaSoldBanca(Suma sold) {
		this.soldBanca = sold;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getData() {
		return data;
	}

	public Suma getVanzariCard() {
		return vanzariCard;
	}

	public Suma getVanzariNumerar() {
		return vanzariNumerar;
	}

	public Suma getSoldCasa() {
		return soldCasa;
	}

	public Suma getSoldBanca() {
		return soldBanca;
	}

	public SursaRealizat getSursa() {
		return sursa;
	}

	public UUID getEventSursaId() {
		return eventSursaId;
	}

	@Override
	public String toString() {
		return "RealizatZilnic [data=" + data + ", vanzariCard=" + vanzariCard + ", vanzariNumerar=" + vanzariNumerar
				+ ", sursa=" + sursa + "]";
	}
}
