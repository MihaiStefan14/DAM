package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.application.services.IPlanningRegistruCasaBusinessWorkflowService;
import org.vivias.domain.casierie.Incasare.TipIncasare;
import org.vivias.domain.casierie.Plata.TipDestinatie;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.spring.AppScanConfig;

/*
 * WORKFLOW Business Service Tests
 * Strategy: Simple-Facade
 *
 * Reproduce fluxul "Inchiderea zilei in registrul de casa" (User Story 5,
 * Sprint 1): deschidere -> incasare -> plata -> inchidere zi (cu diferenta)
 * -> aprobare regularizare.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
public class Test3_BusinessWorkflowService {
	private static Logger logger = Logger.getLogger(Test3_BusinessWorkflowService.class.getName());

	@Autowired @Qualifier("PlanningRegistruCasaBusinessWorkflowServiceFacade")
	private IPlanningRegistruCasaBusinessWorkflowService workflowService;

	@Test
	public void test1_WorkflowServiceFacade() {
		logger.info("Workflow Service implementation instance:: " + workflowService);

		// 1.
		Integer registruId = workflowService.deschideRegistru(LocalDate.now(), new Suma(BigDecimal.valueOf(1000)));
		// 2.
		workflowService.inregistreazaIncasare(registruId, new Suma(BigDecimal.valueOf(500)), TipIncasare.NUMERAR,
				"Vanzari");
		// 3.
		workflowService.inregistreazaPlata(registruId, new Suma(BigDecimal.valueOf(300)), "Furnizor marfa",
				TipDestinatie.FURNIZOR);
		// 4.
		StareRegistru stare = workflowService.inchideZi(registruId, new Suma(BigDecimal.valueOf(1190)));
		assertEquals(StareRegistru.CU_DIFERENTE, stare);

		// 5.
		stare = workflowService.aprobaRegularizareSiFinalizeaza(registruId);
		assertEquals(StareRegistru.INCHIS, stare);

		// 6.
		RegistruCasaSumarView viewData = workflowService.getRegistruSumar(registruId);
		logger.info(viewData.toString());
	}
}
