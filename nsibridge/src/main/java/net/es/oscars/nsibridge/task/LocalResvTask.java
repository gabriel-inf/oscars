package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;

public class LocalResvTask extends Task  {
    private NSI_Resv_Event event;
    private String connectionId;
    public LocalResvTask(String connectionId, NSI_Resv_Event event) {
        this.scope = "oscars";
        this.connectionId = connectionId;
        this.event = event;
    }


    public void onRun() throws TaskException {
        super.onRun();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM sm = smh.findNsiResvSM(connectionId);
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        boolean success = false;
        boolean decided = false;
        try {
            OscarsStatusRecord or = NSI_Util.getLatestOscarsRecord(connectionId);
            if (or == null || or.getStatus() == null ) {
                success = false;
                decided = false;

            } else if (or.getStatus().equals("FINISHED")) {
                if (event.equals(NSI_Resv_Event.LOCAL_RESV_ABORT_CF)) {
                    success = true;
                    decided = true;
                }
            } else if (or.getStatus().equals("RESERVED")) {
                if (event.equals(NSI_Resv_Event.LOCAL_RESV_CHECK_CF)) {
                    success = true;
                    decided = true;
                }
            } else if ( (or.getStatus().equals("FAILED")) || (or.getStatus().equals("UNKNOWN")) ){
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

            OscarsQueryTask oqt = new OscarsQueryTask(connectionId);
            Double d = (tc.getQueryInterval() * 1000);
            Long when = now + d.longValue();
            try {
                wf.schedule(oqt , when);
            } catch (TaskException e) {
                e.printStackTrace();
            }

            // reschedule this same task to look at the results of that query
            d = ((tc.getQueryInterval()+tc.getQueryResultDelay()) * 1000);
            when = now + d.longValue();

            LocalResvTask resvTask = new LocalResvTask(connectionId, NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
            try {
                wf.schedule(resvTask , when);
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
            event = NSI_Resv_Event.LOCAL_RESV_CHECK_FL;
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
