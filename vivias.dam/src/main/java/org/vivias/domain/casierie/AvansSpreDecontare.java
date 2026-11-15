package org.vivias.domain.casierie;

import java.io.Serializable;
import java.time.DayOfWeek;
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
import jakarta.validation.constraints.Size;

// Aggregate Root (APOO, Figura 6.b): avansul in numerar dat unui angajat, justificat cu bonuri in 3 zile lucratoare
@Entity
public class AvansSpreDecontare implements Serializable {

	@Id
	@GeneratedValue
	private Long idAvans;

	@NotNull(message = "Angajatul este obligatoriu!")
	private Long idAngajat;

	@NotNull(message = "Suma avansului este obligatorie!")
	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@NotNull(message = "Destinatia avansului este obligatorie!")
	@Size(min = 1, message = "Destinatia avansului nu poate fi goala!")
	private String destinatie;

	@NotNull(message = "Data acordarii este obligatorie!")
	private LocalDate dataAcordare;

	@Valid
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "cheltuieli_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "cheltuieli_moneda")) })
	private Suma cheltuieli; // [0..1]: suma bonurilor aduse la decontare

	@NotNull
	@Enumerated(EnumType.STRING)
	private StareAvans stare = StareAvans.ACORDAT;

	// alerta "avans nejustificat" se trimite o singura data
	private boolean alertaTrimisa;

	// necesar doar pentru JPA
	protected AvansSpreDecontare() {
		super();
	}

	private AvansSpreDecontare(Long idAngajat, Suma suma, String destinatie, LocalDate dataAcordare) {
		this.idAngajat = idAngajat;
		this.suma = suma;
		this.destinatie = destinatie;
		this.dataAcordare = dataAcordare;
	}

	// Factory: avansul se acorda dupa aprobarea administratorului (CU6)
	public static AvansSpreDecontare acorda(Long idAngajat, Suma suma, String destinatie, LocalDate dataAcordare) {
		if (idAngajat == null)
			throw new IllegalArgumentException("Avansul trebuie acordat unui angajat.");
		if (suma == null || suma.semn() <= 0)
			throw new IllegalArgumentException("Suma avansului trebuie sa fie pozitiva.");
		return new AvansSpreDecontare(idAngajat, suma, destinatie, dataAcordare);
	}

	// intoarce restul de restituit in casa (avans - cheltuieli); negativ daca firma datoreaza diferenta
	public Suma deconteaza(Suma cheltuieli) {
		if (stare != StareAvans.ACORDAT)
			throw new IllegalStateException("Avansul " + idAvans + " este deja decontat.");
		if (cheltuieli == null || cheltuieli.semn() < 0)
			throw new IllegalArgumentException("Cheltuielile decontate trebuie sa fie >= 0.");
		this.cheltuieli = cheltuieli;
		this.stare = StareAvans.DECONTAT;
		return suma.scade(cheltuieli);
	}

	public boolean esteNejustificat(LocalDate azi, int zileLucratoare) {
		return stare == StareAvans.ACORDAT && zileLucratoareDupaAcordare(azi) > zileLucratoare;
	}

	public AvansNejustificatEvent semnaleaza() {
		if (alertaTrimisa)
			throw new IllegalStateException("Alerta pentru avansul " + idAvans + " a fost deja trimisa.");
		this.alertaTrimisa = true;
		return new AvansNejustificatEvent(idAvans, idAngajat);
	}

	private int zileLucratoareDupaAcordare(LocalDate azi) {
		int zile = 0;
		for (LocalDate zi = dataAcordare.plusDays(1); !zi.isAfter(azi); zi = zi.plusDays(1))
			if (zi.getDayOfWeek() != DayOfWeek.SATURDAY && zi.getDayOfWeek() != DayOfWeek.SUNDAY)
				zile++;
		return zile;
	}

	public Long getIdAvans() {
		return idAvans;
	}

	public Long getIdAngajat() {
		return idAngajat;
	}

	public Suma getSuma() {
		return suma;
	}

	public String getDestinatie() {
		return destinatie;
	}

	public LocalDate getDataAcordare() {
		return dataAcordare;
	}

	public Suma getCheltuieli() {
		return cheltuieli;
	}

	public StareAvans getStare() {
		return stare;
	}

	public boolean isAlertaTrimisa() {
		return alertaTrimisa;
	}

	@Override
	public String toString() {
		return "AvansSpreDecontare [idAvans=" + idAvans + ", idAngajat=" + idAngajat + ", suma=" + suma + ", stare="
				+ stare + "]";
	}
}
