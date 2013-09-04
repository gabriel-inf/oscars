package net.es.oscars.nsibridge.state.life;


import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiLifeMdl;
import net.es.oscars.nsibridge.ifces.StateMachineType;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.task.OscarsCancelTask;
import net.es.oscars.nsibridge.task.OscarsTeardownTask;
import net.es.oscars.nsibridge.task.SMTransitionTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.UUID;


public class NSI_UP_Life_Impl implements NsiLifeMdl {
    String connectionId = "";
    public NSI_UP_Life_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_UP_Life_Impl() {}
    private static final Logger log = Logger.getLogger(NSI_UP_Life_Impl.class);


    @Override
    public UUID localTerm(String correlationId) {
        log.debug("localTerm start");
        UUID taskId = null;

        long now = new Date().getTime();

        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Life_SM lsm = smh.findNsiLifeSM(connectionId);


        SMTransitionTask sm = new SMTransitionTask();
        sm.setCorrelationId(correlationId);
        sm.setSmt(StateMachineType.LSM);
        sm.setSuccessEvent(NSI_Life_Event.LOCAL_TERM_CONFIRMED);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            taskId = wf.schedule(sm , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        /*
        OscarsCancelTask ost = new OscarsCancelTask();
        ost.setCorrelationId(correlationId);
        ost.setOscarsOp(OscarsOps.CANCEL);
        ost.setStateMachine(lsm);
        ost.setSuccessEvent(NSI_Life_Event.LOCAL_TERM_CONFIRMED);
        ost.setFailEvent(NSI_Life_Event.LOCAL_TERM_FAILED);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            taskId = wf.schedule(ost , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        */

        return taskId;
    }

    @Override
    public UUID sendTermCF(String correlationId) {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(correlationId, null, CallbackMessages.TERM_CF);
        UUID taskId = null;

        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }


    @Override
    public UUID notifyForcedEndErrorEvent(String correlationId) {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(correlationId, connectionId, CallbackMessages.ERROR_EVENT);
        UUID taskId = null;

        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }
}
