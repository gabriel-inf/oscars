package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.ifces.StateException;
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
            this.stateMachine.process(this.successEvent, this.correlationId);

        } catch (StateException ex) {
            log.error(ex);
        }
        super.onSuccess();

    }
}
