package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.LifecycleStateEnumType;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

public class ProvMonitor extends Thread {

    private static final Logger log = Logger.getLogger(ProvMonitor.class);
    
    public void run(){
        while(true){
            try{
                this.execute();
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                break;
            }catch(Exception e){
                log.error("Error in ProvMonitor: " + e.getMessage());
            }
        }
    }

    public void execute() {
        Date now = new Date();

        List<ConnectionRecord> recordList;
        try {
             recordList = DB_Util.getConnectionRecords();
        } catch (ServiceException ex) {
            log.error(ex);
            return;
        }

        for (ConnectionRecord cr: recordList) {
            if (cr.getLifecycleState() == null) {
                continue;
            }

            // do not set up oscars if we are TERMINATING / TERMINATED / FAILED
            if (!cr.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                continue;
            }
            String connId = cr.getConnectionId();

            ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
            // if there's no committed resvRecord, we're before the very first ReserveHeld
            if (rr == null) {
                continue;
            }

            OscarsProvQueue opq = OscarsProvQueue.getInstance();
            // log.debug("considering connId: "+connId+" ps: "+cr.getProvisionState()+" start: "+rr.getStartTime()+" end: "+rr.getEndTime()+" now: "+now);

            try {
                switch (cr.getProvisionState()) {
                    case PROVISIONED:
                        // send a setup if provisioned and within the resv times
                        if (now.after(rr.getStartTime()) && now.before(rr.getEndTime())) {
                            if (opq.needsOp(connId, OscarsOps.SETUP)) {
                               opq.scheduleOp(connId, OscarsOps.SETUP);
                            }
                        }

                        break;
                    case RELEASED:
                        // send a teardown if after start time
                        if (now.after(rr.getStartTime())) {
                            if (opq.needsOp(connId, OscarsOps.TEARDOWN)) {
                                opq.scheduleOp(connId, OscarsOps.TEARDOWN);
                            }
                        }
                        break;
                    case RELEASING:
                    case PROVISIONING:
                    default:
                        continue;

                }
            } catch (TaskException ex) {
                log.error(ex);
            }
        }
    }



}
