package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.Suma.Moneda;
import org.vivias.domain.casierie.TipDiferenta;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.spring.AppScanConfig;

/*
 * COMPUTATION Business Service Tests
 * Strategy: Simple-Facade
 *
 * Scenariul numeric reproduce User Story 5 din DAM Sprint 1
 * ("Inchiderea zilei in registrul de casa"): sold 1000, incasari 500,
 * plati 300 => sold scriptic 1200; sold faptic 1190 => diferenta MINUS 10.
 * Registrele locale sunt construite prin Factory, dar nu sunt adaugate in
 * repository, ca sa nu afecteze celelalte clase de test.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class Test2_ComputingDomainServices {
	private static Logger logger = Logger.getLogger(Test2_ComputingDomainServices.class.getName());

	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Autowired @Qualifier("RegistruCasaEntityFactoryBase")
	private IRegistruCasaEntityFactory entityFactory;

	@Autowired
	private ICalculatingRegistruCasaDomainService calculatingService;

	@Autowired
	private IConsolidatingRegistruCasaSumarViewDomainService consolidatingService;

	@Autowired
	private PlafoaneNumerar plafoane;

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	private RegistruCasa registruLocal(long soldDeschidere, long vanzariCard, long vanzariNumerar) {
		RegistruCasa registru = entityFactory.buildRegistruCasa(LocalDate.of(2020, 1, 1), lei(soldDeschidere));
		registru.ataseazaRaportZ(new RaportZ(1, lei(vanzariCard), lei(vanzariNumerar)));
		return registru;
	}

	@Test @Order(1)
	public void test1_CalculatingRegistruCasaDomainService_UserStory5() {
		RegistruCasa registru = registruLocal(1000, 0, 500);
		registru.inregistreazaOperatiune(TipOperatiune.PLATA_FURNIZOR, lei(300), "Dispozitie de plata nr. 1");
		registru.incepeInchiderea();
		registru.introduceSoldFaptic(lei(1190));

		calculatingService.calculeazaDiferenta(registru);
		logger.info("Computed registru: soldScriptic=" + registru.getSoldScriptic()
				+ " diferenta=" + registru.getDiferentaTip() + " " + registru.getDiferentaValoare());

		assertEquals(lei(1200), registru.getSoldScriptic());
		assertEquals(lei(10), registru.getDiferentaValoare());
		assertEquals(TipDiferenta.MINUS, registru.getDiferentaTip());
	}

	@Test @Order(2)
	public void test2_ConsolidatingRegistruCasaSumarViewDomainService() {
		// seed-ul zilei de azi (RegistruCasaEntityFactoryBase): 1000 + 500 numerar - 300 plata; card 300
		RegistruCasa azi = entityRepository.getByData(LocalDate.now());
		assertNotNull(azi);

		RegistruCasaSumarView view = consolidatingService.getRegistruCasaSumarViewDataOf(azi);
		logger.info("Computed RegistruCasaSumarView: " + view);
		assertEquals(lei(1000), view.getSoldDeschidere());
		assertEquals(lei(1200), view.getSoldScriptic());
		assertEquals(lei(300), view.getVanzariCard());
		assertEquals(2, view.getNumarOperatiuni()); // incasarea din raportul Z + plata
		assertEquals(lei(0), view.getPropunereDepunere()); // 1200 < plafonul intern de 2000

		List<RegistruCasaSumarView> allViews = consolidatingService
				.getRegistruCasaSumarViewDataOf(entityRepository.toCollection());
		allViews.forEach(v -> logger.info("Computed RegistruCasaSumarView:: " + v));
		assertEquals(entityRepository.size(), allViews.size());
	}

	@Test @Order(3)
	public void test3_CardulNuIntraInSoldulCasei() {
		// banii pe card merg la banca (IncasareCard, "In decontare"), nu in sertar
		RegistruCasa registru = registruLocal(100, 500, 0);
		calculatingService.calculeazaSoldScriptic(registru);
		assertEquals(lei(100), registru.getSoldScriptic());
	}

	@Test @Order(4)
	public void test4_PropunereDepunere_PestePlafonulIntern() {
		// APOO CU5, scenariul "Soldul peste plafon": 400 + 2100 = 2500 -> 500 de depus
		RegistruCasa registru = registruLocal(400, 0, 2100);
		assertEquals(lei(500), calculatingService.calculeazaPropunereDepunere(registru, plafoane));
	}

	@Test @Order(5)
	public void test5_MonedeDiferite_SuntRefuzate() {
		RegistruCasa registru = registruLocal(100, 0, 0);
		registru.setSoldFaptic(new Suma(BigDecimal.valueOf(100), Moneda.EUR));

		assertThrows(IllegalArgumentException.class, () -> calculatingService.calculeazaDiferenta(registru));
	}
}
