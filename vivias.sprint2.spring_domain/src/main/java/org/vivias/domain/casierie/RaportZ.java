package org.vivias.domain.casierie;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Value Object: raportul fiscal Z de inchidere zilnica al casei de marcat (AMEF).
 * Odata tiparit nu se mai schimba, deci e imutabil (APOO, Figura 6.b).
 * Vanzarile in numerar intra in casa; cele pe card merg la banca (IncasareCard,
 * "In decontare"), deci NU intra in soldul casei.
 */
@Embeddable
public class RaportZ implements Serializable {

	@Min(value = 1, message = "Numarul raportului Z trebuie sa fie pozitiv!")
	private int numar;

	@NotNull(message = "Vanzarile pe card din raportul Z sunt obligatorii!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "raportz_card_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "raportz_card_moneda")) })
	private Suma vanzariCard;

	@NotNull(message = "Vanzarile in numerar din raportul Z sunt obligatorii!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "raportz_numerar_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "raportz_numerar_moneda")) })
	private Suma vanzariNumerar;

	// necesar doar pentru JPA (@Embeddable)
	protected RaportZ() {
		super();
	}

	public RaportZ(int numar, Suma vanzariCard, Suma vanzariNumerar) {
		this.numar = numar;
		this.vanzariCard = vanzariCard;
		this.vanzariNumerar = vanzariNumerar;
	}

	public int getNumar() {
		return numar;
	}

	public Suma getVanzariCard() {
		return vanzariCard;
	}

	public Suma getVanzariNumerar() {
		return vanzariNumerar;
	}

	@Override
	public String toString() {
		return "RaportZ [numar=" + numar + ", vanzariCard=" + vanzariCard + ", vanzariNumerar=" + vanzariNumerar + "]";
	}
}
