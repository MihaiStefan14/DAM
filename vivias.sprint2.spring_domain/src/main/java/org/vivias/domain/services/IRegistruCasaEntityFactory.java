package org.vivias.domain.services;

import java.time.LocalDate;

import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;

public interface IRegistruCasaEntityFactory {

	// build registru with: data=today, soldInitial = given, stare = DESCHIS
	public RegistruCasa buildRegistruCasa(LocalDate data, Suma soldInitial);

	// Dependencies to get IDs
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository);

	public void initDomainServiceEntities();
}
