package net.es.oscars.nsibridge.task;

import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.prov.TranslationException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;


public abstract class OscarsTask extends SMTask {
    private static final Logger log = Logger.getLogger(OscarsTask.class);

    protected OscarsOps oscarsOp;

    public OscarsOps getOscarsOp() {
        return oscarsOp;
    }

    public void setOscarsOp(OscarsOps oscarsOp) {
        this.oscarsOp = oscarsOp;
    }



    public void onRun() throws TaskException {

        String pre = "OscarsTask id "+this.getId()+" corrId: "+correlationId;

        log.debug(pre +" starting");

        RequestHolder rh = RequestHolder.getInstance();
        String connId = rh.findConnectionId(correlationId);
        if (connId == null) {
            rh.removeRequest(correlationId);
            throw new TaskException("unknown connectionId");
        }

        try {
            super.onRun();

            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr == null) {
                processFail(connId);
                this.onSuccess();
            }

            String oscarsGri = cr.getOscarsGri();
            if (oscarsGri == null) {
                log.error("no oscars GRI");
                processFail(connId);
                this.onSuccess();
            }
            OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
            if (or == null) {
                log.error("no oscars record");
                processFail(connId);
                this.onSuccess();
            }
            OscarsStates state = OscarsStates.valueOf(or.getStatus());
            if (state == null) {
                log.error("no oscars state");
                processFail(connId);
                this.onSuccess();
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
                    stateMachine.process(successEvent, correlationId);
                    NSI_Util.persistStateMachines(connId);
                    this.onSuccess();
                    return;
                case TIMED_OUT:
                    log.error("unexpected timeout");
                    processFail(connId);
                    return;
            }

            // if we actually need to perform the operation, perform it

            if (opAction.equals(OscarsLogicAction.YES)) {
                this.submitOscars(cr);

            // or, we need to wait
            } else if (opAction.equals(OscarsLogicAction.ASK_LATER)) {

                // until we know whether we are allowed to submit
                OscarsLogicAction allowed = OscarsUtil.pollUntilOpAllowed(oscarsOp, cr);
                switch (allowed) {
                    // should never return this
                    case ASK_LATER:
                        log.error("unexpected ask_later");
                        processFail(connId);
                        return;
                    // if we get this we can not proceed
                    case NO:
                        log.error("not allowed (after waiting)");
                        processFail(connId);
                        break;
                    // if we get this we can not proceed
                    case TIMED_OUT:
                        log.error("timed out");
                        processFail(connId);
                        return;
                    // only now can we try to submit
                    case YES:
                        break;
                }

                // check again if operation is needed
                or = ConnectionRecord.getLatestStatusRecord(cr);
                state = OscarsStates.valueOf(or.getStatus());

                opAction = OscarsStateLogic.isOperationNeeded(oscarsOp, state);
                switch (opAction) {
                    // submit
                    case YES:
                        this.submitOscars(cr);
                        break;
                    case NO:
                        stateMachine.process(successEvent, correlationId);
                        NSI_Util.persistStateMachines(connId);
                        this.onSuccess();
                        return;
                    // should never receive this
                    case ASK_LATER:
                        log.error("unexpected ask_later");
                        processFail(connId);
                        return;
                    // should never receive this
                    case TIMED_OUT:
                        log.error("unexpected timeout");
                        processFail(connId);
                        return;
                }
            }

            // wait until the reservation stabilizes

            OscarsStates os = OscarsUtil.pollUntilResvStable(cr);

            // check whether it succeeded
            OscarsLogicAction result = OscarsStateLogic.didOperationSucceed(oscarsOp, os);
            if (result.equals(OscarsLogicAction.YES)) {
                stateMachine.process(successEvent, correlationId);
                NSI_Util.persistStateMachines(connId);
                this.onSuccess();
                return;

            } else {
                processFail(connId);
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



    protected void processFail(String connId) throws StateException, ServiceException, TaskException {
        stateMachine.process(failEvent, correlationId);
        NSI_Util.persistStateMachines(connId);
        this.onSuccess();
    }


    public abstract void submitOscars(ConnectionRecord cr) throws ServiceException;

}
