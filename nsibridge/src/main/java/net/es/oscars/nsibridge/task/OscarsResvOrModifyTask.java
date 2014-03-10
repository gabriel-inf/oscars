package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.utils.task.RunState;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OscarsResvOrModifyTask extends OscarsTask  {
    private static final Logger log = Logger.getLogger(OscarsResvOrModifyTask.class);

    protected TimingConfig tc;
    public OscarsResvOrModifyTask() {
        this.scope = "oscars";
    }

    public void submitOscars(ConnectionRecord cr) {

    }

    @Override
    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        String exceptionString = "";
        try {
            this.runstate = RunState.RUNNING;
            this.getTimeline().setStarted(new Date().getTime());

            RequestHolder rh = RequestHolder.getInstance();
            ResvRequest req = rh.findResvRequest(this.correlationId);
            if (req == null) {
                throw new TaskException("could not locate resvRequest for corrId:"+correlationId);
            }
            String connId = req.getReserveType().getConnectionId();


            ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                exceptionString = "could not find connection entry for connId: "+connId;
                log.error(exceptionString);
                throw new TaskException(exceptionString);
            }

            OscarsStatusRecord or = cr.getOscarsStatusRecord();

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
                    exceptionString = "";
                    Set<OscarsOps> ops = new HashSet<OscarsOps>();
                    ops.add(OscarsOps.MODIFY);
                    ops.add(OscarsOps.RESERVE);
                    try {
                        OscarsUtil.pollUntilAnOpAllowed(ops, cr, this.id);
                    } catch (TranslationException ex) {
                        exceptionString += ex.toString();
                        log.error(ex);
                        try {
                            DB_Util.saveException(connId, correlationId, exceptionString);
                            this.getStateMachine().process(this.failEvent, this.correlationId);
                            DB_Util.persistStateMachines(connId);
                            return;
                        } catch (ServiceException ex1) {
                            log.error(ex1);
                            return;
                        } catch (StateException ex1) {
                            log.error(ex1);
                            return;
                        }
                    }
                    or = cr.getOscarsStatusRecord();
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
                action = OscarsUtil.pollUntilOpAllowed(theOp, cr, this.id);
            } catch (TranslationException ex) {
                log.error(ex);
                this.getStateMachine().process(this.failEvent, this.correlationId);
                DB_Util.persistStateMachines(connId);
                exceptionString = ex.toString();
                DB_Util.saveException(connId, correlationId, exceptionString);

                this.onSuccess();
                return;
            }

            // if we still cannot perform the operation, fail
            if (!action.equals(OscarsLogicAction.YES)) {
                this.getStateMachine().process(this.failEvent, this.correlationId);
                DB_Util.persistStateMachines(connId);
                this.onSuccess();
                exceptionString = "could not perform operation after trying for a while";
                DB_Util.saveException(connId, correlationId, exceptionString);
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
                exceptionString = ex.toString();
                log.error(ex);
            } catch (ServiceException ex) {
                exceptionString = ex.toString();
                log.error(ex);
            }

            if (!submittedOK) {
                this.getStateMachine().process(this.failEvent, this.correlationId);
                DB_Util.saveException(connId, correlationId, exceptionString);
                DB_Util.persistStateMachines(connId);
                this.onSuccess();
                return;
            }

            try {
                OscarsStates os = OscarsUtil.pollUntilResvStable(cr);

                if (os.equals(OscarsStates.RESERVED) || os.equals(OscarsStates.ACTIVE)) {
                    this.getStateMachine().process(this.successEvent, this.correlationId);
                    this.onSuccess();
                    DB_Util.persistStateMachines(connId);


                    if (theOp.equals(OscarsOps.MODIFY)) {

                        List<ResvRecord> rrs = ConnectionRecord.getUncommittedResvRecords(cr);
                        ResvRecord rr = rrs.get(0);
                        if (rr != null) {
                            DB_Util.updateDataplaneRecord(cr, os, rr.getVersion());
                        } else {
                            DB_Util.updateDataplaneRecord(cr, os, 0);
                        }
                    } else {
                        ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
                        if (rr != null) {
                            DB_Util.updateDataplaneRecord(cr, os, rr.getVersion());
                        } else {
                            DB_Util.updateDataplaneRecord(cr, os, 0);
                        }
                    }






                    if (theOp.equals(OscarsOps.MODIFY) && os.equals(OscarsStates.ACTIVE)) {
                        // send a dataplane update back
                        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
                        NSI_Prov_SM psm = smh.findNsiProvSM(connId);
                        NSI_Prov_TH pth = (NSI_Prov_TH) psm.getTransitionHandler();
                        NsiProvMdl mdl = pth.getMdl();
                        mdl.dataplaneUpdate(correlationId);
                    }
                    return;
                }  else {
                    this.getStateMachine().process(this.failEvent, this.correlationId);
                    this.onSuccess();
                    DB_Util.persistStateMachines(connId);
                    return;
                }
            } catch (ServiceException ex) {
                this.getStateMachine().process(this.failEvent, this.correlationId);
                this.onSuccess();
                DB_Util.persistStateMachines(connId);
                exceptionString = ex.toString();
                DB_Util.saveException(connId, correlationId, exceptionString);
                return;
            }



        } catch (StateException ex) {
            ex.printStackTrace();
            this.onFail();
        } catch (ServiceException ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug("OscarsResvOrModifyTask finishing for corrId: "+this.correlationId);

        this.onSuccess();
    }


}
