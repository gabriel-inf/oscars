package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_TH;
import net.es.oscars.nsibridge.state.resv.NSI_Stub_Resv_Impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReserveStateMachineSteps {
    private NSI_Resv_SM rsm;
    private boolean rsmException = false;
    @Given("^that I have created a new ReserveStateMachine for connectionId: \"([^\"]*)\"$")
    public void that_I_have_created_a_new_ReserveStateMachine(String connectionId) throws Throwable {
        rsm = new NSI_Resv_SM(connectionId);
    }

    @Given("^that I have set the Reserve model implementation to be a stub$")
    public void that_I_have_set_the_Reserve_model_implementation_to_be_a_stub() throws Throwable {
        NSI_Stub_Resv_Impl stub = new NSI_Stub_Resv_Impl(null);
        NSI_Resv_TH th = new NSI_Resv_TH();
        th.setMdl(stub);
        rsm.setTransitionHandler(th);
    }

    @Then("^the ReserveStateMachine state is \"([^\"]*)\"$")
    public void the_ReserveStateMachine_state_is(String arg1) throws Throwable {
        SM_State state = rsm.getState();
        assertThat(state.value(), is(arg1));
    }

    @When("^I submit the Reserve event \"([^\"]*)\"$")
    public void I_submit_the_Reserve_event(String arg1) throws Throwable {
        NSI_Resv_Event ev = NSI_Resv_Event.valueOf(arg1);
        rsmException = false;
        try {
            rsm.process(ev);
        } catch (Exception ex) {
            rsmException = true;
        }
    }

    @Then("^the ReserveStateMachine has thrown an exception$")
    public void the_ReserveStateMachine_has_thrown_an_exception() throws Throwable {
        assertThat(rsmException, is(true));
    }


    @Then("^the ReserveStateMachine has not thrown an exception$")
    public void the_ReserveStateMachine_has_not_thrown_an_exception() throws Throwable {
        assertThat(rsmException, is(false));
    }


}
