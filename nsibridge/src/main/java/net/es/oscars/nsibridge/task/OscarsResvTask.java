package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import net.es.oscars.nsibridge.prov.*;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
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

            boolean newResvSubmittedOK = false;
            try {
                OscarsUtil.submitResv(req);
                newResvSubmittedOK = true;
            } catch (TranslationException ex) {
                log.error(ex);
            } catch (ServiceException ex) {
                log.error(ex);
            }
            if (!newResvSubmittedOK) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
            }



            // TODO: query
            boolean localConfirmed = true;







            if (localConfirmed) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
            } else {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);

            }


        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
