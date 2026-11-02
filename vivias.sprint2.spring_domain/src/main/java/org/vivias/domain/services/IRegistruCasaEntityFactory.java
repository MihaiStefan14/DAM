package org.vivias.domain.services;

import java.time.LocalDate;

import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;

public interface IRegistruCasaEntityFactory {

	// build registru with: data, soldDeschidere = given, stare = DESCHIS, fara operatiuni;
	// operatiunile (componentele) le creeaza radacina: inregistreazaOperatiune / ataseazaRaportZ
	public RegistruCasa buildRegistruCasa(LocalDate data, Suma soldDeschidere);

	// Dependencies to get IDs
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository);

	public void initDomainServiceEntities();
}
