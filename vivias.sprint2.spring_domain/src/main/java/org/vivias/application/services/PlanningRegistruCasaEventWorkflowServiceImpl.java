package org.vivias.application.services;

import java.time.LocalDate;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;

/*
 * Business Workflow Service - Event-based Strategy (slide 44, T2.C4):
 * consuma WorkflowCallEvent, executa pasul prin facade-ul mostenit si
 * publica rezultatul ca WorkflowResponseEvent.
 */
@Service("PlanningRegistruCasaEventWorkflowServiceFacade")
public class PlanningRegistruCasaEventWorkflowServiceImpl
		extends PlanningRegistruCasaBusinessWorkflowServiceImpl
		implements ApplicationListener<WorkflowCallEvent> {

	private static Logger logger = Logger.getLogger(PlanningRegistruCasaEventWorkflowServiceImpl.class.getName());

	// Event Producer Service
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	// Event Consumer Service
	@Override
	public void onApplicationEvent(WorkflowCallEvent event) {
		WorkflowAction actiune = event.getWorkflowAction();
		Object[] p = event.getWorkflowParameters();
		logger.info("Workflow Service: processing " + actiune + " event!");

		Object rezultat;
		switch (actiune) {
		case DESCHIDE_REGISTRU_INITIAL:
			rezultat = deschideRegistruInitial((LocalDate) p[0], (Suma) p[1]);
			break;
		case DESCHIDE_REGISTRU:
			rezultat = deschideRegistru((LocalDate) p[0]);
			break;
		case ATASEAZA_RAPORT_Z:
			rezultat = ataseazaRaportZ((Long) p[0], (Integer) p[1], (Suma) p[2], (Suma) p[3]);
			break;
		case INREGISTREAZA_OPERATIUNE:
			rezultat = inregistreazaOperatiune((Long) p[0], (TipOperatiune) p[1], (Suma) p[2], (String) p[3]);
			break;
		case INCHIDE_ZI:
			rezultat = inchideZi((Long) p[0], (Suma) p[1]);
			break;
		case JUSTIFICA_DIFERENTA:
			rezultat = justificaDiferenta((Long) p[0], (String) p[1]);
			break;
		case APROBA_REGULARIZARE:
			rezultat = aprobaRegularizareSiFinalizeaza((Long) p[0], (Long) p[1]);
			break;
		case GET_REGISTRU_SUMAR:
			rezultat = getRegistruSumar((Long) p[0]);
			break;
		default:
			throw new IllegalArgumentException("WorkflowAction necunoscut: " + actiune);
		}

		applicationEventPublisher.publishEvent(new WorkflowResponseEvent(this, new Object[] { rezultat }, actiune));
	}
}
