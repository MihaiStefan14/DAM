package org.vivias.domain.services;

import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;

@Service
public class ValidatingRegistruCasaDomainServiceImpl
		implements IValidatingRegistruCasaDomainService, ApplicationListener<DomainEvent> {

	private static Logger logger = Logger.getLogger(ValidatingRegistruCasaDomainServiceImpl.class.getName());

	@Autowired
	private Validator validator;

	@Override
	public Set<String> validate(RegistruCasa registru) {
		// JavaBean (JSR-380) Validation Rules annotated into the Domain Entity
		Set<ConstraintViolation<RegistruCasa>> violations = validator.validate(registru);

		logger.info("Violations count: " + violations.size());

		return violations.stream()
				.map(v -> v.getMessage() + " (" + v.getInvalidValue() + ") is an invalid value!")
				.collect(Collectors.toSet());
	}

	@Override
	public void validateWithException(RegistruCasa registru) throws Exception {
		Set<String> violations = validate(registru);
		logger.info("Violations count (to generate exception): " + violations.size());

		if (violations.size() > 0) {
			String violationExceptionMessage = violations.stream()
					.map(v -> "\n>>> JEE.Spring bean validator exception: " + v)
					.collect(Collectors.joining(", "));
			try {
				validateRegistruCasaAggregate(registru);
			} catch (Exception ex) {
				violationExceptionMessage += "\n>>> Local validation: " + ex.getMessage();
			}
			throw new Exception(violationExceptionMessage);
		}
	}

	// Event-based Business Service Integration (SpringEvents listening)
	@Override
	public void onApplicationEvent(DomainEvent domainEvent) {
		RegistruCasa registru = domainEvent.getMessage();
		logger.info(">>>***Validating_ListeningEvents***>>> for: " + registru);

		try {
			this.validateWithException(registru);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	// Domain Validation Service: business-level aggregate invariants,
	// complementare regulilor JSR-380 de mai sus
	@Override
	public boolean validateRegistruCasaAggregate(RegistruCasa registru) {
		if (registru == null)
			throw new RuntimeException("Null RegistruCasa!");
		if (registru.getData() == null)
			throw new RuntimeException("Data registrului este obligatorie!");
		if (registru.getSoldInitial() == null || registru.getSoldInitial().getValoare() == null
				|| registru.getSoldInitial().getValoare().compareTo(BigDecimal.ZERO) < 0)
			throw new RuntimeException("Soldul initial trebuie sa fie >= 0!");
		// regula din User Story 5 (Sprint 1): ziua nu se poate inchide fara
		// soldul faptic numarat de casier
		if ((registru.getStare() == StareRegistru.CU_DIFERENTE || registru.getStare() == StareRegistru.REGULARIZAT
				|| registru.getStare() == StareRegistru.INCHIS) && registru.getSoldFaptic() == null)
			throw new RuntimeException("Soldul faptic este obligatoriu la inchiderea zilei!");
		return true;
	}
}
