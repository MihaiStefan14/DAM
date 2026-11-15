package org.vivias.domain.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vivias.domain.casierie.OperatiuneCasa;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.infrastructure.IRegistruCasaRepository;

// SDI Bean Component
@Component("RegistruCasaDomainService")
public class RegistruCasaDomainServiceImpl implements IRegistruCasaDomainService {

	@Override
	public List<OperatiuneCasa> getOperatii(RegistruCasa registru) {
		return registru.getOperatii();
	}

	@Override
	public List<OperatiuneCasa> getOperatii(RegistruCasa registru, TipOperatiune tip) {
		return registru.getOperatii().stream()
				.filter(o -> o.getTip() == tip)
				.collect(Collectors.toList());
	}

	@Override
	public Integer getNumarOperatiuni(RegistruCasa registru) {
		return registru.getOperatii().size();
	}

	@Override
	public Integer getNumarOperatiuni(Long idRegistru) {
		return entityRepository.findOperatiiByIdRegistru(idRegistru).size();
	}

	// Dependency
	@Autowired
	private IRegistruCasaRepository entityRepository;

	@Override
	public void setRegistruCasaRepository(IRegistruCasaRepository repository) {
		this.entityRepository = repository;
	}

	public RegistruCasaDomainServiceImpl() {
		super();
	}

	public RegistruCasaDomainServiceImpl(IRegistruCasaRepository entityRepository) {
		super();
		this.entityRepository = entityRepository;
	}

	public IRegistruCasaRepository getEntityRepository() {
		return entityRepository;
	}
}
