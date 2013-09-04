package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.prov.DB_Util;
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

            DB_Util.persistStateMachines(connectionId);
            log.debug("saving state machines for connId: "+connectionId);

        } catch (Exception ex) {
            log.error(ex);
        }
        super.onSuccess();

    }
}
