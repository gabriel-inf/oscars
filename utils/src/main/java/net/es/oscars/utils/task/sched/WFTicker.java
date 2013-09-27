package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.Outcome;
import net.es.oscars.utils.task.Task;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionException;

import java.util.Date;

public class WFTicker extends Thread {
    private Logger log = Logger.getLogger(WFTicker.class);

    public void run(){
        while(true){
            try{
                this.execute();
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                break;
            }catch(Exception e){
                log.error("Error in WFTicker: " + e.getMessage());
            }
        }
    }
    
    public void execute() throws JobExecutionException {
        Workflow wf = net.es.oscars.utils.task.sched.Workflow.getInstance();
        long now = new Date().getTime();
        Long sec = now / 1000;
        if (sec % 5 == 0) {
            log.debug("WFTicker alive");
        }
        
        Task task = null;
        try {
            task = wf.nextRunnable(now);
            if (task != null) {
                task.onRun();
            }
        }catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            if(task != null && !Outcome.FAIL.equals(task.getOutcome())){
                this.failTaskOnUnhandledException(task);
            }
        }finally{
            if (task != null) {
                wf.finishRunning(task);
            }
        }

    }
    
    public void failTaskOnUnhandledException(Task task){
        try {
            task.onFail();
        } catch (Exception e) {
            log.error("Error failing task: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
