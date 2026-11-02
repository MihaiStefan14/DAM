package org.vivias.spring;

import java.math.BigDecimal;
import java.util.logging.Logger;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.vivias.application.services.IPlanningRegistruCasaBusinessWorkflowService;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.services.IAuditingRegistruCasaBusinessWorkflowService;

@Configuration
@ComponentScan(basePackages = {
		"org.vivias.domain.services",
		"org.vivias.application.services"
	})
public class AppScanConfig {
	private static Logger logger = Logger.getLogger(AppScanConfig.class.getName());

	// AOP Auditing
	@Autowired @Qualifier("PlanningRegistruCasaBusinessWorkflowServiceFacade")
	private IPlanningRegistruCasaBusinessWorkflowService planningRegistruCasaBusinessWorkflowService;

	@Autowired @Qualifier("AuditingRegistruCasaApplicationServiceImpl")
	private IAuditingRegistruCasaBusinessWorkflowService auditApplicationService;

	@Bean(name = "proxyAuditedApplicationService")
	public IPlanningRegistruCasaBusinessWorkflowService initAuditedApplicationService() {
		ProxyFactory pf = new ProxyFactory();
		pf.addAdvice((AfterReturningAdvice) auditApplicationService);
		pf.setTarget(planningRegistruCasaBusinessWorkflowService);
		IPlanningRegistruCasaBusinessWorkflowService proxyAuditedApplicationService =
				(IPlanningRegistruCasaBusinessWorkflowService) pf.getProxy();

		return proxyAuditedApplicationService;
	}

	// Parametru al aplicatiei (APOO cap. 1.1: plafoanele nu se scriu in codul de domeniu):
	// plafonul intern de casa stabilit de administrator.
	// static: nu depinde de campurile @Autowired ale acestei clase de configurare
	@Bean
	public static PlafoaneNumerar plafoaneNumerar() {
		return new PlafoaneNumerar(new Suma(BigDecimal.valueOf(2000)));
	}

	@Bean
	public Validator initValidator() {
		logger.info("... Init VALIDATION CONTEXT.");

		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		return validator;
	}
}
