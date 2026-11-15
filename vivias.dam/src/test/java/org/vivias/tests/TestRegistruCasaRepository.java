package org.vivias.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.vivias.domain.casierie.OperatiuneCasa;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.domain.services.IRegistruCasaEntityFactory;
import org.vivias.infrastructure.IRegistruCasaRepository;

/*
 * Spring Data JPA Repository Tests
 * @Transactional: fiecare test se anuleaza (rollback) la final, datele seed raman neschimbate
 */
@SpringBootTest
@Transactional
public class TestRegistruCasaRepository {
	private static Logger logger = Logger.getLogger(TestRegistruCasaRepository.class.getName());

	@Autowired @Qualifier("RegistruCasaEntityFactoryBase")
	private IRegistruCasaEntityFactory entityFactory;

	// Spring Data JPA declared Repository
	@Autowired
	private IRegistruCasaRepository entityRepository;

	@Autowired
	private PlafoaneNumerar plafoane;

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	// registru cu raport Z (numerar 500) + o plata de 300: sold scriptic 1000 + 500 - 300 = 1200
	private RegistruCasa registru(LocalDate data, boolean inchis) {
		RegistruCasa registru = entityFactory.buildRegistruCasa(data, lei(1000));
		registru.ataseazaRaportZ(new RaportZ(1, lei(300), lei(500)));
		registru.inregistreazaOperatiune(TipOperatiune.PLATA_FURNIZOR, lei(300), "Dispozitie de plata nr. 1", 98L, null);
		if (inchis) {
			registru.incepeInchiderea();
			registru.introduceSoldFaptic(lei(1200));
			registru.inchide(plafoane);
		}
		return entityRepository.save(registru);
	}

	@Test
	public void test1_SaveCountFindById() {
		assertNotNull(entityRepository, "Repository implementation object: failed initialization!");
		long countInitial = entityRepository.count();

		RegistruCasa salvat = registru(LocalDate.of(2033, 1, 10), false);
		registru(LocalDate.of(2033, 1, 11), false);
		registru(LocalDate.of(2033, 1, 12), false);
		logger.info("Saved registru: " + salvat);

		assertNotNull(salvat.getIdRegistru(), "Registrul nu a fost salvat!");
		assertEquals(countInitial + 3, entityRepository.count());

		RegistruCasa citit = entityRepository.findById(salvat.getIdRegistru()).orElseThrow();
		assertEquals(2, citit.getOperatii().size());
		assertEquals(lei(1200), citit.calculeazaSoldScriptic());
	}

	@Test
	public void test2_QueryableNamedOperations() {
		registru(LocalDate.of(2033, 2, 10), false);
		registru(LocalDate.of(2033, 2, 11), true);

		RegistruCasa rezultat = entityRepository.findByData(LocalDate.of(2033, 2, 10));
		logger.info(">>>> entityRepository.findByData >>> " + rezultat);
		assertNotNull(rezultat);
		assertEquals(StareRegistru.DESCHIS, rezultat.getStare());
		assertNull(entityRepository.findByData(LocalDate.of(2033, 2, 9)), "Ziua nu are registru!");

		List<RegistruCasa> inchise = entityRepository.findByStare(StareRegistru.INCHIS);
		logger.info(">>>> entityRepository.findByStare(INCHIS) >>> " + inchise.size());
		assertTrue(inchise.stream().anyMatch(r -> r.getData().equals(LocalDate.of(2033, 2, 11))));
		assertTrue(inchise.stream().allMatch(r -> r.getStare() == StareRegistru.INCHIS));
	}

	@Test
	public void test3_QueryableAnnotatedOperations() {
		RegistruCasa deschis = registru(LocalDate.of(2033, 3, 10), false);
		registru(LocalDate.of(2033, 3, 11), true);
		registru(LocalDate.of(2033, 3, 12), true);

		// cea mai recenta zi inchisa dinaintea datei: soldul ei devine soldul de deschidere
		List<RegistruCasa> inchise = entityRepository.findInchiseInainte(LocalDate.of(2033, 3, 13));
		logger.info(">>>> entityRepository.findInchiseInainte >>> " + inchise.size());
		assertEquals(LocalDate.of(2033, 3, 12), inchise.get(0).getData());
		assertEquals(LocalDate.of(2033, 3, 11), inchise.get(1).getData());

		List<OperatiuneCasa> operatii = entityRepository.findOperatiiByIdRegistru(deschis.getIdRegistru());
		logger.info(">>>> entityRepository.findOperatiiByIdRegistru >>> " + operatii);
		assertEquals(2, operatii.size());
		assertEquals(TipOperatiune.INCASARE_NUMERAR, operatii.get(0).getTip());
		assertEquals(TipOperatiune.PLATA_FURNIZOR, operatii.get(1).getTip());
	}

	@Test
	public void test4_DeleteCascadeOperatii() {
		RegistruCasa registru = registru(LocalDate.of(2033, 4, 10), false);
		Long idRegistru = registru.getIdRegistru();

		// cascade = ALL + orphanRemoval: operatiunile se sterg odata cu registrul
		entityRepository.deleteById(idRegistru);
		entityRepository.flush();

		assertTrue(entityRepository.findById(idRegistru).isEmpty());
		assertTrue(entityRepository.findOperatiiByIdRegistru(idRegistru).isEmpty());
	}
}
