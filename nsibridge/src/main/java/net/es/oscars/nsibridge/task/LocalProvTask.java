package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import javax.persistence.EntityManager;
import java.util.Date;

public class LocalProvTask extends Task  {
    private NSI_Prov_Event event;
    private String connectionId;
    public LocalProvTask(String connectionId, NSI_Prov_Event event) {
        this.scope = "oscars";
        this.connectionId = connectionId;
        this.event = event;
    }


    public void onRun() throws TaskException {
        super.onRun();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM sm = smh.getProvStateMachines().get(connectionId);
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        Double d = (tc.getTaskInterval() * 1000);

        boolean success = false;
        boolean decided = false;
        try {
            OscarsStatusRecord or = NSI_Util.getLatestOscarsRecord(connectionId);
            if (or == null || or.getStatus() == null ) {
                success = false;
                decided = false;

            } else if (or.getStatus().equals("ACTIVE")) {
                success = true;
                decided = true;
            } else if (or.getStatus().equals("FAILED")) {
                success = false;
                decided = true;
            } else {
                success = false;
                decided = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }



        if (!decided) {
            // reschedule a query task
            Long when = now + d.longValue();

            OscarsQueryTask oqt = new OscarsQueryTask(connectionId);
            d = (tc.getQueryInterval() * 1000);
            when = now + d.longValue();
            try {
                wf.schedule(oqt , when);
            } catch (TaskException e) {
                e.printStackTrace();
            }

            // reschedule this same task to look at the results of that query
            d = ((tc.getQueryInterval()+tc.getQueryResultDelay()) * 1000);
            when = now + d.longValue();

            LocalProvTask provTask = new LocalProvTask(connectionId, NSI_Prov_Event.LOCAL_PROV_CONFIRMED);
            try {
                wf.schedule(provTask , when);
            } catch (TaskException e) {
                e.printStackTrace();
            }

        } else if (success) {
            try {
                sm.process(event);
            } catch (StateException e) {
                e.printStackTrace();
            }
        } else {
            event = NSI_Prov_Event.LOCAL_PROV_FAILED;
            try {
                sm.process(event);
            } catch (StateException e) {
                e.printStackTrace();
            }
        }
        System.out.println(this.id+" ran!");
        this.onSuccess();
    }

}
