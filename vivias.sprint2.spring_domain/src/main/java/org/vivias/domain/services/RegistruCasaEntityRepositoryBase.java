package org.vivias.domain.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vivias.domain.casierie.RegistruCasa;

// SDI Bean Component
@Component
@Scope("singleton")
public class RegistruCasaEntityRepositoryBase implements IRegistruCasaEntityRepository {

	private static Logger logger = Logger.getLogger(RegistruCasaEntityRepositoryBase.class.getName());

	public RegistruCasaEntityRepositoryBase() {
		logger.info(">>> BEAN: RegistruCasaEntityRepositoryBase instantiated!");
	}

	private Map<Integer, RegistruCasa> entities = new HashMap<>();
	private Integer nextID = 0;

	@Override
	public Integer getNextID() {
		nextID++;
		return nextID;
	}

	@Override
	public RegistruCasa getById(Integer id) {
		return entities.get(id);
	}

	@Override
	public RegistruCasa get(RegistruCasa sample) {
		return getById(sample.getRegistruId());
	}

	@Override
	public Collection<RegistruCasa> toCollection() {
		List<RegistruCasa> list = new ArrayList<>();
		list.addAll(this.entities.values());
		return list;
	}

	@Override
	public RegistruCasa add(RegistruCasa entity) {
		if (entity.getRegistruId() == null) {
			nextID++;
			entity.setRegistruId(nextID);
		}
		entities.put(entity.getRegistruId(), entity);
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
			this.entities.remove(entity.getRegistruId());
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
