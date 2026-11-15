package org.vivias.domain.casierie;

import java.util.logging.Logger;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

// JPA Entity Listener: auditul operatiilor de persistenta pe registrul de casa
public class RegistruCasaAuditListener {
	private static Logger logger = Logger.getLogger(RegistruCasaAuditListener.class.getName());

	@PrePersist
	public void onPrePersist(RegistruCasa registru) {
		auditRegistru(registru, RegistruCasaEntityOperation.ADDED);
	}

	@PreUpdate
	public void onPreUpdate(RegistruCasa registru) {
		auditRegistru(registru, RegistruCasaEntityOperation.UPDATED);
	}

	@PreRemove
	public void onPreRemove(RegistruCasa registru) {
		auditRegistru(registru, RegistruCasaEntityOperation.DELETED);
	}

	private void auditRegistru(RegistruCasa registru, RegistruCasaEntityOperation operation) {
		logger.info(">>> JPA RegistruCasaAuditListener: registrul din " + registru.getData() + " (stare "
				+ registru.getStare() + ") > " + operation);
	}

	static public enum RegistruCasaEntityOperation {
		ADDED, UPDATED, DELETED;
	}
}
