package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.oscars.OscarsStateLogic;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import net.es.oscars.nsibridge.prov.*;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.nsibridge.beans.ResvRequest;
import org.apache.log4j.Logger;


public class OscarsResvTask extends Task  {
    private static final Logger log = Logger.getLogger(OscarsResvTask.class);
    protected TimingConfig tc;

    private String connId = "";

    public OscarsResvTask(String connId) {
        this.scope = "oscars";
        this.connId = connId;
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
    }

    public void onRun() throws TaskException {
        log.debug("OscarsResvTask for connId: "+connId+" starting");
        try {
            super.onRun();
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
                log.debug("submitted OSCARS create() for connId: "+connId);
            } catch (TranslationException ex) {
                log.error(ex);
            } catch (ServiceException ex) {
                log.error(ex);
            }

            if (!newResvSubmittedOK) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                rh.removeResvRequest(connId);
                this.onSuccess();
                return;
            }

            boolean localConfirmed = false;
            boolean localDecided = false;
            OscarsStates os = OscarsStates.FAILED;

            // wait until we can query
            Double d = tc.getQueryAfterResvWait() * 1000;
            Long sleep = d.longValue();
            Thread.sleep(sleep);


            double elapsed = 0;
            double timeout = tc.getOscarsResvTimeout() * 1000;

            while (!localDecided && timeout > elapsed) {
                OscarsUtil.submitQuery(cr);
                OscarsStatusRecord oc = ConnectionRecord.getLatestStatusRecord(cr);
                if (oc != null && oc.getStatus() != null) {
                    os = OscarsStates.valueOf(oc.getStatus());
                    if (OscarsStateLogic.isStateSteady(os)) {
                        localDecided = true;
                    }
                }
                if (!localDecided) {
                    Double qi  = tc.getQueryInterval() * 1000;
                    sleep = qi.longValue();
                    Thread.sleep(sleep);
                    elapsed += qi;
                }
            }
            // timed out waiting
            if (elapsed > timeout && !localDecided) {
                localConfirmed = false;
            } else if (os.equals(OscarsStates.RESERVED)) {
                localConfirmed = true;
            }

            if (localConfirmed) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
            } else {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);

            }
            NSI_Util.persistStateMachines(connId);

            rh.removeResvRequest(connId);

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug("OscarsResvTask finishing for connId: "+connId);

        this.onSuccess();
    }

}
