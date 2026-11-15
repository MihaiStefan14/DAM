package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.Collection;

import org.vivias.domain.casierie.RegistruCasa;

// Complete JPA Repository: CRUD interface (implementare: RegistruCasaEntityRepositoryJPA)
public interface IRegistruCasaEntityRepository {
	public RegistruCasa getById(Long id);

	// un singur registru pe zi: null daca ziua nu are inca registru
	public RegistruCasa getByData(LocalDate data);

	// ultima zi INCHISA dinaintea datei: soldul ei faptic devine soldul de deschidere
	public RegistruCasa getUltimulInchisInainte(LocalDate data);

	public RegistruCasa get(RegistruCasa sample);

	public Collection<RegistruCasa> toCollection(); // getAll

	public RegistruCasa add(RegistruCasa entity);

	public Collection<RegistruCasa> addAll(Collection<RegistruCasa> entities);

	public boolean remove(RegistruCasa entity);

	public boolean removeAll(Collection<RegistruCasa> entities);

	public int size();
}
