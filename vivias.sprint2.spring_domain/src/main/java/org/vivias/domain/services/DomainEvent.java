package org.vivias.domain.services;

import org.springframework.context.ApplicationEvent;
import org.vivias.domain.casierie.RegistruCasa;

public class DomainEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private RegistruCasa message;

	public DomainEvent(Object source) {
		super(source);
	}

	public DomainEvent(Object source, RegistruCasa message) {
		super(source);
		this.message = message;
	}

	public RegistruCasa getMessage() {
		return message;
	}
}

// https://www.baeldung.com/spring-events
