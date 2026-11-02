package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.application.services.IPlanningRegistruCasaBusinessWorkflowService;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.Suma.Moneda;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.domain.casierie.ZiCasaInchisaEvent;
import org.vivias.spring.AppScanConfig;

/*
 * WORKFLOW Business Service Tests
 * Strategy: Simple-Facade
 *
 * test1..test4 reproduc scenariile Gherkin din APOO, CU5 - inchide_ziua_de_casa.feature
 * (Background: sold de deschidere 400 lei, plafon intern 2000 lei). Fiecare scenariu
 * are propria zi, pentru ca exista un singur registru pe zi; folosim noiembrie 2030
 * ca sa nu se suprapuna cu datele seed (ieri/azi).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
public class Test3_BusinessWorkflowService {
	private static Logger logger = Logger.getLogger(Test3_BusinessWorkflowService.class.getName());

	@Autowired @Qualifier("PlanningRegistruCasaBusinessWorkflowServiceFacade")
	private IPlanningRegistruCasaBusinessWorkflowService workflowService;

	@Autowired
	private ConfigurableApplicationContext context;

	private final List<ZiCasaInchisaEvent> zileInchise = new ArrayList<>();

	@BeforeEach
	public void ascultaZiCasaInchisaEvent() {
		// evenimentul e un POJO publicat ca payload; clasa anonima pastreaza tipul generic
		context.addApplicationListener(new ApplicationListener<PayloadApplicationEvent<ZiCasaInchisaEvent>>() {
			@Override
			public void onApplicationEvent(PayloadApplicationEvent<ZiCasaInchisaEvent> event) {
				zileInchise.add(event.getPayload());
			}
		});
	}

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	// Background + "Given raportul Z al zilei este importat"
	private Long registruCuRaportZ(LocalDate data, long numerar, long plati) {
		Long idRegistru = workflowService.deschideRegistruInitial(data, lei(400));
		workflowService.ataseazaRaportZ(idRegistru, 1, lei(300), lei(numerar));
		if (plati > 0)
			workflowService.inregistreazaOperatiune(idRegistru, TipOperatiune.PLATA_FURNIZOR, lei(plati),
					"Dispozitie de plata nr. 7");
		return idRegistru;
	}

	@Test
	public void test1_InchidereFaraDiferente() {
		Long idRegistru = registruCuRaportZ(LocalDate.of(2030, 11, 3), 650, 50);

		assertEquals(StareRegistru.INCHIS, workflowService.inchideZi(idRegistru, lei(1000)));

		// soldul final devine soldul de deschidere al zilei urmatoare
		Long maine = workflowService.deschideRegistru(LocalDate.of(2030, 11, 4));
		assertEquals(lei(1000), workflowService.getRegistruSumar(maine).getSoldDeschidere());

		assertEquals(1, zileInchise.size());
		ZiCasaInchisaEvent event = zileInchise.get(0);
		logger.info("Publicat: " + event);
		assertEquals(LocalDate.of(2030, 11, 3), event.getData());
		assertEquals(lei(1000), event.getSoldCasa());
		assertEquals(lei(650), event.getVanzariNumerar());
		assertEquals(lei(300), event.getVanzariCard());
	}

	@Test
	public void test2_DiferentaFaraNotaExplicativa_BlocheazaInchiderea() {
		Long idRegistru = registruCuRaportZ(LocalDate.of(2030, 11, 10), 650, 50);
		assertEquals(StareRegistru.CU_DIFERENTE, workflowService.inchideZi(idRegistru, lei(950)));

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> workflowService.aprobaRegularizareSiFinalizeaza(idRegistru, 1L));
		assertEquals("Diferența de -50 lei trebuie justificată", ex.getMessage());
		assertEquals(StareRegistru.CU_DIFERENTE, workflowService.getRegistruSumar(idRegistru).getStare());
		assertTrue(zileInchise.isEmpty());

		// CU5, alternativa 1: nota explicativa + aprobarea administratorului -> Regularizat -> Inchis
		workflowService.justificaDiferenta(idRegistru, "Rest dat gresit unui client");
		assertEquals(StareRegistru.INCHIS, workflowService.aprobaRegularizareSiFinalizeaza(idRegistru, 1L));
		assertEquals(lei(950), zileInchise.get(0).getSoldCasa());
	}

	@Test
	public void test3_SoldulPestePlafon_GenereazaPropunereDeDepunere() {
		Long idRegistru = registruCuRaportZ(LocalDate.of(2030, 11, 11), 2100, 0);

		assertEquals(StareRegistru.INCHIS, workflowService.inchideZi(idRegistru, lei(2500)));
		assertEquals(lei(500), zileInchise.get(0).getPropunereDepunere());
	}

	@Test
	public void test4_LipsaRaportuluiZ_OpresteInchiderea() {
		Long idRegistru = workflowService.deschideRegistruInitial(LocalDate.of(2030, 11, 12), lei(400));

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> workflowService.inchideZi(idRegistru, lei(400)));
		assertEquals("Lipsește raportul Z", ex.getMessage());
		assertEquals(StareRegistru.DESCHIS, workflowService.getRegistruSumar(idRegistru).getStare());
	}

	/* Reguli suplimentare ale radacinii si ale serviciului */

	@Test
	public void test5_RegistruInchis_NuMaiAcceptaOperatiuni() {
		Long idRegistru = registruCuRaportZ(LocalDate.of(2030, 11, 20), 100, 0);
		workflowService.inchideZi(idRegistru, lei(500));

		assertThrows(IllegalStateException.class, () -> workflowService.inregistreazaOperatiune(idRegistru,
				TipOperatiune.PLATA_FURNIZOR, lei(50), "Dispozitie tarzie"));
		assertThrows(IllegalStateException.class,
				() -> workflowService.ataseazaRaportZ(idRegistru, 2, lei(0), lei(10)));
		assertThrows(IllegalStateException.class, () -> workflowService.inchideZi(idRegistru, lei(500)));
	}

	@Test
	public void test6_UnSingurRegistruPeZi() {
		LocalDate data = LocalDate.of(2030, 11, 21);
		workflowService.deschideRegistruInitial(data, lei(100));

		assertThrows(IllegalStateException.class, () -> workflowService.deschideRegistruInitial(data, lei(100)));
		// fara o zi inchisa anterioara nu exista de unde prelua soldul de deschidere
		assertThrows(IllegalStateException.class, () -> workflowService.deschideRegistru(LocalDate.of(2020, 1, 1)));
	}

	@Test
	public void test7_OperatiuniRefuzate() {
		Long idRegistru = registruCuRaportZ(LocalDate.of(2030, 11, 22), 100, 0);

		// incasarile in numerar vin doar din raportul Z
		assertThrows(IllegalArgumentException.class, () -> workflowService.inregistreazaOperatiune(idRegistru,
				TipOperatiune.INCASARE_NUMERAR, lei(20), "Chitanta"));
		// moneda registrului
		assertThrows(IllegalArgumentException.class, () -> workflowService.inregistreazaOperatiune(idRegistru,
				TipOperatiune.AVANS, new Suma(BigDecimal.valueOf(20), Moneda.EUR), "Dispozitie"));
		// un singur raport Z pe zi
		assertThrows(IllegalStateException.class,
				() -> workflowService.ataseazaRaportZ(idRegistru, 2, lei(0), lei(10)));

		// ID-uri locale in namespace-ul agregatului: 1 = incasarea din raportul Z
		assertEquals(2, workflowService.inregistreazaOperatiune(idRegistru, TipOperatiune.AVANS, lei(30),
				"Dispozitie de plata nr. 8"));
		RegistruCasaSumarView sumar = workflowService.getRegistruSumar(idRegistru);
		assertEquals(lei(470), sumar.getSoldScriptic()); // 400 + 100 - 30
	}

	@Test
	public void test8_RegistruInexistent_SiSoldDeschidereNegativ() {
		assertThrows(IllegalArgumentException.class, () -> workflowService.getRegistruSumar(-1L));
		// regula JSR-380 @DecimalMin pe Suma, verificata prin DomainEvent -> Validation Service
		assertThrows(RuntimeException.class,
				() -> workflowService.deschideRegistruInitial(LocalDate.of(2030, 11, 23), lei(-5)));
	}
}
