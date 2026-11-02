package org.vivias.domain.services;

import org.vivias.domain.casierie.RegistruCasa;

/**
 * Computation Rules: Domain Computation Services for Entity Fields.
 * Implementeaza regula de business din User Story 5 (Sprint 1):
 * soldCalculat = soldInitial + totalIncasari - totalPlati
 * diferenta = soldFaptic - soldCalculat (PLUS/MINUS/ZERO)
 */
public interface ICalculatingRegistruCasaDomainService {

	RegistruCasa calculeazaSoldCalculat(RegistruCasa registru);

	RegistruCasa calculeazaDiferenta(RegistruCasa registru);
}
