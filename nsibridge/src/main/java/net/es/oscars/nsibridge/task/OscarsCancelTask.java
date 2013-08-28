package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
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

public class OscarsCancelTask extends Task  {

    private String connId = "";
    private static final Logger log = Logger.getLogger(OscarsCancelTask.class);
    protected TimingConfig tc;
    private SimpleRequestType type;

    public OscarsCancelTask(String connId, SimpleRequestType type) {
        this.connId = connId;
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        this.type = type;

        this.scope = "oscars";
    }


    public void onRun() throws TaskException {
        String pre = "OscarsCancel job id "+this.getId()+" connId: "+connId;

        log.debug(pre +" starting");
        try {
            super.onRun();

            RequestHolder rh = RequestHolder.getInstance();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);

            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr == null) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                rh.removeSimpleRequest(connId, type);
                this.onSuccess();
            }




            SimpleRequest req = rh.findSimpleRequest(connId, type);
            String oscarsGri = cr.getOscarsGri();


            if (req != null) {
                log.debug("found request for connId: "+connId);
            }




            double elapsed = 0;
            double timeout = tc.getOscarsResvTimeout() * 1000;
            Long sleep;

            boolean cancelRequired = true;
            boolean cancelNeedDecided = false;

            while (!cancelNeedDecided && timeout > elapsed) {
                if (ConnectionRecord.getLatestStatusRecord(cr) != null) {
                    OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
                    if (or.getStatus().equals("RESERVED") || or.getStatus().equals("ACTIVE")) {
                        cancelRequired = true;
                        cancelNeedDecided = true;
                    } else if (or.getStatus().equals("FAILED") ||
                            or.getStatus().equals("CANCELLED") ||
                            or.getStatus().equals("UNKNOWN")) {
                        cancelNeedDecided = true;
                        cancelRequired = false;
                    } else if (or.getStatus().equals("INSETUP") ||
                            or.getStatus().equals("INTEARDOWN") ||
                            or.getStatus().equals("INPATHCALCULATION")  ) {
                        cancelNeedDecided = false;
                    } else {
                        cancelNeedDecided = false;
                    }
                }
                OscarsUtil.submitQuery(cr);

                if (!cancelNeedDecided) {
                    Double qi  = tc.getQueryInterval() * 1000;
                    sleep = qi.longValue();
                    log.debug(pre + " not decided yet, waiting "+sleep+" ms to query again");

                    Thread.sleep(sleep);
                    elapsed += qi;
                }
            }

            if (elapsed > timeout && !cancelNeedDecided) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                rh.removeSimpleRequest(connId, type);
                this.onSuccess();
                return;
            } else if (!cancelRequired) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_CF);
                rh.removeSimpleRequest(connId, type);
                this.onSuccess();
                return;
            }

            CancelResContent rc = NSI_OSCARS_Translation.makeOscarsCancel(oscarsGri);
            boolean cancelSubmittedOK = false;


            try {
                log.debug(pre + " submitting OSCARS cancel()");
                CancelResReply reply = OscarsProxy.getInstance().sendCancel(rc);
                log.debug(pre + " submitted OSCARS cancel()");
                cancelSubmittedOK = true;
            } catch (OSCARSServiceException e) {
                log.error(e);
            }

            if (!cancelSubmittedOK) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                rh.removeSimpleRequest(connId, type);
                this.onSuccess();
                return;
            }

            boolean localDecided = false;
            OscarsStates os = OscarsStates.FAILED;

            // wait until we can query
            Double d = tc.getQueryAfterCancelWait() * 1000;
            sleep = d.longValue();
            log.debug(pre + " waiting "+sleep+" ms until we can query");
            Thread.sleep(sleep);


            elapsed = 0;
            timeout = tc.getOscarsResvTimeout() * 1000;

            while (!localDecided && timeout > elapsed) {
                OscarsUtil.submitQuery(cr);
                OscarsStatusRecord oc = ConnectionRecord.getLatestStatusRecord(cr);
                if (oc != null && oc.getStatus() != null) {
                    os = OscarsStates.valueOf(oc.getStatus());
                    log.debug(pre + " oscars latest state: "+oc.getStatus()+" query again");

                    if (OscarsStateLogic.isStateSteady(os)) {
                        localDecided = true;
                    }
                }
                if (!localDecided) {
                    Double qi  = tc.getQueryInterval() * 1000;
                    sleep = qi.longValue();
                    log.debug(pre + " not decided yet, waiting "+sleep+" ms to query again");

                    Thread.sleep(sleep);
                    elapsed += qi;
                }
            }

            boolean localCancelConfirmed = false;

            // timed out waiting
            if (elapsed > timeout && !localDecided) {
                localCancelConfirmed = false;
            } else if (os.equals(OscarsStates.CANCELLED)) {
                localCancelConfirmed = true;
            }

            if (localCancelConfirmed) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_CF);
            } else {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
            }

            rh.removeSimpleRequest(connId, type);

            NSI_Util.persistStateMachines(connId);


        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
