package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.prov.TranslationException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;


public abstract class OscarsTask extends SMTask {
    private static final Logger log = Logger.getLogger(OscarsTask.class);

    protected OscarsOps oscarsOp = null;

    public OscarsOps getOscarsOp() {
        return oscarsOp;
    }

    public void setOscarsOp(OscarsOps oscarsOp) {
        this.oscarsOp = oscarsOp;
    }



    public void onRun() throws TaskException {

        String pre = "OscarsTask id "+this.getId()+" corrId: "+correlationId;

        log.debug(pre +" starting");


        try {
            super.onRun();

            ConnectionRecord cr = DB_Util.getConnectionRecord(connectionId);
            if (cr == null) {
                processFail();
                this.onSuccess();
                return;
            }

            String oscarsGri = cr.getOscarsGri();
            if (oscarsGri == null) {
                processFail(connectionId);
                this.onSuccess();
                return;
            }
            OscarsStatusRecord or = cr.getOscarsStatusRecord();
            if (or == null) {
                processFail(connectionId, "no oscars record");
                this.onSuccess();
                return;
            }
            OscarsStates state = OscarsStates.valueOf(or.getStatus());
            if (state == null) {
                processFail(connectionId, "no oscars state");
                this.onSuccess();
                return;
            }


            // do not need to perform the action if it is not needed for this state
            // ie no need to setup if already active
            // in this case, we have succeeded
            OscarsLogicAction opAction = OscarsStateLogic.isOperationNeeded(oscarsOp, state);
            switch (opAction) {
                case YES:
                    break;
                case ASK_LATER:
                    break;
                case NO:
                    this.getStateMachine().process(successEvent, correlationId);
                    DB_Util.persistStateMachines(connectionId);
                    this.onSuccess();
                    return;
                case TIMED_OUT:
                    processFail(connectionId, "unexpected timeout");
                    return;
            }

            // if we actually need to perform the operation, perform it

            if (opAction.equals(OscarsLogicAction.YES)) {
                this.submitOscars(cr);

            // or, we need to wait
            } else if (opAction.equals(OscarsLogicAction.ASK_LATER)) {

                // until we know whether we are allowed to submit
                OscarsLogicAction allowed = OscarsUtil.pollUntilOpAllowed(oscarsOp, cr, this.id);
                switch (allowed) {
                    // should never return this
                    case ASK_LATER:
                        processFail(connectionId, "unexpected ask_later");
                        return;
                    // if we get this we can not proceed
                    case NO:
                        processFail(connectionId, "not allowed (after waiting)");
                        break;
                    // if we get this we can not proceed
                    case TIMED_OUT:
                        processFail(connectionId, "timed out");
                        return;
                    // only now can we try to submit
                    case YES:
                        break;
                }

                // check again if operation is needed
                or = cr.getOscarsStatusRecord();
                state = OscarsStates.valueOf(or.getStatus());

                opAction = OscarsStateLogic.isOperationNeeded(oscarsOp, state);
                switch (opAction) {
                    // submit
                    case YES:
                        this.submitOscars(cr);
                        break;
                    case NO:
                        this.getStateMachine().process(successEvent, correlationId);
                        DB_Util.persistStateMachines(connectionId);
                        this.onSuccess();
                        return;
                    // should never receive this
                    case ASK_LATER:
                        processFail(connectionId, "unexpected ask_later");
                        return;
                    // should never receive this
                    case TIMED_OUT:
                        processFail(connectionId, "unexpected timeout");
                        return;
                }
            }

            // wait until the reservation stabilizes
            OscarsStates os = OscarsUtil.pollUntilResvStable(cr);

            // check whether it succeeded
            OscarsLogicAction result = OscarsStateLogic.didOperationSucceed(oscarsOp, os);
            if (result.equals(OscarsLogicAction.YES)) {
                this.getStateMachine().process(successEvent, correlationId);
                DB_Util.persistStateMachines(connectionId);
                this.onSuccess();
                return;

            } else {
                processFail(connectionId, "operation failed");
                return;
            }

        } catch (TranslationException ex) {
            ex.printStackTrace();
            this.onFail();


        } catch (StateException ex) {
            ex.printStackTrace();
            this.onFail();
        } catch (ServiceException ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }




    protected void processFail(String connId, String exceptionString) throws StateException, ServiceException, TaskException {
        log.error(exceptionString);
        DB_Util.saveException(connId, correlationId, exceptionString);
        this.processFail(connId);
    }


    protected void processFail(String connId) throws StateException, ServiceException, TaskException {
        this.processFail();
        DB_Util.persistStateMachines(connId);
    }


    protected void processFail() throws StateException, ServiceException, TaskException {
        this.getStateMachine().process(failEvent, correlationId);
        this.onSuccess();
    }

    public abstract void submitOscars(ConnectionRecord cr) throws ServiceException;

}
