package net.es.oscars.nsibridge.test.cuke;


import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_TH;
import net.es.oscars.nsibridge.state.life.NSI_Stub_Life_Impl;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LifecycleSMSteps {
    private NSI_Life_SM lsm;
    private boolean lsmException = false;

    @Given("^that I have created a new LifecycleStateMachine for connectionId: \"([^\"]*)\"$")
    public void that_I_have_created_a_new_LifecycleStateMachine_for_connectionId(String arg1) throws Throwable {
        lsm = new NSI_Life_SM(arg1);
    }
    @Given("^that I have set the Lifecycle model implementation to be a stub$")
    public void that_I_have_set_the_Lifecycle_model_implementation_to_be_a_stub() throws Throwable {
        NSI_Stub_Life_Impl stub = new NSI_Stub_Life_Impl(null);
        NSI_Life_TH th = new NSI_Life_TH();
        th.setMdl(stub);
        lsm.setTransitionHandler(th);
    }


    @When("^I submit the Lifecycle event \"([^\"]*)\"$")
    public void I_submit_the_Lifecycle_event(String arg1) throws Throwable {
        NSI_Life_Event ev = NSI_Life_Event.valueOf(arg1);
        lsmException = false;
        try {
            lsm.process(ev);
        } catch (Exception ex) {
            lsmException = true;
        }
    }

    @Then("^the LifecycleStateMachine state is \"([^\"]*)\"$")
    public void the_LifecycleStateMachine_state_is(String arg1) throws Throwable {

        SM_State state = lsm.getState();
        assertThat(state.value(), is(arg1));

    }
}
