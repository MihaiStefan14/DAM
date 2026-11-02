package org.vivias.domain.services;

import java.time.LocalDate;
import java.util.Collection;

import org.vivias.domain.casierie.RegistruCasa;

public interface IRegistruCasaEntityRepository {
	public Long getNextID();

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
