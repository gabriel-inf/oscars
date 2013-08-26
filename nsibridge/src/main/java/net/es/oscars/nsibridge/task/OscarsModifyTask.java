package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.oscars.OscarsStateLogic;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;

public class OscarsModifyTask extends Task  {
    private static final Logger log = Logger.getLogger(OscarsModifyTask.class);

    private String connId = "";

    protected TimingConfig tc;
    public OscarsModifyTask(String connId) {
        this.scope = "oscars";
        this.connId = connId;
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
    }

    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            RequestHolder rh = RequestHolder.getInstance();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();

            ResvRequest req = rh.findResvRequest(connId);
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);


            Long now = new Date().getTime();
            super.onRun();
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                throw new TaskException("could not find connection entry for connId: "+connId);
            }

            // should we wait for OSCARS status to stabilize?
            boolean waitRequired = shouldWait(cr);

            double elapsed = 0;
            double timeout = tc.getModifyWaitTimeout() * 1000;

            // if yes, query every so often
            while (waitRequired && elapsed < timeout) {
                Double qi = tc.getQueryInterval() * 1000;
                Long sleep = qi.longValue();
                Thread.sleep(sleep);
                elapsed += qi;
                OscarsUtil.submitQuery(cr);
                waitRequired = shouldWait(cr);
            }
            // if we timed out waiting for a modify window, fail
            if (elapsed > timeout && waitRequired) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
            }



            // submit the modify


            boolean modifySubmittedOK = false;
            try {
                OscarsUtil.submitModify(req);
                modifySubmittedOK = true;
                log.debug("submitted OSCARS modify() for connId: "+connId);
            } catch (TranslationException ex) {
                log.error(ex);
            } catch (ServiceException ex) {
                log.error(ex);
            }

            if (!modifySubmittedOK) {
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


            elapsed = 0;
            timeout = tc.getOscarsResvTimeout() * 1000;

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
                // TODO: ensure the modify went through
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

        log.debug("OscarsModifyTask finishing for connId: "+connId);

        this.onSuccess();
    }


    private boolean shouldWait(ConnectionRecord cr) throws TaskException {
        boolean waitRequired = true;
        OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
        if        ( or.getStatus().equals("RESERVED") ||
                    or.getStatus().equals("ACTIVE")) {
            waitRequired = false;
        } else if ( or.getStatus().equals("INSETUP") ||
                    or.getStatus().equals("INTEARDOWN") ||
                    or.getStatus().equals("INPATHCALCULATION")  ) {
            waitRequired = true;
        } else if ( or.getStatus().equals("FAILED") ||
                    or.getStatus().equals("CANCELLED") ||
                    or.getStatus().equals("UNKNOWN")) {
            throw new TaskException("Unacceptable OSCARS status for modify");
        }
        return waitRequired;

    }
}
