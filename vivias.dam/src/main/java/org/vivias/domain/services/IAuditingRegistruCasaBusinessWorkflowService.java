package org.vivias.domain.services;

import java.util.List;

import org.vivias.domain.casierie.RegistruCasaAuditView;
import org.vivias.domain.casierie.RegistruCasaAuditView.EOperatiuneCasa;
import org.vivias.domain.casierie.Suma;

public interface IAuditingRegistruCasaBusinessWorkflowService {
	void auditOperatiune(Long idRegistru, Suma suma, EOperatiuneCasa operatiune);

	// Jurnalul de audit (Audit Entity Views), in ordinea inregistrarii
	List<RegistruCasaAuditView> getJurnalAudit();
}
