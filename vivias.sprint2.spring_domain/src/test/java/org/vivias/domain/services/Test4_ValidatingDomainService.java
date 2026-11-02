package org.vivias.domain.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.spring.AppScanConfig;

/*
 * VALIDATION Service Tests
 *
 * Strategies
 * - Simple Facade
 * - Event-based (Spring ApplicationEvent)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class Test4_ValidatingDomainService {
	private static Logger logger = Logger.getLogger(Test4_ValidatingDomainService.class.getName());

	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	// Validation Flow built in-place: Validation Service as Simple-Facade
	@Autowired
	private IValidatingRegistruCasaDomainService validatorService;

	@Test @Order(1)
	public void test1_ValidatingRegistruCasaDomainService() {
		logger.info("Validation Service object :: " + validatorService);

		RegistruCasa registru = entityRepository.getById(2);
		registru.setSoldInitial(null); // forteaza o violare JSR-380 (@NotNull)

		try {
			validatorService.validateWithException(registru);
		} catch (Exception ex) {
			logger.severe(ex.getMessage());
		}
	}

	@Test @Order(2)
	public void test2_BusinessRuleValidation_SoldFapticObligatoriuLaInchidere() {
		RegistruCasa registru = new RegistruCasa(99, LocalDate.now(), new Suma(BigDecimal.valueOf(100)));
		registru.setStare(org.vivias.domain.casierie.StareRegistru.INCHIS); // fara soldFaptic -> invalid

		boolean exceptieAruncata = false;
		try {
			validatorService.validateRegistruCasaAggregate(registru);
		} catch (RuntimeException ex) {
			exceptieAruncata = true;
			logger.info("Regula de business a prins corect lipsa soldFaptic: " + ex.getMessage());
		}
		org.junit.jupiter.api.Assertions.assertTrue(exceptieAruncata);
	}

	// Validation Flow by event-forwarding pattern: ValidatingRegistruCasaDomainService
	// as event-consuming service
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Test @Order(3)
	public void test3_ValidatingRegistruCasaDomainService_EventBased() {
		RegistruCasa registru = entityRepository.getById(1);
		registru.setSoldInitial(null);

		try {
			applicationEventPublisher.publishEvent(new DomainEvent(this, registru));
		} catch (Exception ex) {
			logger.severe(ex.getMessage());
		}
	}
}
