package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import net.es.oscars.utils.task.sched.Schedule;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;


public class ScheduleSteps {
    private static Logger log = Logger.getLogger(ScheduleSteps.class);
    @Given("^I have started the scheduler$")
    public void I_have_started_the_scheduler() throws Throwable {
        log.debug("starting scheduler");
        Schedule sch = Schedule.getInstance();

        sch.start();

    }


}
