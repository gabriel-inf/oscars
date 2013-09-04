package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.ifces.SM_Event;
import net.es.oscars.nsibridge.ifces.StateMachine;
import net.es.oscars.nsibridge.ifces.StateMachineType;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;

public class SMTask extends Task {
    private StateMachineType smt;

    protected SM_Event successEvent;
    protected SM_Event failEvent;
    protected String correlationId;
    protected String connectionId;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public StateMachine getStateMachine() throws TaskException {
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (smh == null) {
            throw new TaskException("could not find the state machine holder");
        }


        RequestHolder rh = RequestHolder.getInstance();
        if (rh == null) {
            throw new TaskException("could not find the request holder");
        }

        if (smt == null) {
            throw new TaskException("no state machine type set");
        }

        switch (smt) {
            case RSM:
                return smh.findNsiResvSM(connectionId);
            case PSM:
                return smh.findNsiProvSM(connectionId);
            case LSM:
                return smh.findNsiLifeSM(connectionId);
        }
        throw new TaskException("could not find a an "+smt+" SM for connId: "+connectionId);

    }

    public StateMachineType getSmt() {
        return smt;
    }

    public void setSmt(StateMachineType smt) {
        this.smt = smt;
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
