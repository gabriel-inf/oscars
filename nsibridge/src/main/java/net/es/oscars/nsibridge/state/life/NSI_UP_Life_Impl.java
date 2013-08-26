package net.es.oscars.nsibridge.state.life;


import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiLifeMdl;
import net.es.oscars.nsibridge.task.OscarsCancelTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import java.util.Date;
import java.util.UUID;


public class NSI_UP_Life_Impl implements NsiLifeMdl {
    String connectionId = "";
    public NSI_UP_Life_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_UP_Life_Impl() {}



    @Override
    public UUID localTerm() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task oscarsCancel = new OscarsCancelTask(connectionId);
        UUID taskId = null;
        try {
            taskId = wf.schedule(oscarsCancel, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendTermCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.TERM_CF);
        UUID taskId = null;

        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }


    @Override
    public UUID notifyForcedEndErrorEvent() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.ERROR_EVENT);
        UUID taskId = null;

        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }
}
