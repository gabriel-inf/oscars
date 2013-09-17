package net.es.oscars.nsibridge.test.cuke;


import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.config.OscarsStubSecConfig;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.prov.ScheduleUtils;
import net.es.oscars.nsibridge.soap.impl.OscarsSecurityContext;
import net.es.oscars.utils.task.sched.Schedule;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnvironmentSteps {
    private static Logger log = Logger.getLogger(EnvironmentSteps.class);
    private static boolean didSetup = false;
    @Given("^I have set up the run environment$")
    public void I_have_set_up_the_run_environment() throws Throwable {
        if (didSetup) {
            // log.info("resv timeout: "+SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class).getResvTimeout());
            return;
        }

        SpringContext sc = SpringContext.getInstance();
        sc.initContext("src/test/resources/config/beans.xml");

        OscarsProxy op = OscarsProxy.getInstance();
        op.initialize();


        Schedule sch = Schedule.getInstance();
        sch.start();
        assertThat(sch.isStarted(), is(true));

        ScheduleUtils.scheduleProvMonitor();
        ScheduleUtils.scheduleResvTimeoutMonitor();

        OscarsSecurityContext.getInstance().setSubjectAttributes(new SubjectAttributes());
        didSetup = true;

    }
    @Given("^I wait up to (\\d+) sec until any previous tasks complete$")
    public void I_wait_up_to_sec_until_any_previous_tasks_complete(int seconds) throws Throwable {
        Workflow wf = Workflow.getInstance();
        int elapsed = 0;
        int timeout = seconds * 1000;
        while ((elapsed < timeout) && wf.hasItems()) {
            Thread.sleep(100);
            elapsed += 100;
        }
        log.debug("waited for "+elapsed+" ms for workflow tasks to complete");
        log.debug(wf.printTasks());
        assertThat(wf.hasItems(), is(false));
    }


    @Given("^I clear all existing tasks$")
    public void I_clear_all_existing_tasks() throws Throwable {
        Workflow wf = Workflow.getInstance();
        if (wf.hasItems()) {
            log.debug(wf.printTasks());
        }
        wf.clear();
    }



    @When("^I wait (\\d+) milliseconds$")
    public void I_wait_milliseconds(int arg1) throws Throwable {
        log.debug("sleeping for .."+arg1+" ms");
        Thread.sleep(arg1);
    }
    @Then("^I log \"([^\"]*)\"$")
    public void I_log(String arg1) throws Throwable {
        log.info(arg1);
    }



}
