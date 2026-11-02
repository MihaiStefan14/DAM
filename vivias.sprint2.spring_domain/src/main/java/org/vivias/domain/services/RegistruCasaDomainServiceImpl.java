package org.vivias.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vivias.domain.casierie.Incasare;
import org.vivias.domain.casierie.Plata;
import org.vivias.domain.casierie.RegistruCasa;

// SDI Bean Component
@Component("RegistruCasaDomainService")
public class RegistruCasaDomainServiceImpl implements IRegistruCasaDomainService {

	@Override
	public List<Incasare> getIncasari(RegistruCasa registru) {
		return registru.getIncasari();
	}

	@Override
	public List<Plata> getPlati(RegistruCasa registru) {
		return registru.getPlati();
	}

	@Override
	public Integer getNumarOperatiuni(RegistruCasa registru) {
		return registru.getIncasari().size() + registru.getPlati().size();
	}

	@Override
	public Integer getNumarOperatiuni(Integer registruId) {
		RegistruCasa registru = entityRepository.getById(registruId);
		return getNumarOperatiuni(registru);
	}

	// Dependency
	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Override
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository) {
		this.entityRepository = repository;
	}

	public RegistruCasaDomainServiceImpl() {
		super();
	}

	public RegistruCasaDomainServiceImpl(IRegistruCasaEntityRepository entityRepository) {
		super();
		this.entityRepository = entityRepository;
	}

	public IRegistruCasaEntityRepository getEntityRepository() {
		return entityRepository;
	}
}
