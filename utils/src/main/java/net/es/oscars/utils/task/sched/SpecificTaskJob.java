package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.UUID;

public class SpecificTaskJob implements Job {
    private Logger log = Logger.getLogger(SpecificTaskJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID taskId;
        JobDataMap data = context.getJobDetail().getJobDataMap();

        taskId = (UUID) data.get("taskId");
        try {
            Workflow.getInstance().runScheduledTask(taskId);
        } catch (TaskException ex) {
            log.error(ex);

        }

    }

}
