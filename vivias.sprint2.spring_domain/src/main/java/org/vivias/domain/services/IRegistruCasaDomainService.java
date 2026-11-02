package org.vivias.domain.services;

import java.util.List;

import org.vivias.domain.casierie.OperatiuneCasa;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.TipOperatiune;

public interface IRegistruCasaDomainService {
	public List<OperatiuneCasa> getOperatii(RegistruCasa registru);

	public List<OperatiuneCasa> getOperatii(RegistruCasa registru, TipOperatiune tip);

	public Integer getNumarOperatiuni(RegistruCasa registru);

	public Integer getNumarOperatiuni(Long idRegistru);

	// Dependencies to get IDs
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository);
}
