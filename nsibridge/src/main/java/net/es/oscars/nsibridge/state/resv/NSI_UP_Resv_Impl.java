package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiResvMdl;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.task.*;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class NSI_UP_Resv_Impl implements NsiResvMdl {
    protected String connectionId = "";
    protected TimingConfig tc;
    private static final Logger log = Logger.getLogger(NSI_UP_Resv_Impl.class);


    public NSI_UP_Resv_Impl(String connId) {
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        connectionId = connId;
    }

    @Override
    public UUID localRollback(String correlationId) {
        // TODO
        return null;
    }


    @Override
    public UUID localCheck(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;


        ConnectionRecord cr = null;
        try {
            NSI_Util.isConnectionOK(connectionId);
            cr = NSI_Util.getConnectionRecord(connectionId);
        } catch (ServiceException e) {
            try {
                NSI_Resv_SM.handleEvent(connectionId, correlationId, NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
            } catch (StateException e1) {
                e1.printStackTrace();
            }
        }

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);

        RequestHolder rh = RequestHolder.getInstance();


        OscarsResvOrModifyTask ost = new OscarsResvOrModifyTask();
        ost.setCorrelationId(correlationId);
        ost.setSuccessEvent(NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
        ost.setFailEvent(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
        ost.setStateMachine(rsm);
        ost.setOscarsOp(OscarsOps.RESERVE);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        log.info("scheduling Modify / Resv for connId:" + connectionId + ", will run in " + d + "ms");
        try {
            taskId = wf.schedule(ost, when);
            log.info("task id: "+taskId);

        } catch (TaskException e) {
            log.error(e);
            try {
                NSI_Resv_SM.handleEvent(connectionId, correlationId, NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                rh.removeResvRequest(connectionId);
            } catch (StateException ex) {
                ex.printStackTrace();
            }
        }

        return taskId;
    }

    @Override
    public UUID localHold(String correlationId) {
        UUID taskId = null;


        log.debug("localHold: " + connectionId);
        // nothing to do - everything is done in the check phase
        return taskId;
    }

    @Override
    public UUID localCommit(String correlationId) {
        UUID taskId = null;

        log.debug("localCommit: " + connectionId);
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);


        try {
            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.LOCAL_RESV_COMMIT_CF, correlationId);
            taskId = taskIds.iterator().next();
            NSI_Util.persistStateMachines(connectionId);
        }catch (StateException ex) {
            log.error(ex);
        }catch (ServiceException ex) {
            log.error(ex);
        }


        return taskId;
    }

    @Override
    public UUID localAbort(String correlationId) {

        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        boolean recordOK = true;

        ConnectionRecord cr = null;
        try {
            NSI_Util.isConnectionOK(connectionId);
            cr = NSI_Util.getConnectionRecord(connectionId);
            if (cr.getOscarsGri() == null) {
                recordOK = false;
            }
        } catch (ServiceException e) {
            e.printStackTrace();
            recordOK = false;
        }

        if (!recordOK) {
            try {
                NSI_Resv_SM.handleEvent(connectionId, correlationId, NSI_Resv_Event.LOCAL_RESV_ABORT_FL);
                return null;
            } catch (StateException e) {
                e.printStackTrace();
            }
        }

        // submit the oscars cancel()
        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.findNsiResvSM(connectionId);

        OscarsCancelTask ost = new OscarsCancelTask();
        ost.setCorrelationId(correlationId);
        ost.setOscarsOp(OscarsOps.CANCEL);
        ost.setStateMachine(rsm);
        ost.setSuccessEvent(NSI_Resv_Event.LOCAL_RESV_ABORT_CF);
        ost.setFailEvent(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);

        try {
            taskId = wf.schedule(ost, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        return taskId;
    }

    @Override
    public UUID sendRsvCF(String correlationId) {
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(correlationId, CallbackMessages.RESV_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvFL(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(correlationId, CallbackMessages.RESV_FL);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    @Override
    public UUID sendRsvCmtCF(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(correlationId, CallbackMessages.RESV_CM_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvCmtFL(String correlationId) {
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(correlationId, CallbackMessages.RESV_CM_FL);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvAbtCF(String correlationId) {
        UUID taskId = null;
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(correlationId, CallbackMessages.RESV_AB_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvTimeout(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        Task sendNsiMsg = new SendNSIMessageTask(correlationId, CallbackMessages.RESV_TIMEOUT);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }





}
