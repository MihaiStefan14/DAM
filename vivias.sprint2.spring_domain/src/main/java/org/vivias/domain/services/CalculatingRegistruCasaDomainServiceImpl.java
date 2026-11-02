package org.vivias.domain.services;

import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipDiferenta;

/*
 * Naming template: Action.ing Entity-name Service-type
 *
 * Populeaza campurile calculate (@Transient) ale registrului. Formula soldului
 * scriptic sta in radacina (RegistruCasa.calculeazaSoldScriptic), pentru ca o
 * folosesc si regulile de inchidere; aritmetica trece prin VO-ul Suma, deci
 * monedele diferite genereaza exceptie, nu un total gresit.
 */
@Service
public class CalculatingRegistruCasaDomainServiceImpl implements ICalculatingRegistruCasaDomainService {

	@Override
	public RegistruCasa calculeazaSoldScriptic(RegistruCasa registru) {
		registru.setSoldScriptic(registru.calculeazaSoldScriptic());
		return registru;
	}

	@Override
	public RegistruCasa calculeazaDiferenta(RegistruCasa registru) {
		calculeazaSoldScriptic(registru);

		if (registru.getSoldFaptic() == null) {
			registru.setDiferentaValoare(null);
			registru.setDiferentaTip(null);
			return registru;
		}

		Suma diferenta = registru.getSoldFaptic().scade(registru.getSoldScriptic());

		TipDiferenta tip;
		if (diferenta.semn() == 0)
			tip = TipDiferenta.ZERO;
		else if (diferenta.semn() > 0)
			tip = TipDiferenta.PLUS;
		else
			tip = TipDiferenta.MINUS;

		registru.setDiferentaValoare(diferenta.absoluta());
		registru.setDiferentaTip(tip);
		return registru;
	}

	// Inainte de inchidere se estimeaza pe soldul scriptic; dupa, pe cel numarat
	@Override
	public Suma calculeazaPropunereDepunere(RegistruCasa registru, PlafoaneNumerar plafoane) {
		Suma soldCasa = registru.getSoldFaptic() != null ? registru.getSoldFaptic()
				: registru.calculeazaSoldScriptic();
		return plafoane.sumaDeDepus(soldCasa);
	}
}
