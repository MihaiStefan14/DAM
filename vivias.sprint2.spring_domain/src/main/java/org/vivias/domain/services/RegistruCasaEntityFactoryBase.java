package org.vivias.domain.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vivias.domain.casierie.Incasare;
import org.vivias.domain.casierie.Incasare.TipIncasare;
import org.vivias.domain.casierie.Plata;
import org.vivias.domain.casierie.Plata.TipDestinatie;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;

// Aggregate Entity Factory (SDI Bean Component)
@Component("RegistruCasaEntityFactoryBase")
@Scope("singleton")
public class RegistruCasaEntityFactoryBase implements IRegistruCasaEntityFactory {

	private static Logger logger = Logger.getLogger(RegistruCasaEntityFactoryBase.class.getName());

	public RegistruCasaEntityFactoryBase() {
		logger.info(">>> BEAN: RegistruCasaEntityFactoryBase instantiated!");
	}

	// Dependency
	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Override
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository) {
		this.entityRepository = repository;
	}

	public RegistruCasaEntityFactoryBase(IRegistruCasaEntityRepository entityRepository) {
		super();
		this.entityRepository = entityRepository;
	}

	@Override
	public RegistruCasa buildRegistruCasa(LocalDate data, Suma soldInitial) {
		Integer nextID = this.entityRepository.getNextID();
		return new RegistruCasa(nextID, data, soldInitial);
	}

	@PostConstruct
	@Override
	public void initDomainServiceEntities() {
		logger.info(">> PostConstruct :: initDomainServiceEntities");

		// Registru_1: seed data identic cu scenariul Gherkin din User Story 5
		// (Sprint 1): soldInitial 1000, incasari 500, plati 300 -> soldCalculat 1200
		RegistruCasa registru1 = buildRegistruCasa(LocalDate.now(), new Suma(BigDecimal.valueOf(1000)));
		registru1.adaugaIncasare(new Incasare(new Suma(BigDecimal.valueOf(500)), TipIncasare.NUMERAR, "Vanzari ziua curenta"));
		registru1.adaugaPlata(new Plata(new Suma(BigDecimal.valueOf(300)), "Furnizor marfa", TipDestinatie.FURNIZOR));
		entityRepository.add(registru1);

		// Registru_2: fara diferente (soldFaptic = soldCalculat la inchidere)
		RegistruCasa registru2 = buildRegistruCasa(LocalDate.now().minusDays(1), new Suma(BigDecimal.valueOf(800)));
		registru2.adaugaIncasare(new Incasare(new Suma(BigDecimal.valueOf(250)), TipIncasare.CARD, "Vanzari card"));
		registru2.adaugaPlata(new Plata(new Suma(BigDecimal.valueOf(100)), "Avans salariu Ionescu M.", TipDestinatie.AVANS));
		entityRepository.add(registru2);

		logger.info(">> EntityRepository registru.count :: " + entityRepository.size());
	}
}
