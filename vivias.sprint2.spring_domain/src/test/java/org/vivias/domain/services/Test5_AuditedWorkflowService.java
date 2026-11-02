package org.vivias.domain.services;

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
import org.vivias.domain.casierie.Suma;
import org.vivias.spring.AppScanConfig;

/*
 * Integrated test:
 * - PlanningRegistruCasaBusinessWorkflowService as proxy-AOP
 * - AuditingRegistruCasaApplicationService with AfterReturningAdvice aspect
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppScanConfig.class)
public class Test5_AuditedWorkflowService {
	private static Logger logger = Logger.getLogger(Test5_AuditedWorkflowService.class.getName());

	@Autowired @Qualifier("proxyAuditedApplicationService")
	private IPlanningRegistruCasaBusinessWorkflowService auditedService;

	@Test
	public void test_IntegratedServices() {
		logger.info("Workflow Service implementation instance:: " + auditedService);
		logger.info("Workflow Service implementation class:: " + auditedService.getClass().getName());

		Integer registruId = auditedService.deschideRegistru(LocalDate.now(), new Suma(BigDecimal.valueOf(500)));
		auditedService.inregistreazaIncasare(registruId, new Suma(BigDecimal.valueOf(100)), TipIncasare.CARD, "Test");

		// AuditingRegistruCasaApplicationServiceImpl.afterReturning(...) loghează automat auditul
		auditedService.inchideZi(registruId, new Suma(BigDecimal.valueOf(600)));

		logger.info(auditedService.getRegistruSumar(registruId).toString());
	}
}
