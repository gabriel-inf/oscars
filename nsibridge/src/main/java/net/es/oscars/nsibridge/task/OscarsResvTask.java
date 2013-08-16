package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.*;

import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.nsibridge.beans.ResvRequest;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;

public class OscarsResvTask extends Task  {
    private static final Logger log = Logger.getLogger(OscarsResvTask.class);

    private String connId = "";

    public OscarsResvTask(String connId) {
        this.scope = "oscars";
        this.connId = connId;
    }

    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            super.onRun();
            EntityManager em = PersistenceHolder.getInstance().getEntityManager();
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                throw new TaskException("could not find connection entry for connId: "+connId);
            }



            RequestHolder rh = RequestHolder.getInstance();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();


            ResvRequest req = rh.findResvRequest(connId);
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);


            if (req != null) {
                log.debug("found request for connId: "+connId);
            }

            if (rsm != null) {
                log.debug("found state machine for connId: "+connId);
            }

            ResCreateContent rc = null;
            try {
                rc = NSI_OSCARS_Translation.makeOscarsResv(req);
            } catch (TranslationException ex) {
                log.debug(ex);
                log.debug("could not translate NSI request");

                try {
                    rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                } catch (StateException e1) {
                    ex.printStackTrace();
                    e1.printStackTrace();
                }

            }
            if (rc != null) {
                try {
                    CreateReply reply = OscarsProxy.getInstance().sendCreate(rc);
                    log.debug("connId: "+connId+"gri: "+reply.getGlobalReservationId());

                    em.getTransaction().begin();
                    cr.setOscarsGri(reply.getGlobalReservationId());
                    em.persist(cr);
                    em.getTransaction().commit();

                    if (reply.getStatus().equals("FAILED")) {
                        rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                    } else {
                        rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
                    }
                } catch (OSCARSServiceException e) {
                    try {
                        rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                    } catch (StateException e1) {
                        e.printStackTrace();
                        e1.printStackTrace();
                    }

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
