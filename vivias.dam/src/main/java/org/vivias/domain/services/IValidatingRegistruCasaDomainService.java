package org.vivias.domain.services;

import java.util.Set;

import org.vivias.domain.casierie.RegistruCasa;

public interface IValidatingRegistruCasaDomainService {
	Set<String> validate(RegistruCasa registru);

	void validateWithException(RegistruCasa registru) throws Exception;

	boolean validateRegistruCasaAggregate(RegistruCasa registru);
}
