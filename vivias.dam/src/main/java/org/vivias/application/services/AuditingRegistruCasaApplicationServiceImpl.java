package org.vivias.application.services;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasaAuditView.EOperatiuneCasa;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.services.AuditingRegistruCasaBusinessWorkflowServiceImpl;
import org.vivias.domain.services.IAuditingRegistruCasaBusinessWorkflowService;

/*
 * AOP-ready auditing service: decorates PlanningRegistruCasaBusinessWorkflowService
 * (via a Spring AOP proxy, see AppScanConfig) with an audit entry every time
 * a cash-register operation completes, without the workflow service needing
 * to know about auditing (separation of concerns). APOO, Tabelul 1.1:
 * jurnal de audit pentru incasari, plati si registrul de casa.
 */
@Service("AuditingRegistruCasaApplicationServiceImpl")
public class AuditingRegistruCasaApplicationServiceImpl
		extends AuditingRegistruCasaBusinessWorkflowServiceImpl
		implements IAuditingRegistruCasaBusinessWorkflowService, AfterReturningAdvice {

	private static Logger logger = Logger.getLogger(AuditingRegistruCasaApplicationServiceImpl.class.getName());

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		logger.info(">>>> SpringAOP.INTERCEPTION: Execute afterReturning for: " + method.getName());

		switch (method.getName()) {
		case "deschideRegistruInitial":
			auditOperatiune((Long) returnValue, (Suma) args[1], EOperatiuneCasa.REGISTRU_DESCHIS);
			break;
		case "deschideRegistru":
			auditOperatiune((Long) returnValue, null, EOperatiuneCasa.REGISTRU_DESCHIS);
			break;
		case "ataseazaRaportZ":
			auditOperatiune((Long) args[0], (Suma) args[3], EOperatiuneCasa.RAPORT_Z_ATASAT);
			break;
		case "inregistreazaOperatiune":
			auditOperatiune((Long) args[0], (Suma) args[2], EOperatiuneCasa.OPERATIUNE_INREGISTRATA);
			break;
		case "inchideZi":
			auditOperatiune((Long) args[0], (Suma) args[1], returnValue == StareRegistru.INCHIS
					? EOperatiuneCasa.ZI_INCHISA : EOperatiuneCasa.DIFERENTA_CONSTATATA);
			break;
		case "justificaDiferenta":
			auditOperatiune((Long) args[0], null, EOperatiuneCasa.DIFERENTA_JUSTIFICATA);
			break;
		case "aprobaRegularizareSiFinalizeaza":
			auditOperatiune((Long) args[0], null, EOperatiuneCasa.DIFERENTA_REGULARIZATA);
			auditOperatiune((Long) args[0], null, EOperatiuneCasa.ZI_INCHISA);
			break;
		default:
			return; // operatiile de citire (getRegistruSumar) nu se auditeaza
		}
		logger.info(">>>...>>> Spring.AOP.DECORATED RegistruCasa: " + args[0] + ", rezultat=" + returnValue);
	}
}
