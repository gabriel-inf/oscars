package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
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

        try {
            Task task = wf.nextRunnable(now);
            if (task != null) {
                task.onRun();
                wf.finishRunning(task);
            }

        } catch (TaskException ex) {
            log.error(ex);
        }

    }

}
