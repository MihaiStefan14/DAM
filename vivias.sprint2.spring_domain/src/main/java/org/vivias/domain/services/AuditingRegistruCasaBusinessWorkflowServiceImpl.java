package org.vivias.domain.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasaAuditView;
import org.vivias.domain.casierie.RegistruCasaAuditView.EOperatiuneCasa;
import org.vivias.domain.casierie.Suma;

@Service("AuditingRegistruCasaBusinessWorkflowServiceImpl")
// Auditing PlanningRegistruCasaBusinessWorkflowService: incasari, plati, inchidere zi, regularizare
public class AuditingRegistruCasaBusinessWorkflowServiceImpl implements IAuditingRegistruCasaBusinessWorkflowService {

	private static Logger logger = Logger.getLogger(AuditingRegistruCasaBusinessWorkflowServiceImpl.class.getName());

	// in-memory audit log (analog repository-ului Map-based)
	private final List<RegistruCasaAuditView> jurnalAudit = Collections.synchronizedList(new ArrayList<>());

	// Business Logic
	@Override
	public void auditOperatiune(Long idRegistru, Suma suma, EOperatiuneCasa operatiune) {
		RegistruCasaAuditView intrare = new RegistruCasaAuditView(idRegistru, suma, operatiune);
		jurnalAudit.add(intrare);
		logger.info("AUDIT INFO: " + intrare);
	}

	@Override
	public List<RegistruCasaAuditView> getJurnalAudit() {
		synchronized (jurnalAudit) {
			return new ArrayList<>(jurnalAudit);
		}
	}
}
