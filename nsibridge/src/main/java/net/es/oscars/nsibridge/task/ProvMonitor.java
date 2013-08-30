package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
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
            // log.info("connId: "+cr.getConnectionId());
            // log.info("  RSM: "+ cr.getReserveState());
            // log.info("  PSM: "+ cr.getProvisionState());
            // log.info("  LSM: "+ cr.getLifecycleState());

            if (cr.getLifecycleState() == null) {
                continue;
            }
            // do not set up oscars if we are TERMINATING / TERMINATED
            if (cr.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                if (cr.getProvisionState() == null) {
                    continue;
                }

                if (cr.getProvisionState().equals(ProvisionStateEnumType.PROVISIONED)) {
                    String connId = cr.getConnectionId();
                    // log.info("---- will SETUP: "+connId);
                    OscarsProvQueue.getInstance().getInspect().put(connId, "SETUP");

                    ResvRecord rr = ConnectionRecord.getLatestResvRecord(cr);


                    if (rr.getStartTime().after(now)) {
                        // log.info("should start provisioning "+connId);
                    }
                }
            }
        }


    }



}
