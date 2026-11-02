package org.vivias.domain.services;

import java.util.Collection;

import org.vivias.domain.casierie.RegistruCasa;

public interface IRegistruCasaEntityRepository {
	public Integer getNextID();

	public RegistruCasa getById(Integer id);

	public RegistruCasa get(RegistruCasa sample);

	public Collection<RegistruCasa> toCollection(); // getAll

	public RegistruCasa add(RegistruCasa entity);

	public Collection<RegistruCasa> addAll(Collection<RegistruCasa> entities);

	public boolean remove(RegistruCasa entity);

	public boolean removeAll(Collection<RegistruCasa> entities);

	public int size();
}
