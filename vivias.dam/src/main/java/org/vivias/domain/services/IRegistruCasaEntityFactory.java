package org.vivias.domain.services;

import java.time.LocalDate;

import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.infrastructure.IRegistruCasaRepository;

public interface IRegistruCasaEntityFactory {

	// build registru with: data, soldDeschidere = given, stare = DESCHIS, fara operatiuni;
	// operatiunile (componentele) le creeaza radacina: inregistreazaOperatiune / ataseazaRaportZ
	public RegistruCasa buildRegistruCasa(LocalDate data, Suma soldDeschidere);

	// Dependency: repository-ul in care se salveaza datele initiale
	public void setRegistruCasaRepository(IRegistruCasaRepository repository);

	public void initDomainServiceEntities();
}
