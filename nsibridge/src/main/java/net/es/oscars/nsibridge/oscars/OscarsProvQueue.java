package net.es.oscars.nsibridge.oscars;

import net.es.oscars.utils.task.RunState;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OscarsProvQueue {
    private static Logger log = Logger.getLogger(OscarsProvQueue.class);
    private static OscarsProvQueue ourInstance = new OscarsProvQueue();

    private ConcurrentLinkedQueue<OscarsAction> scheduled = new ConcurrentLinkedQueue<OscarsAction>();
    private ConcurrentLinkedQueue<OscarsAction> running = new ConcurrentLinkedQueue<OscarsAction>();
    private ConcurrentHashMap<UUID, RunState> runStates = new ConcurrentHashMap<UUID, RunState>();



    public static OscarsProvQueue getInstance() {
        return ourInstance;
    }

    private OscarsProvQueue() {
    }

    public synchronized void scheduleOp(String connId, OscarsOps op) {
        boolean found = false;
        for (OscarsAction act : scheduled) {
            if (act.getConnId().equals(connId) && act.getOp().equals(op)) {
                // log.debug("found existing action for connId: "+connId+" op: "+op);
                found = true;
            }
        }
        if (found) return;

        for (OscarsAction act : running) {
            if (act.getConnId().equals(connId) && act.getOp().equals(op)) {
                found = true;
            }
        }

        if (found) return;

        OscarsAction action = new OscarsAction();
        action.setConnId(connId);
        action.setOp(op);
        action.setId(UUID.randomUUID());
        // log.debug("scheduled new action for connId: "+connId+" op: "+op);
        runStates.put(action.getId(), RunState.SCHEDULED);
        scheduled.add(action);
    }

    public List<OscarsAction> getScheduled(String connId) {
        List<OscarsAction> results = new ArrayList<OscarsAction>();
        for (OscarsAction act : scheduled) {
            if (act.getConnId().equals(connId)) {
                results.add(act);
            }
        }
        return results;

    }

}
