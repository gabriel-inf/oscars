package net.es.oscars.nsibridge.test.cuke;


import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.config.OscarsConfig;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.ScheduleUtils;
import net.es.oscars.utils.task.sched.Schedule;
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

        ApplicationContext ax = SpringContext.getInstance().getContext();
        OscarsConfig oc =  ax.getBean("oscarsConfig", OscarsConfig.class);
        OscarsStubConfig os = ax.getBean("oscarsStubConfig", OscarsStubConfig.class);

        op.setOscarsConfig(oc);
        OscarsProxy.getInstance().setOscarsStubConfig(os);

        assertThat(op.getOscarsConfig(), notNullValue());
        assertThat(op.getOscarsStubConfig(), notNullValue());

        Schedule sch = Schedule.getInstance();
        sch.start();
        assertThat(sch.isStarted(), is(true));

        ScheduleUtils.scheduleProvMonitor();
        ScheduleUtils.scheduleResvTimeoutMonitor();


        didSetup = true;

    }
    @When("^I wait (\\d+) milliseconds$")
    public void I_wait_milliseconds(int arg1) throws Throwable {
        log.debug("sleeping for .."+arg1+" ms");
        Thread.sleep(arg1);
    }


}
