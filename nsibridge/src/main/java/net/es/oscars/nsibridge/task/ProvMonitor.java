package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ProvMonitor implements Job {

    private static final Logger log = Logger.getLogger(ProvMonitor.class);


    public void execute(JobExecutionContext arg0) throws JobExecutionException {

        EntityManager em = PersistenceHolder.getEntityManager();

        em.getTransaction().begin();
        Date now = new Date();


        String query = "SELECT c FROM ConnectionRecord c";
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        for (ConnectionRecord cr: recordList) {
            if (cr.getLifecycleState() == null) {
                continue;
            }

            // do not set up oscars if we are TERMINATING / TERMINATED / FAILED
            if (cr.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                if (cr.getProvisionState() == null) {
                    continue;
                }

                if (cr.getProvisionState().equals(ProvisionStateEnumType.PROVISIONED)) {
                    String connId = cr.getConnectionId();
                    OscarsProvQueue.getInstance().getInspect().put(connId, OscarsOps.SETUP);
                    log.info("should start SETUP: "+connId);

                    ResvRecord rr = ConnectionRecord.getLatestResvRecord(cr);
                    if (rr == null) {
                        continue;
                    }


                    if (rr.getStartTime().after(now)) {
                        // log.info("should start setup "+connId);
                    }
                }

                if (cr.getProvisionState().equals(ProvisionStateEnumType.RELEASED)) {
                    String connId = cr.getConnectionId();
                    OscarsProvQueue.getInstance().getInspect().put(connId, OscarsOps.TEARDOWN);
                    log.info("should start TEARDOWN: "+connId);

                    ResvRecord rr = ConnectionRecord.getLatestResvRecord(cr);
                    if (rr == null) {
                        continue;
                    }


                    if (rr.getEndTime().before(now)) {
                        // log.info("should start teardown "+connId);
                    }
                }

            }
        }


    }



}
