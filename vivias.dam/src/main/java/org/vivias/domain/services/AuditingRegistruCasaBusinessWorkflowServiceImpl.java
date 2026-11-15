package org.vivias.domain.services;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasaAuditView;
import org.vivias.domain.casierie.RegistruCasaAuditView.EOperatiuneCasa;
import org.vivias.domain.casierie.Suma;
import org.vivias.infrastructure.IRegistruCasaAuditRepository;

@Service("AuditingRegistruCasaBusinessWorkflowServiceImpl")
// Auditing PlanningRegistruCasaBusinessWorkflowService: incasari, plati, inchidere zi, regularizare
public class AuditingRegistruCasaBusinessWorkflowServiceImpl implements IAuditingRegistruCasaBusinessWorkflowService {

	private static Logger logger = Logger.getLogger(AuditingRegistruCasaBusinessWorkflowServiceImpl.class.getName());

	// jurnalul de audit, salvat in baza de date
	@Autowired
	private IRegistruCasaAuditRepository auditRepository;

	// Business Logic
	@Override
	public void auditOperatiune(Long idRegistru, Suma suma, EOperatiuneCasa operatiune) {
		RegistruCasaAuditView intrare = new RegistruCasaAuditView(idRegistru, suma, operatiune);
		auditRepository.save(intrare);
		logger.info("AUDIT INFO: " + intrare);
	}

	@Override
	public List<RegistruCasaAuditView> getJurnalAudit() {
		return auditRepository.findAllByOrderByIdAsc();
	}
}
