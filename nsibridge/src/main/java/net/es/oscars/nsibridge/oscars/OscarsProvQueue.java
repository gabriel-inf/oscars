package net.es.oscars.nsibridge.oscars;

import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.StateMachineType;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.task.OscarsSetupTask;
import net.es.oscars.nsibridge.task.OscarsTask;
import net.es.oscars.nsibridge.task.OscarsTeardownTask;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OscarsProvQueue {
    private static Logger log = Logger.getLogger(OscarsProvQueue.class);
    private static OscarsProvQueue ourInstance = new OscarsProvQueue();

    private ConcurrentHashMap<String, OscarsOps> lastScheduled = new ConcurrentHashMap<String, OscarsOps>();


    public static OscarsProvQueue getInstance() {
        return ourInstance;
    }

    private OscarsProvQueue() {
    }

    public synchronized boolean needsOp(String connId, OscarsOps op) {
        if (op.equals(OscarsOps.SETUP)) {
            // we have never set up this connId
            if (lastScheduled.get(connId) == null) {
                return true;
            }
            // the last thing we did was to setup this connId
            if (lastScheduled.get(connId) == op) {
                return false;
            } else {
                // the last thing we did was to teardown this connId
                return true;
            }
        } else if (op.equals(OscarsOps.TEARDOWN)) {
            // we have never set up this connId; do not tear down
            if (lastScheduled.get(connId) == null) {
                return false;
            }
            // the last thing we did was to tear down this connId
            if (lastScheduled.get(connId) == op) {
                return false;
            } else {
                // the last thing we did was to set up this connId
                return true;
            }
        } else {
            log.error("invalid op "+op);
            // should never get here
            return false;
        }
    }

    public synchronized void scheduleOp(String connId, OscarsOps op) throws TaskException {
        log.debug("scheduling "+connId+" op: "+op);
        if (!needsOp(connId, op)) {
            return;
        }
        long now = new Date().getTime();

        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();

        OscarsTask ost;
        if (op.equals(OscarsOps.SETUP)) {
            ost = new OscarsSetupTask();
            ost.setOscarsOp(OscarsOps.SETUP);
            ost.setFailEvent(NSI_Prov_Event.LOCAL_SETUP_FAILED);
            ost.setSuccessEvent(NSI_Prov_Event.LOCAL_SETUP_CONFIRMED);
        } else if (op.equals(OscarsOps.TEARDOWN)) {
            ost = new OscarsTeardownTask();
            ost.setOscarsOp(OscarsOps.TEARDOWN);
            ost.setFailEvent(NSI_Prov_Event.LOCAL_TEARDOWN_FAILED);
            ost.setSuccessEvent(NSI_Prov_Event.LOCAL_TEARDOWN_CONFIRMED);
        } else {
            throw new TaskException("invalid oscarsOp!");
        }
        ost.setConnectionId(connId);
        ost.setCorrelationId(UUID.randomUUID().toString());
        ost.setSmt(StateMachineType.PSM);

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        UUID taskId = wf.schedule(ost, when);
        log.debug("scheduled new action for connId: "+connId+" op: "+op+" taskId: "+taskId);
        lastScheduled.put(connId, op);
    }

    public OscarsOps getScheduled(String connId) {
        return lastScheduled.get(connId);
    }

}
