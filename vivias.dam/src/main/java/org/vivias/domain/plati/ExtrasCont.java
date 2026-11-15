package org.vivias.domain.plati;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vivias.domain.casierie.Suma;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

// Aggregate Root (APOO, Figura 6.c): extrasul zilnic; un extras nu se importa de doua ori (data unica)
@Entity
public class ExtrasCont implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull(message = "Data extrasului este obligatorie!")
	@Column(unique = true)
	private LocalDate data;

	@NotNull
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "sold_final_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "sold_final_moneda")) })
	private Suma soldFinal;

	@OneToMany(mappedBy = "extras", cascade = ALL, fetch = EAGER, orphanRemoval = true)
	private List<TranzactieBancara> tranzactii = new ArrayList<>();

	// necesar doar pentru JPA
	protected ExtrasCont() {
		super();
	}

	public ExtrasCont(LocalDate data, Suma soldFinal) {
		this.data = data;
		this.soldFinal = soldFinal;
	}

	// la import: tranzactiile se adauga doar prin radacina
	public TranzactieBancara adaugaTranzactie(Suma suma, String referinta, SensTranzactie sens) {
		TranzactieBancara tranzactie = new TranzactieBancara(suma, referinta, sens, this);
		tranzactii.add(tranzactie);
		return tranzactie;
	}

	public List<TranzactieBancara> tranzactiiFaraPereche() {
		return tranzactii.stream().filter(t -> t.getStare() == StareReconciliere.NERECONCILIATA).toList();
	}

	public void marcheazaReconciliata(Long idTranzactie) {
		tranzactie(idTranzactie).marcheazaReconciliata();
	}

	public void marcheazaDiscrepanta(Long idTranzactie) {
		tranzactie(idTranzactie).marcheazaDiscrepanta();
	}

	private TranzactieBancara tranzactie(Long idTranzactie) {
		return tranzactii.stream().filter(t -> idTranzactie.equals(t.getId())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Tranzactia " + idTranzactie + " nu e in extrasul din " + data + "."));
	}

	public Long getId() {
		return id;
	}

	public LocalDate getData() {
		return data;
	}

	public Suma getSoldFinal() {
		return soldFinal;
	}

	public List<TranzactieBancara> getTranzactii() {
		return Collections.unmodifiableList(tranzactii);
	}

	@Override
	public String toString() {
		return "ExtrasCont [data=" + data + ", soldFinal=" + soldFinal + ", tranzactii=" + tranzactii + "]";
	}
}
