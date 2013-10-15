package net.es.oscars.pss.workflow;

import java.util.ArrayList;
import java.util.Date;

import net.es.oscars.pss.api.Workflow;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.util.ClassFactory;

import org.apache.log4j.Logger;

public class WorkflowInspectorJob extends Thread {
    private Logger log = Logger.getLogger(WorkflowInspectorJob.class);

    public void run(){
        while(true){
            try{
                this.execute();
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                break;
            }catch(Exception e){
                log.error("Error in WorkflowInspectorJob: " + e.getMessage());
            }
        }
    }


    public void execute() {
        Date now = new Date();
        Workflow wfAgent = ClassFactory.getInstance().getWorkflow();
        PSSAction next = wfAgent.next();
        if (next != null) {
            ArrayList<PSSAction> actions = new ArrayList<PSSAction>();
            actions.add(next);
            try {
                wfAgent.process(actions);
            } catch (PSSException e) {
                e.printStackTrace();
            }
            wfAgent.update(next);
        }
    }

}
