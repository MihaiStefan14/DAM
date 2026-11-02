package org.vivias.domain.services;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasaAuditView;
import org.vivias.domain.casierie.RegistruCasaAuditView.EOperatiuneCasa;
import org.vivias.domain.casierie.Suma;

@Service("AuditingRegistruCasaBusinessWorkflowServiceImpl")
// Auditing PlanningRegistruCasaBusinessWorkflowService :: inchideZi
public class AuditingRegistruCasaBusinessWorkflowServiceImpl implements IAuditingRegistruCasaBusinessWorkflowService {

	private static Logger logger = Logger.getLogger(AuditingRegistruCasaBusinessWorkflowServiceImpl.class.getName());

	// Business Logic
	@Override
	public void auditInchidereZi(Integer registruId, Suma soldFaptic) {
		logger.info("AUDIT INFO: " + new RegistruCasaAuditView(registruId, soldFaptic, EOperatiuneCasa.ZI_INCHISA));
	}
}
