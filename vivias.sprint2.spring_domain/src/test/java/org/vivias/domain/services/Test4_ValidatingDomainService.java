package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.spring.AppScanConfig;

/*
 * VALIDATION Service Tests
 *
 * Strategies
 * - Simple Facade
 * - Event-based (Spring ApplicationEvent)
 *
 * Testele construiesc registre noi prin Factory (nu le adauga in repository),
 * ca sa nu strice datele seed folosite de celelalte clase de test, care
 * impart acelasi context Spring. Starile "imposibile" (ex. INCHIS fara raport Z)
 * se obtin prin setStare - ca la o reconstituire corupta din baza de date.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class Test4_ValidatingDomainService {
	private static Logger logger = Logger.getLogger(Test4_ValidatingDomainService.class.getName());

	@Autowired @Qualifier("RegistruCasaEntityFactoryBase")
	private IRegistruCasaEntityFactory entityFactory;

	// Validation Flow built in-place: Validation Service as Simple-Facade
	@Autowired
	private IValidatingRegistruCasaDomainService validatorService;

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	private RegistruCasa registruNou(long soldDeschidere) {
		return entityFactory.buildRegistruCasa(LocalDate.of(2020, 1, 1), lei(soldDeschidere));
	}

	@Test @Order(1)
	public void test1_RegistruValid_FaraViolari() throws Exception {
		RegistruCasa registru = registruNou(100);
		registru.ataseazaRaportZ(new RaportZ(1, lei(50), lei(200)));
		registru.inregistreazaOperatiune(TipOperatiune.PLATA_FURNIZOR, lei(20), "Dispozitie de plata nr. 1");

		assertTrue(validatorService.validate(registru).isEmpty());
		validatorService.validateWithException(registru); // nu arunca
	}

	@Test @Order(2)
	public void test2_ValidatingRegistruCasaDomainService_JSR380() {
		logger.info("Validation Service object :: " + validatorService);

		RegistruCasa registru = registruNou(100);
		registru.setSoldDeschidere(null); // forteaza o violare JSR-380 (@NotNull)

		Set<String> violari = validatorService.validate(registru);
		assertFalse(violari.isEmpty());

		Exception ex = assertThrows(Exception.class, () -> validatorService.validateWithException(registru));
		logger.info("Violari detectate corect: " + ex.getMessage());
	}

	@Test @Order(3)
	public void test3_ValidareAgregat_ValueObjectInvalid() {
		// @Valid pe raportZ: regulile VO-ului fac parte din validarea agregatului
		RegistruCasa registru = registruNou(100);
		registru.ataseazaRaportZ(new RaportZ(0, lei(0), lei(10))); // numarul raportului trebuie sa fie >= 1

		assertFalse(validatorService.validate(registru).isEmpty());
	}

	@Test @Order(4)
	public void test4_RegulaAgregat_InchidereFaraRaportZ() {
		RegistruCasa registru = registruNou(100);
		registru.setSoldFaptic(lei(100));
		registru.setStare(StareRegistru.INCHIS);

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> validatorService.validateRegistruCasaAggregate(registru));
		assertEquals("Lipsește raportul Z", ex.getMessage());

		// validateWithException include si regula de agregat
		assertThrows(Exception.class, () -> validatorService.validateWithException(registru));
	}

	@Test @Order(5)
	public void test5_RegulaAgregat_DiferentaNejustificataLaInchidere() {
		RegistruCasa registru = registruNou(100);
		registru.ataseazaRaportZ(new RaportZ(1, lei(0), lei(50)));
		registru.setSoldFaptic(lei(140)); // scriptic 150 -> diferenta -10, fara nota/aprobare
		registru.setStare(StareRegistru.INCHIS);

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> validatorService.validateRegistruCasaAggregate(registru));
		logger.info("Regula de business a prins corect diferenta nejustificata: " + ex.getMessage());
	}

	// Validation Flow by event-forwarding pattern: ValidatingRegistruCasaDomainService
	// as event-consuming service
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Test @Order(6)
	public void test6_ValidatingRegistruCasaDomainService_EventBased() {
		RegistruCasa registruValid = registruNou(100);
		applicationEventPublisher.publishEvent(new DomainEvent(this, registruValid)); // nu arunca

		RegistruCasa registruInvalid = registruNou(100);
		registruInvalid.setSoldDeschidere(null);

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> applicationEventPublisher.publishEvent(new DomainEvent(this, registruInvalid)));
		logger.info("Validare prin eveniment: " + ex.getMessage());
	}
}
