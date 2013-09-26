package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;

public class ProvMonitor implements Job {

    private static final Logger log = Logger.getLogger(ProvMonitor.class);


    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        Date now = new Date();

        Long millis = now.getTime();
        Long sec = millis / 1000;
        if (sec % 5 == 0) {
            log.debug("prov monitor alive");
        }

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
            // log.debug("considering connId: "+connId+" ps: "+cr.getProvisionState()+" record: "+now);

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
