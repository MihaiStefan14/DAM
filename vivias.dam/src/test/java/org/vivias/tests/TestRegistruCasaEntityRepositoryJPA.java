package org.vivias.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.services.IRegistruCasaEntityFactory;
import org.vivias.infrastructure.IRegistruCasaEntityRepository;

/*
 * (Bonus) Complete JPA Repository Tests: IRegistruCasaEntityRepository
 * implementat cu EntityManager (RegistruCasaEntityRepositoryJPA)
 * @Transactional: fiecare test se anuleaza (rollback) la final
 */
@SpringBootTest
@Transactional
public class TestRegistruCasaEntityRepositoryJPA {
	private static Logger logger = Logger.getLogger(TestRegistruCasaEntityRepositoryJPA.class.getName());

	@Autowired @Qualifier("RegistruCasaEntityFactoryBase")
	private IRegistruCasaEntityFactory entityFactory;

	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Autowired
	private PlafoaneNumerar plafoane;

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	private RegistruCasa registru(LocalDate data, long soldFaptic) {
		RegistruCasa registru = entityFactory.buildRegistruCasa(data, lei(1000));
		registru.ataseazaRaportZ(new RaportZ(1, lei(300), lei(500)));
		if (soldFaptic > 0) {
			registru.incepeInchiderea();
			registru.introduceSoldFaptic(lei(soldFaptic));
			registru.inchide(plafoane);
		}
		return registru;
	}

	@Test
	public void test1_AddGetSize() {
		assertNotNull(entityRepository, "Repository implementation object: failed initialization!");
		int sizeInitial = entityRepository.size();

		RegistruCasa registru = entityRepository.add(registru(LocalDate.of(2034, 1, 10), 0));
		entityRepository.addAll(List.of(registru(LocalDate.of(2034, 1, 11), 0), registru(LocalDate.of(2034, 1, 12), 0)));
		logger.info("Added registru: " + registru);

		assertNotNull(registru.getIdRegistru());
		assertEquals(sizeInitial + 3, entityRepository.size());
		assertEquals(sizeInitial + 3, entityRepository.toCollection().size());
		assertEquals(LocalDate.of(2034, 1, 10), entityRepository.getById(registru.getIdRegistru()).getData());
		assertEquals(registru, entityRepository.get(registru));
	}

	@Test
	public void test2_GetByDataSiUltimulInchis() {
		entityRepository.add(registru(LocalDate.of(2034, 2, 10), 1500));
		entityRepository.add(registru(LocalDate.of(2034, 2, 11), 1500));
		entityRepository.add(registru(LocalDate.of(2034, 2, 12), 0));

		assertNotNull(entityRepository.getByData(LocalDate.of(2034, 2, 12)));
		assertNull(entityRepository.getByData(LocalDate.of(2034, 2, 13)));

		// ziua de 12 e deschisa: ultima zi inchisa dinainte de 13 este 11
		RegistruCasa ultimul = entityRepository.getUltimulInchisInainte(LocalDate.of(2034, 2, 13));
		logger.info(">>>> entityRepository.getUltimulInchisInainte >>> " + ultimul);
		assertEquals(LocalDate.of(2034, 2, 11), ultimul.getData());
		assertEquals(lei(1500), ultimul.getSoldFaptic());
	}

	@Test
	public void test3_Remove() {
		RegistruCasa registru = entityRepository.add(registru(LocalDate.of(2034, 3, 10), 0));
		int size = entityRepository.size();

		assertTrue(entityRepository.remove(registru));
		assertEquals(size - 1, entityRepository.size());
		assertNull(entityRepository.getById(registru.getIdRegistru()));
		assertFalse(entityRepository.remove(registru), "Registrul a fost deja sters!");
	}
}
