package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_TH;
import net.es.oscars.nsibridge.state.resv.NSI_Stub_Resv_Impl;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReserveStateMachineSteps {
    private HashMap<String, Boolean> rsmExceptions = new HashMap<String, Boolean>();
    private NSI_SM_Holder smh;
    @Given("^that I have created a new ReserveStateMachine for connectionId: \"([^\"]*)\"$")
    public void that_I_have_created_a_new_ReserveStateMachine(String connectionId) throws Throwable {
        NSI_Resv_SM rsm = new NSI_Resv_SM(connectionId);
        smh = NSI_SM_Holder.getInstance();
        smh.getResvStateMachines().put(connectionId, rsm);
        rsmExceptions.put(connectionId, false);
    }

    @Given("^that I have set the Reserve model implementation for connectionId: \"([^\"]*)\" to be a stub$")
    public void that_I_have_set_the_Reserve_model_implementation_to_be_a_stub(String connectionId) throws Throwable {
        NSI_Stub_Resv_Impl stub = new NSI_Stub_Resv_Impl(null);
        NSI_Resv_TH th = new NSI_Resv_TH();
        th.setMdl(stub);

        smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.findNsiResvSM(connectionId);
        rsm.setTransitionHandler(th);
    }

    @Then("^the ReserveStateMachine state for connectionId: \"([^\"]*)\" is: \"([^\"]*)\"$")
    public void the_ReserveStateMachine_state_for_connectionId_is(String connectionId, String stateStr) throws Throwable {
        smh = NSI_SM_Holder.getInstance();

        NSI_Resv_SM rsm = smh.findNsiResvSM(connectionId);
        SM_State state = rsm.getState();

        assertThat(state.value(), is(stateStr));
    }

    @When("^I submit the Reserve event: \"([^\"]*)\" for connectionId: \"([^\"]*)\"$")
    public void I_submit_the_Reserve_event(String arg1, String connectionId) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.findNsiResvSM(connectionId);

        NSI_Resv_Event ev = NSI_Resv_Event.valueOf(arg1);
        rsmExceptions.put(connectionId, false);
        try {
            rsm.process(ev);
        } catch (Exception ex) {
            rsmExceptions.put(connectionId, true);
        }
    }

    @Then("^the ReserveStateMachine for connectionId: \"([^\"]*)\" has thrown an exception$")
    public void the_ReserveStateMachine_has_thrown_an_exception(String arg1) throws Throwable {
        assertThat(rsmExceptions.get(arg1), is(true));
    }


    @Then("^the ReserveStateMachine for connectionId: \"([^\"]*)\" has not thrown an exception$")
    public void the_ReserveStateMachine_has_not_thrown_an_exception(String arg1) throws Throwable {
        assertThat(rsmExceptions.get(arg1), is(false));
    }


}
