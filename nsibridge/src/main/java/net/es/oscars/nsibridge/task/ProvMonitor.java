package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

public class ProvMonitor implements Job {

    private static final Logger log = Logger.getLogger(ProvMonitor.class);


    public void execute(JobExecutionContext arg0) throws JobExecutionException {
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

            try {
                switch (cr.getProvisionState()) {
                    case PROVISIONED:
                        // send a setup if provisioned and
                        if (rr.getStartTime().before(now) && rr.getEndTime().after(now)) {
                            if (opq.needsOp(connId, OscarsOps.SETUP)) {
                               opq.scheduleOp(connId, OscarsOps.SETUP);
                            }
                        }

                        break;
                    case RELEASED:
                        if (opq.needsOp(connId, OscarsOps.TEARDOWN)) {
                            opq.scheduleOp(connId, OscarsOps.TEARDOWN);
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
