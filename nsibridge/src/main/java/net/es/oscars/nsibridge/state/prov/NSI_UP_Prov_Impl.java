package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.task.*;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_UP_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_UP_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_UP_Prov_Impl() {}



    @Override
    public void localProv() {
        long now = new Date().getTime();
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();

        OscarsSetupTask ost = new OscarsSetupTask(connectionId);
        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            wf.schedule(ost , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        OscarsQueryTask oqt = new OscarsQueryTask(connectionId);
        d = (tc.getQueryAfterSetupWait() * 1000);
        when = now + d.longValue();
        try {
            wf.schedule(oqt , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        d = (tc.getQueryResultDelay() * 1000);
        when = now + d.longValue();

        LocalProvTask provTask = new LocalProvTask(connectionId, NSI_Prov_Event.LOCAL_PROV_CONFIRMED);
        try {
            wf.schedule(provTask , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendProvCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.PROV_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyProvFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.ERROR);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void localRel() {
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        OscarsTeardownTask ost = new OscarsTeardownTask(connectionId);
        try {
            wf.schedule(ost , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        OscarsQueryTask oqt = new OscarsQueryTask(connectionId);
        d = (tc.getQueryAfterSetupWait() * 1000);
        when = now + d.longValue();
        try {
            wf.schedule(oqt , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }

        d = (tc.getQueryResultDelay() * 1000);
        when = now + d.longValue();

        LocalProvTask provTask = new LocalProvTask(connectionId, NSI_Prov_Event.LOCAL_PROV_CONFIRMED);
        try {
            wf.schedule(provTask , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendRelCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.REL_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyRelFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.ERROR);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
}
