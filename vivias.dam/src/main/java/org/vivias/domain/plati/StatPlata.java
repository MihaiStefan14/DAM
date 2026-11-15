package org.vivias.domain.plati;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.c): statul de plata al unei luni. Components: LinieStat (1..*)
@Entity
public class StatPlata implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull(message = "Luna statului de plata este obligatorie!")
	@Convert(converter = YearMonthConverter.class)
	@Column(unique = true)
	private YearMonth luna;

	@OneToMany(mappedBy = "statPlata", cascade = ALL, fetch = EAGER, orphanRemoval = true)
	private List<LinieStat> linii = new ArrayList<>();

	// necesar doar pentru JPA
	protected StatPlata() {
		super();
	}

	public StatPlata(YearMonth luna) {
		this.luna = luna;
	}

	public LinieStat adaugaLinie(Long idAngajat, Suma salariuNet, IBAN iban) {
		if (linii.stream().anyMatch(l -> l.getIdAngajat().equals(idAngajat)))
			throw new IllegalArgumentException("Angajatul " + idAngajat + " are deja o linie in statul din " + luna + ".");
		LinieStat linie = new LinieStat(idAngajat, salariuNet, iban, this);
		linii.add(linie);
		return linie;
	}

	public Suma totalNet() {
		return linii.stream().map(LinieStat::getSalariuNet).reduce(Suma::aduna)
				.orElse(Suma.zero(Suma.Moneda.RON));
	}

	public Long getId() {
		return id;
	}

	public YearMonth getLuna() {
		return luna;
	}

	public List<LinieStat> getLinii() {
		return Collections.unmodifiableList(linii);
	}

	@Override
	public String toString() {
		return "StatPlata [luna=" + luna + ", linii=" + linii + "]";
	}
}
