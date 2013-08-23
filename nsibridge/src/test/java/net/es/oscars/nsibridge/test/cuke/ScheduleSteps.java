package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import net.es.oscars.utils.task.sched.Schedule;


public class ScheduleSteps {

    @Given("^I have started the scheduler$")
    public void I_have_started_the_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        sch.start();

    }

}
