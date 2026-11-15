package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;

import jakarta.persistence.EntityManager;

// Complete JPA Repository: EntityManager injection
@Repository
@Transactional
public class RegistruCasaEntityRepositoryJPA implements IRegistruCasaEntityRepository {

	private static Logger logger = Logger.getLogger(RegistruCasaEntityRepositoryJPA.class.getName());

	public RegistruCasaEntityRepositoryJPA() {
		logger.info(">>> BEAN: RegistruCasaEntityRepositoryJPA instantiated!");
	}

	@Autowired
	private EntityManager em;

	@Override
	public RegistruCasa getById(Long id) {
		return em.find(RegistruCasa.class, id);
	}

	@Override
	public RegistruCasa get(RegistruCasa sample) {
		return getById(sample.getIdRegistru());
	}

	@Override
	public Collection<RegistruCasa> toCollection() {
		return em.createQuery("SELECT r FROM RegistruCasa r ORDER BY r.data", RegistruCasa.class).getResultList();
	}

	@Override
	public RegistruCasa getByData(LocalDate data) {
		List<RegistruCasa> rezultat = em
				.createQuery("SELECT r FROM RegistruCasa r WHERE r.data = :data", RegistruCasa.class)
				.setParameter("data", data).getResultList();
		return rezultat.isEmpty() ? null : rezultat.get(0);
	}

	@Override
	public RegistruCasa getUltimulInchisInainte(LocalDate data) {
		List<RegistruCasa> rezultat = em
				.createQuery("SELECT r FROM RegistruCasa r WHERE r.stare = :stare AND r.data < :data "
						+ "ORDER BY r.data DESC", RegistruCasa.class)
				.setParameter("stare", StareRegistru.INCHIS).setParameter("data", data).setMaxResults(1)
				.getResultList();
		return rezultat.isEmpty() ? null : rezultat.get(0);
	}

	@Override
	public RegistruCasa add(RegistruCasa entity) {
		if (entity.getIdRegistru() == null) {
			em.persist(entity);
			return entity;
		}
		return em.merge(entity);
	}

	@Override
	public Collection<RegistruCasa> addAll(Collection<RegistruCasa> entities) {
		List<RegistruCasa> salvate = new ArrayList<>();
		for (RegistruCasa entity : entities)
			salvate.add(this.add(entity));
		return salvate;
	}

	@Override
	public boolean remove(RegistruCasa entity) {
		RegistruCasa existent = get(entity);
		if (existent == null)
			return false;
		em.remove(existent);
		return true;
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
		return em.createQuery("SELECT COUNT(r) FROM RegistruCasa r", Long.class).getSingleResult().intValue();
	}
}
