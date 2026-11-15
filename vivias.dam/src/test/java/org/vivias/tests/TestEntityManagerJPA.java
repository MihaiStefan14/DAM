package org.vivias.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;

import jakarta.persistence.EntityManager;

/*
 * JPA Mapping Tests: EntityManager
 * @Transactional: fiecare test se anuleaza (rollback) la final
 */
@SpringBootTest
@Transactional
public class TestEntityManagerJPA {
	private static Logger logger = Logger.getLogger(TestEntityManagerJPA.class.getName());

	@Autowired
	EntityManager em;

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	private RegistruCasa registru(LocalDate data) {
		RegistruCasa registru = new RegistruCasa(null, data, lei(1000));
		registru.ataseazaRaportZ(new RaportZ(1, lei(300), lei(500)));
		registru.inregistreazaOperatiune(TipOperatiune.PLATA_FURNIZOR, lei(300), "Dispozitie de plata nr. 1", 98L, null);
		return registru;
	}

	@Test
	void testEntityManager() {
		assertNotNull(em);

		// doua registre, fiecare cu operatiunile 1 si 2 (ID-uri locale)
		RegistruCasa r1 = registru(LocalDate.of(2032, 1, 10));
		RegistruCasa r2 = registru(LocalDate.of(2032, 1, 11));
		em.persist(r1);
		em.persist(r2);
		em.flush();
		em.clear();

		assertNotNull(r1.getIdRegistru(), "Id-ul registrului nu a fost generat!");
		assertNotEquals(r1.getIdRegistru(), r2.getIdRegistru());

		// citire din baza de date: componentele, VO-urile si enum-urile se reconstituie
		RegistruCasa citit = em.find(RegistruCasa.class, r1.getIdRegistru());
		logger.info("RegistruCasa citit din DB: " + citit);
		assertEquals(2, citit.getOperatii().size());
		assertEquals(TipOperatiune.INCASARE_NUMERAR, citit.getOperatii().get(0).getTip());
		assertEquals(Long.valueOf(98), citit.getOperatii().get(1).getIdFactura());
		assertEquals(lei(300), citit.getRaportZ().getVanzariCard());
		assertEquals(StareRegistru.DESCHIS, citit.getStare());
		assertEquals(lei(1200), citit.calculeazaSoldScriptic());

		List<RegistruCasa> registre = em
				.createQuery("SELECT r FROM RegistruCasa r WHERE r.data >= :data", RegistruCasa.class)
				.setParameter("data", LocalDate.of(2032, 1, 1)).getResultList();
		assertEquals(2, registre.size());
	}
}
