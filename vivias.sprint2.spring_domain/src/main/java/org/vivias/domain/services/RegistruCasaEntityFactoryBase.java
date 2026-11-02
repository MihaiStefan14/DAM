package org.vivias.domain.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;

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

	// parametru al aplicatiei, folosit la inchiderea zilei din datele seed
	@Autowired
	private PlafoaneNumerar plafoane;

	@Override
	public void setRegistruCasaEntityRepository(IRegistruCasaEntityRepository repository) {
		this.entityRepository = repository;
	}

	public RegistruCasaEntityFactoryBase(IRegistruCasaEntityRepository entityRepository) {
		super();
		this.entityRepository = entityRepository;
	}

	@Override
	public RegistruCasa buildRegistruCasa(LocalDate data, Suma soldDeschidere) {
		Long nextID = this.entityRepository.getNextID();
		return new RegistruCasa(nextID, data, soldDeschidere);
	}

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	@PostConstruct
	@Override
	public void initDomainServiceEntities() {
		logger.info(">> PostConstruct :: initDomainServiceEntities");

		// Ziua de ieri, INCHISA: 800 + 250 (numerar din raport Z) - 50 (avans) = 1000.
		// Cardul (120) apare in raportul Z, dar nu intra in casa.
		RegistruCasa ieri = buildRegistruCasa(LocalDate.now().minusDays(1), lei(800));
		ieri.ataseazaRaportZ(new RaportZ(1, lei(120), lei(250)));
		ieri.inregistreazaOperatiune(TipOperatiune.AVANS, lei(50), "Dispozitie de plata nr. 1");
		ieri.incepeInchiderea();
		ieri.introduceSoldFaptic(lei(1000));
		ieri.inchide(plafoane);
		entityRepository.add(ieri);

		// Ziua de azi, DESCHISA, cu soldul de deschidere = soldul final de ieri.
		// Seed identic cu scenariul Gherkin din User Story 5 (DAM Sprint 1):
		// sold 1000, incasari 500, plati 300 -> sold scriptic 1200
		RegistruCasa azi = buildRegistruCasa(LocalDate.now(), ieri.getSoldFaptic());
		azi.ataseazaRaportZ(new RaportZ(2, lei(300), lei(500)));
		azi.inregistreazaOperatiune(TipOperatiune.PLATA_FURNIZOR, lei(300), "Dispozitie de plata nr. 2", 98L, null);
		entityRepository.add(azi);

		logger.info(">> EntityRepository registru.count :: " + entityRepository.size());
	}
}
