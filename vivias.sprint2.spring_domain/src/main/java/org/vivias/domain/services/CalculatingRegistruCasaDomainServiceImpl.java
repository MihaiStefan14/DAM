package org.vivias.domain.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.Suma.Moneda;
import org.vivias.domain.casierie.TipDiferenta;

/*
 * Naming template: Action.ing Entity-name Service-type
 */
@Service
public class CalculatingRegistruCasaDomainServiceImpl implements ICalculatingRegistruCasaDomainService {

	@Override
	public RegistruCasa calculeazaSoldCalculat(RegistruCasa registru) {
		Moneda moneda = registru.getSoldInitial().getMoneda();

		BigDecimal totalIncasari = registru.getIncasari().stream()
				.map(i -> i.getSuma().getValoare())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalPlati = registru.getPlati().stream()
				.map(p -> p.getSuma().getValoare())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal soldCalculatValoare = registru.getSoldInitial().getValoare()
				.add(totalIncasari)
				.subtract(totalPlati);

		registru.setSoldCalculat(new Suma(soldCalculatValoare, moneda));
		return registru;
	}

	@Override
	public RegistruCasa calculeazaDiferenta(RegistruCasa registru) {
		if (registru.getSoldCalculat() == null)
			calculeazaSoldCalculat(registru);

		if (registru.getSoldFaptic() == null) {
			registru.setDiferentaValoare(null);
			registru.setDiferentaTip(null);
			return registru;
		}

		Moneda moneda = registru.getSoldInitial().getMoneda();
		BigDecimal diferentaValoare = registru.getSoldFaptic().getValoare()
				.subtract(registru.getSoldCalculat().getValoare());

		TipDiferenta tip;
		int comparatie = diferentaValoare.compareTo(BigDecimal.ZERO);
		if (comparatie == 0)
			tip = TipDiferenta.ZERO;
		else if (comparatie > 0)
			tip = TipDiferenta.PLUS;
		else
			tip = TipDiferenta.MINUS;

		registru.setDiferentaValoare(new Suma(diferentaValoare.abs(), moneda));
		registru.setDiferentaTip(tip);
		return registru;
	}
}
