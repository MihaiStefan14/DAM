package org.vivias.spring;

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

	@Bean
	public Validator initValidator() {
		logger.info("... Init VALIDATION CONTEXT.");

		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		return validator;
	}
}
