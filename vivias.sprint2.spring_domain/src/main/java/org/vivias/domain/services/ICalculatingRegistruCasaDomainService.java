package org.vivias.domain.services;

import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;

/**
 * Computation Rules: Domain Computation Services for Entity Fields.
 * Regulile de calcul ale inchiderii zilei (APOO CU5; DAM Sprint 1, User Story 5):
 * soldScriptic = soldDeschidere + intrari (incasari numerar, restituiri) - iesiri (plati, depuneri, avansuri)
 * diferenta = soldFaptic - soldScriptic (PLUS/MINUS/ZERO)
 * propunereDepunere = ce trece din sold peste plafonul intern de casa
 */
public interface ICalculatingRegistruCasaDomainService {

	RegistruCasa calculeazaSoldScriptic(RegistruCasa registru);

	RegistruCasa calculeazaDiferenta(RegistruCasa registru);

	Suma calculeazaPropunereDepunere(RegistruCasa registru, PlafoaneNumerar plafoane);
}
