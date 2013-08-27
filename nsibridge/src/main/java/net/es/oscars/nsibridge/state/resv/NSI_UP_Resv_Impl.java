package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiResvMdl;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.task.*;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Set;
import java.util.UUID;


public class NSI_UP_Resv_Impl implements NsiResvMdl {
    protected String connectionId = "";
    protected TimingConfig tc;
    private static final Logger LOG = Logger.getLogger(NSI_UP_Resv_Impl.class);


    public NSI_UP_Resv_Impl(String connId) {
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        connectionId = connId;
    }

    @Override
    public UUID localRollback() {
        // TODO
        return null;
    }


    @Override
    public UUID localCheck() {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;


        ConnectionRecord cr = null;
        try {
            cr = NSI_Util.getConnectionRecord(connectionId);
        } catch (ServiceException e) {
            try {
                NSI_Resv_SM.handleEvent(connectionId, NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
            } catch (StateException e1) {
                e1.printStackTrace();
            }
        }



        boolean newResvRequired = false;
        boolean waitRequired = false;
        if (cr.getOscarsGri() == null) {
            newResvRequired = true;
        } else if (ConnectionRecord.getLatestStatusRecord(cr) != null) {
            OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
            if (or.getStatus().equals("RESERVED") || or.getStatus().equals("ACTIVE")) {
                newResvRequired = false;
                waitRequired = false;
            } else if (or.getStatus().equals("FAILED") ||
                       or.getStatus().equals("CANCELLED") ||
                       or.getStatus().equals("UNKNOWN")) {
                newResvRequired = true;
                waitRequired = false;
            } else if (or.getStatus().equals("INSETUP") ||
                       or.getStatus().equals("INTEARDOWN") ||
                       or.getStatus().equals("INPATHCALCULATION")  ) {
                waitRequired = true;
            }
        }

        if (newResvRequired) {
            // submit the oscars create()
            Double d = (tc.getTaskInterval() * 1000);
            Long when = now + d.longValue();
            Task oscarsResv = new OscarsResvTask(connectionId);
            try {
                LOG.info("scheduling oscarsResv for connId:"+connectionId+", will run in "+d+"ms");
                taskId = wf.schedule(oscarsResv, when);

            } catch (TaskException e) {
                LOG.error(e);
                NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
                NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);
                try {
                    rsm.process(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                } catch (StateException ex) {
                    ex.printStackTrace();
                }
            }

        } else {
            if (waitRequired) {

            }
        }
        return taskId;
    }

    @Override
    public UUID localHold() {
        UUID taskId = null;


        LOG.debug("localHold: "+connectionId);
        // nothing to do - everything is done in the check phase
        return taskId;
    }

    @Override
    public UUID localCommit() {
        UUID taskId = null;

        LOG.debug("localCommit: "+connectionId);
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);


        try {
            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.LOCAL_RESV_COMMIT_CF);
            taskId = taskIds.iterator().next();
            NSI_Util.persistStateMachines(connectionId);
        }catch (StateException ex) {
            LOG.error(ex);
        }catch (ServiceException ex) {
            LOG.error(ex);
        }


        return taskId;
    }

    @Override
    public UUID localAbort() {

        // TODO : schedule just one job!
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        // submit the oscars cancel()
        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        Task oscarsCancel = new OscarsCancelTask(connectionId);
        try {
            wf.schedule(oscarsCancel, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        // query to see what happened
        d = (tc.getQueryAfterResvWait() * 1000);
        when = now + d.longValue();
        OscarsQueryTask oqt = new OscarsQueryTask(connectionId);
        try {
            taskId = wf.schedule(oqt , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        // examine the results of the query()
        d = ((tc.getQueryAfterResvWait() + tc.getQueryResultDelay()) * 1000);
        when = now + d.longValue();
        LocalResvTask lrt = new LocalResvTask(connectionId, NSI_Resv_Event.LOCAL_RESV_ABORT_CF);
        try {
            wf.schedule(lrt , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }


        return taskId;
    }

    @Override
    public UUID sendRsvCF() {
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvFL() {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_FL);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    @Override
    public UUID sendRsvCmtCF() {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_CM_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvCmtFL() {
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_CM_FL);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvAbtCF() {
        UUID taskId = null;
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_AB_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvTimeout() {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_TIMEOUT);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }




}
