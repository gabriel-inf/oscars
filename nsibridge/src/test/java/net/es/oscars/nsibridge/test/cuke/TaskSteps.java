package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
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
    private HashMap<String, Set<UUID>> corrTasks = new HashMap<String, Set<UUID>>();

    @Then("^I know the reserve taskIds$")
    public void I_know_the_reserve_taskIds() throws Throwable {
        String corrId = HelperSteps.getValue("corrId");

        RequestHolder rh = RequestHolder.getInstance();
        ResvRequest rr = rh.findResvRequest(corrId);
        assertThat(rr, notNullValue());
        assertThat(rr.getTaskIds().size(), is(1));
        for (UUID taskId : rr.getTaskIds()) {
            log.debug("rr taskId: "+taskId);
        }

        corrTasks.put(corrId, rr.getTaskIds());
        for (UUID taskId : corrTasks.get(corrId)) {
            log.debug("connId: "+corrId+" taskId: "+taskId);
        }
    }

    @Then("^I know the simpleRequest taskIds$")
    public void I_know_the_simpleRequest_taskIds() throws Throwable {
        String corrId = HelperSteps.getValue("corrId");

        RequestHolder rh = RequestHolder.getInstance();
        SimpleRequest rr = rh.findSimpleRequest(corrId);
        assertThat(rr, notNullValue());
        assertThat(rr.getTaskIds().size(), is(1));
        corrTasks.put(corrId, rr.getTaskIds());
    }



    @When("^I tell the scheduler to run the taskIds in (\\d+) milliseconds$")
    public void I_tell_the_scheduler_to_run_the_taskIds_in_milliseconds(int arg2) throws Throwable {
        String corrId = HelperSteps.getValue("corrId");

        Schedule schedule = Schedule.getInstance();
        for (UUID taskId : corrTasks.get(corrId)) {
            schedule.scheduleSpecificTask(taskId, arg2);
        }
    }

    @When("^I wait up to (\\d+) ms until the task runstate is \"([^\"]*)\"$")
    public void I_wait_up_to_ms_until_the_task_runstate_is(int timeout, String runState) throws Throwable {
        String corrId = HelperSteps.getValue("corrId");

        Workflow wf = Workflow.getInstance();
        int elapsed = 0;
        boolean haveRunState = false;
        while ((elapsed < timeout) && !haveRunState) {
            boolean matchAll = true;
            for (UUID taskId : corrTasks.get(corrId)) {
                RunState rs = wf.getRunState(taskId);
                log.debug("taskId: " + taskId + " runState: " + rs);
                if (!rs.toString().equals(runState)) {
                    matchAll = false;
                }
            }
            if (matchAll) {
                haveRunState = true;
            } else {
                log.debug("sleeping 100ms until runstate for corrId: "+corrId+" is "+runState);
                Thread.sleep(100);
                elapsed += 100;
            }
        }
        assertThat(elapsed < timeout, is (true));
        assertThat(haveRunState, is(true));

        for (UUID taskId : corrTasks.get(corrId)) {
            RunState rs = wf.getRunState(taskId);
            assertThat(rs.toString(), is(runState));
        }
        log.debug("waited for "+elapsed+" ms until runstate for corrId: "+corrId+" became "+runState);
    }


}
