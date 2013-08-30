package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.UUID;

public class OscarsStubStatusTask extends Task {

    private static final Logger log = Logger.getLogger(OscarsStubStatusTask.class);
    private String gri;
    private OscarsStates state;

    public OscarsStubStatusTask(String gri, OscarsStates state) {
        this.gri = gri;
        this.state = state;
        this.scope = UUID.randomUUID().toString();
    }

    public void onRun() throws TaskException {
        try {
            log.debug("task id: "+this.id+" gri: "+gri+" state: "+state);
            OscarsProxy.getInstance().getStubStates().put(gri, state);
        } catch (Exception ex) {
            log.error(ex);

        }
    }

}
