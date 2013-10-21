package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.TranslationException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.utils.task.RunState;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.Date;

public class OscarsCancelOrModifyTask extends OscarsTask  {
    private static final Logger log = Logger.getLogger(OscarsCancelOrModifyTask.class);

    protected TimingConfig tc;
    public OscarsCancelOrModifyTask() {
        this.scope = "oscars";
    }

    public void submitOscars(ConnectionRecord cr) {

    }

    @Override
    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        String exString = "";
        try {
            this.runstate = RunState.RUNNING;
            this.getTimeline().setStarted(new Date().getTime());

            String connId = this.getConnectionId();

            ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                exString = "could not find connection entry for connId: "+connId;
                log.error(exString);
                throw new TaskException(exString);
            }

            // first, remove any existing uncommitted reservation records
            try {
                DB_Util.abortResvRecord(connectionId);
            } catch (ServiceException ex) {
                exString = ex.toString();
                log.error(exString, ex);
            }


            OscarsOps theOp = OscarsOps.CANCEL;
            if (this.getOscarsOp() != null) {
                theOp = this.getOscarsOp();
            } else {
                // default action is to cancel
                // but, if there was a previous committed reservation record, we need to modify
                // the OSCARS reservation back to it
                ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
                if (rr != null) {
                    theOp = OscarsOps.MODIFY;
                }
            }


            OscarsLogicAction action;

            exString = "";
            try {
                action = OscarsUtil.pollUntilOpAllowed(theOp, cr, this.id);
            } catch (TranslationException ex) {
                exString += ex.toString();
                log.error(ex);
                try {
                    DB_Util.saveException(connId, correlationId, exString);
                    this.onFail();
                    return;
                } catch (ServiceException ex1) {
                    log.error(ex1);
                    return;
                }
            }

            // if we still cannot perform the operation, fail
            if (!action.equals(OscarsLogicAction.YES)) {
                this.onFail();
                exString = "could not perform operation after trying for a while";
                DB_Util.saveException(connId, correlationId, exString);
                return;
            }

            boolean submittedOK = false;
            try {
                if (theOp.equals(OscarsOps.MODIFY)) {
                    log.debug("submitting OSCARS modify() for connId: "+connId);
                    OscarsUtil.submitRollback(cr);
                } else {
                    log.debug("submitting OSCARS cancel() for connId: "+connId);
                    OscarsUtil.submitCancel(cr);
                }
                submittedOK = true;
            } catch (TranslationException ex) {
                exString = ex.toString();
                log.error(exString, ex);
            } catch (ServiceException ex) {
                exString = ex.toString();
                log.error(exString, ex);
            }

            if (!submittedOK) {
                this.onFail();
                return;
            }

            OscarsStates os;
            try {
                os = OscarsUtil.pollUntilResvStable(cr);
            } catch (Exception ex) {
                this.onFail();
                return;
            }

            DB_Util.updateDataplaneRecord(cr, os);


            switch (theOp) {
                case MODIFY:
                    if (os.equals(OscarsStates.ACTIVE) || os.equals(OscarsStates.RESERVED)) {
                        this.onSuccess();
                    } else {
                        this.onFail();
                    }
                    return;
                case CANCEL:
                    if (os.equals(OscarsStates.CANCELLED)) {
                        this.onSuccess();
                    } else {
                        this.onFail();
                    }
                    return;
                default:
                    exString = "bad op!";
                    log.error(exString);
                    this.onFail();
                    return;
            }

        } catch (ServiceException ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug("OscarsResvOrModifyTask finishing for corrId: "+this.correlationId);

        this.onSuccess();
    }


}
