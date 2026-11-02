package org.vivias.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vivias.application.services.IPlanningRegistruCasaBusinessWorkflowService;
import org.vivias.domain.casierie.RegistruCasaAuditView.EOperatiuneCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
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

	@Autowired @Qualifier("AuditingRegistruCasaApplicationServiceImpl")
	private IAuditingRegistruCasaBusinessWorkflowService auditService;

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	@Test
	public void test_IntegratedServices() {
		logger.info("Workflow Service implementation instance:: " + auditedService);
		logger.info("Workflow Service implementation class:: " + auditedService.getClass().getName());

		Long idRegistru = auditedService.deschideRegistruInitial(LocalDate.of(2031, 1, 5), lei(500));
		auditedService.ataseazaRaportZ(idRegistru, 1, lei(50), lei(100));

		// AuditingRegistruCasaApplicationServiceImpl.afterReturning(...) logheaza automat auditul
		StareRegistru stare = auditedService.inchideZi(idRegistru, lei(600));
		assertEquals(StareRegistru.INCHIS, stare);

		RegistruCasaSumarView sumar = auditedService.getRegistruSumar(idRegistru);
		logger.info(sumar.toString());
		assertEquals(lei(600), sumar.getSoldScriptic());

		List<EOperatiuneCasa> operatiuniAuditate = auditService.getJurnalAudit().stream()
				.filter(a -> idRegistru.equals(a.getIdRegistru()))
				.map(a -> a.getOperatiune())
				.collect(Collectors.toList());
		assertEquals(List.of(EOperatiuneCasa.REGISTRU_DESCHIS, EOperatiuneCasa.RAPORT_Z_ATASAT,
				EOperatiuneCasa.ZI_INCHISA), operatiuniAuditate);
	}
}
