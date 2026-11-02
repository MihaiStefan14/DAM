package org.vivias.application.services;

// Workflow event: raspunsul (producer = PlanningRegistruCasaEventWorkflowServiceImpl, consumer = client)
public class WorkflowResponseEvent extends WorkflowEvent {
	private static final long serialVersionUID = 1L;

	public WorkflowResponseEvent(Object source, Object[] workflowParameters, WorkflowAction workflowAction) {
		super(source, workflowParameters, workflowAction);
	}
}
