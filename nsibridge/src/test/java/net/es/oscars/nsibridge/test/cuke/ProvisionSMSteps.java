package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.nsibridge.state.prov.NSI_Stub_Prov_Impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProvisionSMSteps {
    private NSI_Prov_SM psm;
    private boolean psmException = false;

    @Given("^that I have created a new ProvisioningStateMachine for connectionId: \"([^\"]*)\"$")
    public void that_I_have_created_a_new_ProvisioningStateMachine_for_connectionId(String arg1) throws Throwable {
        psm = new NSI_Prov_SM(arg1);
    }

    @Given("^that I have set the Provisioning model implementation to be a stub$")
    public void that_I_have_set_the_Provisioning_model_implementation_to_be_a_stub() throws Throwable {
        NSI_Stub_Prov_Impl stub = new NSI_Stub_Prov_Impl(null);
        NSI_Prov_TH th = new NSI_Prov_TH();
        th.setMdl(stub);
        psm.setTransitionHandler(th);
    }

    @Then("^the ProvisioningStateMachine state is \"([^\"]*)\"$")
    public void the_ProvisioningStateMachine_state_is(String arg1) throws Throwable {

        SM_State state = psm.getState();
        assertThat(state.value(), is(arg1));
    }

    @When("^I submit the Provisioning event \"([^\"]*)\"$")
    public void I_submit_the_Provisioning_event(String arg1) throws Throwable {
        NSI_Prov_Event ev = NSI_Prov_Event.valueOf(arg1);
        psmException = false;
        try {
            psm.process(ev);
        } catch (Exception ex) {
            psmException = true;
        }
    }


}
