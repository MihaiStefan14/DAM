package org.vivias.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipDiferenta;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.spring.AppScanConfig;

/*
 * WORKFLOW Business Service Tests
 * Strategy: Event-based (producer: testul -> WorkflowCallEvent;
 * consumer: PlanningRegistruCasaEventWorkflowServiceImpl -> WorkflowResponseEvent)
 *
 * Fluxul User Story 5 din DAM Sprint 1, cu o diferenta regularizata.
 * Spring livreaza evenimentele sincron, deci raspunsul e disponibil imediat
 * dupa publishEvent(...).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
public class Test6_EventWorkflowService {
	private static Logger logger = Logger.getLogger(Test6_EventWorkflowService.class.getName());

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private ConfigurableApplicationContext context;

	private final List<WorkflowResponseEvent> raspunsuri = new ArrayList<>();

	@BeforeEach
	public void ascultaRaspunsurile() {
		// clasa anonima (nu lambda): Spring isi poate deduce tipul evenimentului ascultat
		context.addApplicationListener(new ApplicationListener<WorkflowResponseEvent>() {
			@Override
			public void onApplicationEvent(WorkflowResponseEvent event) {
				raspunsuri.add(event);
			}
		});
	}

	private Object cere(WorkflowAction actiune, Object... parametri) {
		applicationEventPublisher.publishEvent(new WorkflowCallEvent(this, parametri, actiune));

		WorkflowResponseEvent raspuns = raspunsuri.get(raspunsuri.size() - 1);
		assertEquals(actiune, raspuns.getWorkflowAction());
		logger.info("Raspuns " + actiune + ": " + raspuns.getWorkflowParameters()[0]);
		return raspuns.getWorkflowParameters()[0];
	}

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	@Test
	public void test1_EventWorkflowService() {
		Long idRegistru = (Long) cere(WorkflowAction.DESCHIDE_REGISTRU_INITIAL, LocalDate.of(2031, 2, 3),
				lei(1000));
		assertNotNull(idRegistru);

		assertEquals(lei(1500), cere(WorkflowAction.ATASEAZA_RAPORT_Z, idRegistru, 1, lei(200), lei(500)));
		cere(WorkflowAction.INREGISTREAZA_OPERATIUNE, idRegistru, TipOperatiune.PLATA_FURNIZOR, lei(300),
				"Dispozitie de plata nr. 1");

		assertEquals(StareRegistru.CU_DIFERENTE, cere(WorkflowAction.INCHIDE_ZI, idRegistru, lei(1190)));
		cere(WorkflowAction.JUSTIFICA_DIFERENTA, idRegistru, "Rest dat gresit");
		assertEquals(StareRegistru.INCHIS, cere(WorkflowAction.APROBA_REGULARIZARE, idRegistru, 1L));

		RegistruCasaSumarView sumar = (RegistruCasaSumarView) cere(WorkflowAction.GET_REGISTRU_SUMAR, idRegistru);
		assertEquals(lei(1200), sumar.getSoldScriptic());
		assertEquals(TipDiferenta.MINUS, sumar.getDiferentaTip());
		assertEquals(lei(10), sumar.getDiferentaValoare());
		assertEquals(7, raspunsuri.size());
	}
}
