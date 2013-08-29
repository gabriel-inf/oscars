package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.ifces.SM_Event;
import net.es.oscars.nsibridge.ifces.StateMachine;
import net.es.oscars.utils.task.Task;

public class SMTask extends Task {
    protected StateMachine stateMachine;
    protected SM_Event successEvent;
    protected SM_Event failEvent;
    protected String correlationId;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public SM_Event getSuccessEvent() {
        return successEvent;
    }

    public void setSuccessEvent(SM_Event successEvent) {
        this.successEvent = successEvent;
    }

    public SM_Event getFailEvent() {
        return failEvent;
    }

    public void setFailEvent(SM_Event failEvent) {
        this.failEvent = failEvent;
    }
}
