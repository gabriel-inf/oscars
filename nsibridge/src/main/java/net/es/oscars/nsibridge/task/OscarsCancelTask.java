package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.SpringContext;
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

public class OscarsCancelTask extends Task  {

    private String corrId = "";
    private static final Logger log = Logger.getLogger(OscarsCancelTask.class);
    protected TimingConfig tc;
    private SimpleRequestType type;

    public OscarsCancelTask(String corrId, SimpleRequestType type) {
        this.corrId = corrId;
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);

        this.scope = "oscars";
    }


    public void onRun() throws TaskException {
        String pre = "OscarsCancel job id "+this.getId()+" corrId: "+corrId;

        log.debug(pre +" starting");
        try {
            super.onRun();

            RequestHolder rh = RequestHolder.getInstance();

            SimpleRequest req = rh.getSimpleRequests().get(corrId);

            if (req != null) {
                log.debug("found request for corrId: "+corrId);
            } else {
                throw new TaskException("could not find request");
            }

            String connId = req.getConnectionId();

            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);

            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr == null) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                this.onSuccess();
            }

            String oscarsGri = cr.getOscarsGri();
            if (oscarsGri == null) {
                throw new TaskException("unknown OSCARS GRI");
            }
            OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
            if (or == null) {
                throw new TaskException("no OSCARS record");
            }
            OscarsStates state = OscarsStates.valueOf(or.getStatus());
            if (state == null) {
                throw new TaskException("unknown OSCARS state");
            }
            OscarsOps op = OscarsOps.CANCEL;

            OscarsLogicAction cancelAction = OscarsStateLogic.isOperationNeeded(op, state);
            boolean cancelRequired = false;
            while (cancelAction.equals(OscarsLogicAction.ASK_LATER)) {
                OscarsUtil.pollUntilOpAllowed(op, cr);

                or = ConnectionRecord.getLatestStatusRecord(cr);
                state = OscarsStates.valueOf(or.getStatus());

                cancelAction = OscarsStateLogic.isOperationNeeded(op, state);
                if (cancelAction.equals(OscarsLogicAction.YES)) {
                    cancelRequired = true;
                }
            }


            OscarsOps theOp = OscarsOps.CANCEL;

            OscarsLogicAction action;
            try {
                action = OscarsUtil.pollUntilOpAllowed(theOp, cr);
            } catch (ServiceException ex) {
                log.error(ex);
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                this.onSuccess();
                return;
            }

            // if we still cannot perform the operation, fail
            if (!action.equals(OscarsLogicAction.YES)) {
                log.error("timed out waiting ");
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                this.onSuccess();
                return;
            }


            // submit the modify
            boolean submittedOK = false;
            try {
                log.debug("submitting OSCARS create() for connId: "+connId);
                OscarsUtil.submitCancel(cr);
                submittedOK = true;
            } catch (ServiceException ex) {
                log.error(ex);
            }

            if (!submittedOK) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                this.onSuccess();
                return;
            }

            try {
                OscarsStates os = OscarsUtil.pollUntilResvStable(cr);
                if (os.equals(OscarsStates.CANCELLED)) {
                    rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_CF);
                    this.onSuccess();
                    return;
                }
            } catch (ServiceException ex) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
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

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
