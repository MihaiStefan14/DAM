package org.vivias.domain.services;

import java.util.List;

import org.vivias.domain.casierie.Incasare;
import org.vivias.domain.casierie.Plata;
import org.vivias.domain.casierie.RegistruCasa;

public interface IRegistruCasaDomainService {
	public List<Incasare> getIncasari(RegistruCasa registru);

	public List<Plata> getPlati(RegistruCasa registru);

	public Integer getNumarOperatiuni(RegistruCasa registru);

	public Integer getNumarOperatiuni(Integer registruId);

	// Dependencies to get IDs
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository);
}
