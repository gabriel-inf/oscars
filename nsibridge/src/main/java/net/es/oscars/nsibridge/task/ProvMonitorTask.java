package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ProvMonitorTask extends Task  {
    private static final Logger log = Logger.getLogger(ProvMonitorTask.class);

    public ProvMonitorTask() {
        this.scope = "nsi";
    }

    public void onRun() throws TaskException {

        try {
            EntityManager em = PersistenceHolder.getEntityManager();
            super.onRun();
            log.debug(this.id + " starting");

            em.getTransaction().begin();
            Date now = new Date();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();


            String query = "SELECT c FROM ConnectionRecord c";
            List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
            em.getTransaction().commit();
            for (ConnectionRecord cr: recordList) {
                // do not
                if (cr.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                    if (cr.getProvisionState().equals(ProvisionStateEnumType.PROVISIONING)) {
                        String connId = cr.getConnectionId();
                        ResvRecord rr = ConnectionRecord.getLatestResvRecord(cr);
                        if (rr.getStartTime().after(now)) {
                            log.info("should start provisioning "+connId);

                        }
                    }
                }
            }



        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }



}
