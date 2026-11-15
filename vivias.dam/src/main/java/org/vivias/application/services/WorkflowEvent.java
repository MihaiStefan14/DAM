package org.vivias.application.services;

import org.springframework.context.ApplicationEvent;

// Base workflow event: actiunea + parametrii (call) sau rezultatul (response)
public abstract class WorkflowEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private final Object[] workflowParameters;
	private final WorkflowAction workflowAction;

	public WorkflowEvent(Object source, Object[] workflowParameters, WorkflowAction workflowAction) {
		super(source);
		this.workflowParameters = workflowParameters;
		this.workflowAction = workflowAction;
	}

	public Object[] getWorkflowParameters() {
		return workflowParameters;
	}

	public WorkflowAction getWorkflowAction() {
		return workflowAction;
	}
}
