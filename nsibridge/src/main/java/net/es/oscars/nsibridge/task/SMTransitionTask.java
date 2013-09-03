package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.ifces.StateMachineType;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.UUID;

public class SMTransitionTask extends SMTask {
    private static final Logger log = Logger.getLogger(SMTransitionTask.class);

    public SMTransitionTask() {
        this.scope = UUID.randomUUID().toString();
    }

    public void onRun() throws TaskException {
        super.onRun();

        try {
            this.getStateMachine().process(this.successEvent, this.correlationId);
            RequestHolder rh = RequestHolder.getInstance();
            String connId = rh.findConnectionId(correlationId);
            NSI_Util.persistStateMachines(connId);
            log.debug("saving state machines for connId: "+connId);

        } catch (Exception ex) {
            log.error(ex);
        }
        super.onSuccess();

    }
}
