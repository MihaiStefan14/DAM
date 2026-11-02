package org.vivias.application.services;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.services.AuditingRegistruCasaBusinessWorkflowServiceImpl;
import org.vivias.domain.services.IAuditingRegistruCasaBusinessWorkflowService;

/*
 * AOP-ready auditing service: decorates PlanningRegistruCasaBusinessWorkflowService
 * (via a Spring AOP proxy, see AppScanConfig) with an audit entry every time
 * inchideZi(...) completes, without the workflow service needing to know
 * about auditing (separation of concerns).
 */
@Service("AuditingRegistruCasaApplicationServiceImpl")
public class AuditingRegistruCasaApplicationServiceImpl
		extends AuditingRegistruCasaBusinessWorkflowServiceImpl
		implements IAuditingRegistruCasaBusinessWorkflowService, AfterReturningAdvice {

	private static Logger logger = Logger.getLogger(AuditingRegistruCasaApplicationServiceImpl.class.getName());

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		logger.info(">>>> SpringAOP.INTERCEPTION: Execute afterReturning for: " + method.getName());
		if (method.getName().equals("inchideZi")) {
			Integer registruId = (Integer) args[0];
			Suma soldFaptic = (Suma) args[1];

			auditInchidereZi(registruId, soldFaptic);

			logger.info(">>>...>>> Spring.AOP.DECORATED RegistruCasa: " + registruId + ", stare=" + returnValue);
		}
	}
}
