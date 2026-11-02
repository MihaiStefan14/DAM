package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.Suma;
import org.vivias.spring.AppScanConfig;

/*
 * COMPUTATION Business Service Tests
 * Strategy: Simple-Facade
 *
 * Scenariul numeric reproduce exact User Story 5 din Sprint 1
 * ("Inchiderea zilei in registrul de casa"): soldInitial=1000,
 * incasari=500, plati=300 => soldCalculat=1200; soldFaptic=1190
 * => diferenta MINUS 10.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class Test2_ComputingDomainServices {
	private static Logger logger = Logger.getLogger(Test2_ComputingDomainServices.class.getName());

	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Autowired
	private ICalculatingRegistruCasaDomainService calculatingService;

	@Autowired
	private IConsolidatingRegistruCasaSumarViewDomainService consolidatingService;

	@Test @Order(1)
	public void test1_CalculatingRegistruCasaDomainService() {
		RegistruCasa registru = entityRepository.getById(1);
		assertNotNull(registru);

		registru.setSoldFaptic(new Suma(BigDecimal.valueOf(1190)));

		calculatingService.calculeazaSoldCalculat(registru);
		calculatingService.calculeazaDiferenta(registru);

		logger.info("Computed registru: soldCalculat=" + registru.getSoldCalculat()
				+ " diferenta=" + registru.getDiferentaTip() + " " + registru.getDiferentaValoare());

		assertEquals(0, registru.getSoldCalculat().getValoare().compareTo(BigDecimal.valueOf(1200)));
		assertEquals(0, registru.getDiferentaValoare().getValoare().compareTo(BigDecimal.valueOf(10)));
	}

	@Test @Order(2)
	public void test2_ConsolidatingRegistruCasaSumarViewDomainService() {
		RegistruCasaSumarView view = consolidatingService.getRegistruCasaSumarViewDataOf(entityRepository.getById(1));
		logger.info("Computed RegistruCasaSumarView: " + view);
		assertNotNull(view.getSoldCalculat());

		List<RegistruCasaSumarView> allViews = consolidatingService
				.getRegistruCasaSumarViewDataOf(entityRepository.toCollection());
		allViews.forEach(v -> logger.info("Computed RegistruCasaSumarView:: " + v));
	}
}
