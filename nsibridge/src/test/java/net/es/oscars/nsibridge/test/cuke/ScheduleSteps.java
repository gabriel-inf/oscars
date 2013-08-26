package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import net.es.oscars.utils.task.sched.Schedule;
import net.es.oscars.utils.task.sched.Workflow;


public class ScheduleSteps {
    @Given("^I have started the scheduler$")
    public void I_have_started_the_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        sch.start();

    }
    @Given("^I have stopped the scheduler$")
    public void I_have_stopped_the_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        sch.stop();
    }


    @Given("^I have started the Quartz scheduler$")
    public void I_have_started_the_Quartz_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        sch.startQuartz();
    }

    @Given("^I have stopped the Quartz scheduler$")
    public void I_have_stopped_the_Quartz_scheduler() throws Throwable {
        Schedule sch = Schedule.getInstance();
        sch.stopQuartz();
    }



}
