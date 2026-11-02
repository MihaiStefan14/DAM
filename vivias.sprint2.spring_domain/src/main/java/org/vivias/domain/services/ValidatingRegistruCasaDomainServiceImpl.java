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

		String violationExceptionMessage = violations.stream()
				.map(v -> "\n>>> JEE.Spring bean validator exception: " + v)
				.collect(Collectors.joining(", "));

		// regulile de agregat se verifica mereu, nu doar cand exista si violari JSR-380
		boolean agregatValid = true;
		try {
			validateRegistruCasaAggregate(registru);
		} catch (RuntimeException ex) {
			agregatValid = false;
			violationExceptionMessage += "\n>>> Local validation: " + ex.getMessage();
		}

		if (!violations.isEmpty() || !agregatValid)
			throw new Exception(violationExceptionMessage);
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
		if (registru.getSoldDeschidere() == null || registru.getSoldDeschidere().getValoare() == null
				|| registru.getSoldDeschidere().getValoare().compareTo(BigDecimal.ZERO) < 0)
			throw new RuntimeException("Soldul de deschidere trebuie sa fie >= 0!");
		// numerarul din casa nu poate fi negativ
		if (registru.calculeazaSoldScriptic().semn() < 0)
			throw new RuntimeException("Soldul scriptic al casei nu poate fi negativ!");

		StareRegistru stare = registru.getStare();
		// CU5: ziua nu se poate inchide fara raportul Z si fara soldul numarat de casier
		if (stare != StareRegistru.DESCHIS && registru.getRaportZ() == null)
			throw new RuntimeException("Lipsește raportul Z");
		if ((stare == StareRegistru.CU_DIFERENTE || stare == StareRegistru.REGULARIZAT
				|| stare == StareRegistru.INCHIS) && registru.getSoldFaptic() == null)
			throw new RuntimeException("Soldul faptic este obligatoriu la inchiderea zilei!");
		// CU5, alternativa 1: o zi inchisa cu diferenta trebuie sa fi fost justificata si aprobata
		if (stare == StareRegistru.INCHIS && !registru.getSoldFaptic().equals(registru.calculeazaSoldScriptic())
				&& (registru.getNotaExplicativa() == null || registru.getAprobatDe() == null))
			throw new RuntimeException("Ziua a fost inchisa cu o diferenta nejustificata sau neaprobata!");
		return true;
	}
}
