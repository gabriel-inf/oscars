package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class OscarsResvOrModifyTask extends Task  {
    private static final Logger log = Logger.getLogger(OscarsResvOrModifyTask.class);

    private String corrId = "";

    protected TimingConfig tc;
    public OscarsResvOrModifyTask(String corrId) {
        this.scope = "oscars";
        this.corrId = corrId;
    }

    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {

            super.onRun();

            RequestHolder rh = RequestHolder.getInstance();
            ResvRequest req = rh.findResvRequest(corrId);
            String connId = req.getReserveType().getConnectionId();


            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);

            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                throw new TaskException("could not find connection entry for connId: "+connId);
            }

            OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);

            boolean newResvRequired = false;

            if (cr.getOscarsGri() == null) {
                newResvRequired = true;
            } else if (or == null || or.getStatus() == null) {
                newResvRequired = true;
            } else {
                OscarsStates state = OscarsStates.valueOf(or.getStatus());

                // depending on the state I will need to either modify or reserve
                OscarsLogicAction modAction = OscarsStateLogic.isOperationNeeded(OscarsOps.MODIFY, state);
                OscarsLogicAction resvAction = OscarsStateLogic.isOperationNeeded(OscarsOps.RESERVE, state);

                while (modAction.equals(OscarsLogicAction.ASK_LATER) || resvAction.equals(OscarsLogicAction.ASK_LATER)) {
                    Set<OscarsOps> ops = new HashSet<OscarsOps>();
                    ops.add(OscarsOps.MODIFY);
                    ops.add(OscarsOps.RESERVE);
                    try {
                        OscarsUtil.pollUntilAnOpAllowed(ops, cr);
                    } catch (ServiceException ex) {
                        log.error(ex);
                        try {
                            NSI_Resv_SM.handleEvent(connId, NSI_Resv_Event.LOCAL_RESV_CHECK_FL);


                        } catch (StateException ex1) {
                            log.error(ex1);
                        }

                    }
                    or = ConnectionRecord.getLatestStatusRecord(cr);
                    state = OscarsStates.valueOf(or.getStatus());

                    modAction = OscarsStateLogic.isOperationNeeded(OscarsOps.MODIFY, state);
                    resvAction = OscarsStateLogic.isOperationNeeded(OscarsOps.RESERVE, state);
                    if (resvAction.equals(OscarsLogicAction.YES)) {
                        newResvRequired = true;
                    }
                }
            }

            OscarsOps theOp = OscarsOps.MODIFY;
            if (newResvRequired) {
                theOp = OscarsOps.RESERVE;
            }
            req.setOscarsOp(theOp);
            log.debug("oscars op:"+theOp);


            OscarsLogicAction action;
            try {
                action = OscarsUtil.pollUntilOpAllowed(theOp, cr);
            } catch (ServiceException ex) {
                log.error(ex);
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                this.onSuccess();
                return;
            }

            // if we still cannot perform the operation, fail
            if (!action.equals(OscarsLogicAction.YES)) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                this.onSuccess();
                return;
            }


            // submit the modify
            boolean submittedOK = false;
            try {
                if (theOp.equals(OscarsOps.MODIFY)) {
                    log.debug("submitting OSCARS modify() for connId: "+connId);
                    OscarsUtil.submitModify(req);
                } else {
                    log.debug("submitting OSCARS create() for connId: "+connId);
                    OscarsUtil.submitResv(req);
                }
                submittedOK = true;
            } catch (TranslationException ex) {
                log.error(ex);
            } catch (ServiceException ex) {
                log.error(ex);
            }

            if (!submittedOK) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                this.onSuccess();
                return;
            }

            try {
                OscarsStates os = OscarsUtil.pollUntilResvStable(cr);
                if (os.equals(OscarsStates.RESERVED)) {
                    rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
                    this.onSuccess();
                    return;
                }
            } catch (ServiceException ex) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                this.onSuccess();
                return;
            }

            NSI_Util.persistStateMachines(connId);


        } catch (StateException ex) {
            ex.printStackTrace();
            this.onFail();
        } catch (ServiceException ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug("OscarsResvOrModifyTask finishing for corrId: "+corrId);

        this.onSuccess();
    }


}
