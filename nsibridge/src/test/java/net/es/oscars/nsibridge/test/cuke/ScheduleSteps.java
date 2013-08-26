package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import net.es.oscars.utils.task.sched.Schedule;
import net.es.oscars.utils.task.sched.Workflow;


public class ScheduleSteps {
    private static boolean started = false;
    @Given("^I have started the scheduler$")
    public void I_have_started_the_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        if (!started) {
            started = true;
            sch.start();
        }

    }
    @Given("^I have stopped the scheduler$")
    public void I_have_stopped_the_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        if (started) {
            started = false;
            sch.stop();
        }

    }



}
