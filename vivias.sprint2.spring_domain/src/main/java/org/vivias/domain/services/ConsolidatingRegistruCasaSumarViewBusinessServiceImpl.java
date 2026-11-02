package org.vivias.domain.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;

@Service
public class ConsolidatingRegistruCasaSumarViewBusinessServiceImpl
		implements IConsolidatingRegistruCasaSumarViewDomainService {

	// Computation Services for Entity Fields, reused to populate the View
	@Autowired
	private ICalculatingRegistruCasaDomainService calculatingService;

	@Override
	public RegistruCasaSumarView getRegistruCasaSumarViewDataOf(RegistruCasa registru) {
		calculatingService.calculeazaSoldCalculat(registru);
		calculatingService.calculeazaDiferenta(registru);

		return new RegistruCasaSumarView(
				registru.getRegistruId(), registru.getData(), registru.getStare(),
				registru.getSoldInitial(), registru.getSoldCalculat(), registru.getSoldFaptic(),
				registru.getDiferentaValoare(), registru.getDiferentaTip(),
				registru.getIncasari().size(), registru.getPlati().size());
	}

	@Override
	public List<RegistruCasaSumarView> getRegistruCasaSumarViewDataOf(Collection<RegistruCasa> registre) {
		return registre.stream()
				.map(this::getRegistruCasaSumarViewDataOf)
				.collect(Collectors.toList());
	}
}
