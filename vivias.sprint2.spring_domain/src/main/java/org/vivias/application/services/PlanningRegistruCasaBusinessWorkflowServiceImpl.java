package org.vivias.application.services;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.Incasare;
import org.vivias.domain.casierie.Incasare.TipIncasare;
import org.vivias.domain.casierie.Plata;
import org.vivias.domain.casierie.Plata.TipDestinatie;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.services.ICalculatingRegistruCasaDomainService;
import org.vivias.domain.services.IConsolidatingRegistruCasaSumarViewDomainService;
import org.vivias.domain.services.IRegistruCasaEntityFactory;
import org.vivias.domain.services.IRegistruCasaEntityRepository;

@Service("PlanningRegistruCasaBusinessWorkflowServiceFacade")
public class PlanningRegistruCasaBusinessWorkflowServiceImpl implements IPlanningRegistruCasaBusinessWorkflowService {

	// Support Services
	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Autowired
	@Qualifier("RegistruCasaEntityFactoryBase")
	private IRegistruCasaEntityFactory entityFactory;

	@Autowired
	private ICalculatingRegistruCasaDomainService calculatingService;

	@Autowired
	private IConsolidatingRegistruCasaSumarViewDomainService consolidatingService;

	// (1) Deschide registrul de casa al zilei
	@Override
	public Integer deschideRegistru(LocalDate data, Suma soldInitial) {
		RegistruCasa registru = entityFactory.buildRegistruCasa(data, soldInitial);
		entityRepository.add(registru);
		return registru.getRegistruId();
	}

	// (2) Inregistreaza o incasare
	@Override
	public Integer inregistreazaIncasare(Integer registruId, Suma suma, TipIncasare tip, String observatii) {
		RegistruCasa registru = entityRepository.getById(registruId);
		Incasare incasare = new Incasare(suma, tip, observatii);
		registru.adaugaIncasare(incasare);
		return incasare.getIncasareId();
	}

	// (3) Inregistreaza o plata
	@Override
	public Integer inregistreazaPlata(Integer registruId, Suma suma, String destinatie, TipDestinatie tipDestinatie) {
		RegistruCasa registru = entityRepository.getById(registruId);
		Plata plata = new Plata(suma, destinatie, tipDestinatie);
		registru.adaugaPlata(plata);
		return plata.getPlataId();
	}

	// (4) Inchide ziua: calculeaza soldCalculat si diferenta (User Story 5)
	@Override
	public StareRegistru inchideZi(Integer registruId, Suma soldFaptic) {
		RegistruCasa registru = entityRepository.getById(registruId);
		registru.setSoldFaptic(soldFaptic);

		calculatingService.calculeazaSoldCalculat(registru);
		calculatingService.calculeazaDiferenta(registru);

		boolean faraDiferenta = registru.getDiferentaTip() == null
				|| registru.getDiferentaTip() == org.vivias.domain.casierie.TipDiferenta.ZERO;

		registru.setStare(faraDiferenta ? StareRegistru.INCHIS : StareRegistru.CU_DIFERENTE);
		return registru.getStare();
	}

	// (5) Administratorul aproba regularizarea diferentei si inchide ziua
	@Override
	public StareRegistru aprobaRegularizareSiFinalizeaza(Integer registruId) {
		RegistruCasa registru = entityRepository.getById(registruId);
		if (registru.getStare() == StareRegistru.CU_DIFERENTE)
			registru.setStare(StareRegistru.REGULARIZAT);
		registru.setStare(StareRegistru.INCHIS);
		return registru.getStare();
	}

	// (6) Sumarul registrului
	@Override
	public RegistruCasaSumarView getRegistruSumar(Integer registruId) {
		RegistruCasa registru = entityRepository.getById(registruId);
		return consolidatingService.getRegistruCasaSumarViewDataOf(registru);
	}
}
