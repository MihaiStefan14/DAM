package org.vivias.domain.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;

@Service
public class ConsolidatingRegistruCasaSumarViewBusinessServiceImpl
		implements IConsolidatingRegistruCasaSumarViewDomainService {

	// Computation Services for Entity Fields, reused to populate the View
	@Autowired
	private ICalculatingRegistruCasaDomainService calculatingService;

	// parametru al aplicatiei (AppScanConfig)
	@Autowired
	private PlafoaneNumerar plafoane;

	@Override
	public RegistruCasaSumarView getRegistruCasaSumarViewDataOf(RegistruCasa registru) {
		calculatingService.calculeazaDiferenta(registru);
		RaportZ raportZ = registru.getRaportZ();

		return new RegistruCasaSumarView(
				registru.getIdRegistru(), registru.getData(), registru.getStare(),
				registru.getSoldDeschidere(), registru.getSoldScriptic(), registru.getSoldFaptic(),
				registru.getDiferentaValoare(), registru.getDiferentaTip(),
				raportZ == null ? null : raportZ.getVanzariCard(),
				raportZ == null ? null : raportZ.getVanzariNumerar(),
				registru.getOperatii().size(),
				calculatingService.calculeazaPropunereDepunere(registru, plafoane));
	}

	@Override
	public List<RegistruCasaSumarView> getRegistruCasaSumarViewDataOf(Collection<RegistruCasa> registre) {
		return registre.stream()
				.map(this::getRegistruCasaSumarViewDataOf)
				.collect(Collectors.toList());
	}
}
