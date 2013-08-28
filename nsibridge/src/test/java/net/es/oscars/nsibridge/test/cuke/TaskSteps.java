package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.utils.task.RunState;
import net.es.oscars.utils.task.sched.Schedule;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class TaskSteps {
    private static Logger log = Logger.getLogger(TaskSteps.class);
    private HashMap<String, Set<UUID>> connTasks = new HashMap<String, Set<UUID>>();

    @Then("^I know the reserve taskIds for connectionId: \"([^\"]*)\"$")
    public void I_know_the_reserve_taskIds_for_connectionId(String arg1) throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        ResvRequest rr = rh.findResvRequest(arg1);
        assertThat(rr, notNullValue());
        assertThat(rr.getTaskIds().size(), is(1));
        connTasks.put(arg1, rr.getTaskIds());
    }

    @Then("^I know the simpleRequest taskIds for connectionId: \"([^\"]*)\" type: \"([^\"]*)\"$")
    public void I_know_the_simpleRequest_taskIds_for_connectionId(String arg1, String arg2) throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        SimpleRequest rr = rh.findSimpleRequest(arg1, SimpleRequestType.valueOf(arg2));
        assertThat(rr, notNullValue());
        assertThat(rr.getTaskIds().size(), is(1));
        connTasks.put(arg1, rr.getTaskIds());
    }



    @When("^I tell the scheduler to run the taskIds for connectionId: \"([^\"]*)\" in (\\d+) milliseconds$")
    public void I_tell_the_scheduler_to_run_the_taskIds_for_connectionId_in_milliseconds(String arg1, int arg2) throws Throwable {
        Schedule schedule = Schedule.getInstance();
        for (UUID taskId : connTasks.get(arg1)) {
            schedule.scheduleSpecificTask(taskId, arg2);
        }
    }

    @When("^I wait up to (\\d+) ms until the runstate for the taskIds for connectionId: \"([^\"]*)\" is \"([^\"]*)\"$")
    public void I_wait_up_to_ms_until_the_runstate_for_the_taskIds_for_connectionId_is(int timeout, String connId, String runState) throws Throwable {
        Workflow wf = Workflow.getInstance();
        int elapsed = 0;
        boolean haveRunState = false;
        while ((elapsed < timeout) && !haveRunState) {
            boolean matchAll = true;
            for (UUID taskId : connTasks.get(connId)) {
                RunState rs = wf.getRunState(taskId);
                log.debug("taskId:" + taskId + " runState: " + rs);
                if (!rs.toString().equals(runState)) {
                    matchAll = false;
                }
            }
            if (matchAll) {
                haveRunState = true;
            } else {
                log.debug("sleeping 100ms until runstate for connId: "+connId+" is "+runState);
                Thread.sleep(100);
                elapsed += 100;
            }
        }
        assertThat(elapsed < timeout, is (true));
        assertThat(haveRunState, is(true));

        for (UUID taskId : connTasks.get(connId)) {
            RunState rs = wf.getRunState(taskId);
            assertThat(rs.toString(), is(runState));
        }
        log.debug("waited for "+elapsed+" ms until runstate for connId: "+connId+" became "+runState);
    }


}
