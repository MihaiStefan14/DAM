package org.vivias.domain.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;

// SDI Bean Component
@Component
@Scope("singleton")
public class RegistruCasaEntityRepositoryBase implements IRegistruCasaEntityRepository {

	private static Logger logger = Logger.getLogger(RegistruCasaEntityRepositoryBase.class.getName());

	public RegistruCasaEntityRepositoryBase() {
		logger.info(">>> BEAN: RegistruCasaEntityRepositoryBase instantiated!");
	}

	private Map<Long, RegistruCasa> entities = new HashMap<>();
	private Long nextID = 0L;

	@Override
	public Long getNextID() {
		nextID++;
		return nextID;
	}

	@Override
	public RegistruCasa getById(Long id) {
		return entities.get(id);
	}

	@Override
	public RegistruCasa get(RegistruCasa sample) {
		return getById(sample.getIdRegistru());
	}

	@Override
	public Collection<RegistruCasa> toCollection() {
		List<RegistruCasa> list = new ArrayList<>();
		list.addAll(this.entities.values());
		return list;
	}

	@Override
	public RegistruCasa getByData(LocalDate data) {
		return entities.values().stream()
				.filter(r -> data.equals(r.getData()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public RegistruCasa getUltimulInchisInainte(LocalDate data) {
		return entities.values().stream()
				.filter(r -> r.getStare() == StareRegistru.INCHIS && r.getData().isBefore(data))
				.max(Comparator.comparing(RegistruCasa::getData))
				.orElse(null);
	}

	@Override
	public RegistruCasa add(RegistruCasa entity) {
		if (entity.getIdRegistru() == null) {
			nextID++;
			entity.setIdRegistru(nextID);
		}
		entities.put(entity.getIdRegistru(), entity);
		return entity;
	}

	@Override
	public Collection<RegistruCasa> addAll(Collection<RegistruCasa> entities) {
		for (RegistruCasa entity : entities)
			this.add(entity);
		return entities;
	}

	@Override
	public boolean remove(RegistruCasa entity) {
		if (this.entities.containsValue(entity)) {
			this.entities.remove(entity.getIdRegistru());
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<RegistruCasa> entities) {
		boolean flag = true;
		for (RegistruCasa entity : entities)
			if (!this.remove(entity))
				flag = false;
		return flag;
	}

	@Override
	public int size() {
		return this.entities.values().size();
	}
}
