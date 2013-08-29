package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.task.*;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;
import java.util.UUID;


public class NSI_UP_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_UP_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_UP_Prov_Impl() {}



    @Override
    public UUID localProv(String correlationId) {
        UUID taskId = null;

        long now = new Date().getTime();

        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM psm = smh.findNsiProvSM(connectionId);

        OscarsSetupTask ost = new OscarsSetupTask();
        ost.setCorrelationId(correlationId);
        ost.setOscarsOp(OscarsOps.SETUP);
        ost.setStateMachine(psm);
        ost.setFailEvent(NSI_Prov_Event.LOCAL_PROV_FAILED);
        ost.setSuccessEvent(NSI_Prov_Event.LOCAL_PROV_CONFIRMED);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            taskId = wf.schedule(ost , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendProvCF(String correlationId) {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.PROV_CF);
        UUID taskId = null;
        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID notifyProvFL(String correlationId) {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.ERROR);
        UUID taskId = null;

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    @Override
    public UUID localRel(String correlationId) {
        UUID taskId = null;

        long now = new Date().getTime();

        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM psm = smh.findNsiProvSM(connectionId);

        OscarsTeardownTask ost = new OscarsTeardownTask();
        ost.setCorrelationId(correlationId);
        ost.setOscarsOp(OscarsOps.TEARDOWN);
        ost.setStateMachine(psm);
        ost.setSuccessEvent(NSI_Prov_Event.LOCAL_REL_CONFIRMED);
        ost.setFailEvent(NSI_Prov_Event.LOCAL_REL_FAILED);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            taskId = wf.schedule(ost , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    @Override
    public UUID sendRelCF(String correlationId) {
        long now = new Date().getTime();
        UUID taskId = null;

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.REL_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    @Override
    public UUID notifyRelFL(String correlationId) {
        long now = new Date().getTime();
        UUID taskId = null;

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.ERROR);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }
}
