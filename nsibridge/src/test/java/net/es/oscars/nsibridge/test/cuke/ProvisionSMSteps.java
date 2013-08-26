package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.nsibridge.state.prov.NSI_Stub_Prov_Impl;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProvisionSMSteps {
    private HashMap<String, Boolean> psmExceptions = new HashMap<String, Boolean>();
    private NSI_SM_Holder smh;

    @Given("^that I have created a new ProvisioningStateMachine for connectionId: \"([^\"]*)\"$")
    public void that_I_have_created_a_new_ProvisioningStateMachine_for_connectionId(String connectionId) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM psm = new NSI_Prov_SM(connectionId);
        smh.getProvStateMachines().put(connectionId, psm);
        psmExceptions.put(connectionId, false);

    }

    @Given("^that I have set the Provisioning model implementation for connectionId: \"([^\"]*)\" to be a stub$")
    public void that_I_have_set_the_Provisioning_model_implementation_to_be_a_stub(String connectionId) throws Throwable {
        NSI_Stub_Prov_Impl stub = new NSI_Stub_Prov_Impl(null);
        NSI_Prov_TH th = new NSI_Prov_TH();
        th.setMdl(stub);

        smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM psm = smh.findNsiProvSM(connectionId);
        psm.setTransitionHandler(th);
    }

    @Then("^the ProvisioningStateMachine state for connectionId: \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_ProvisioningStateMachine_state_for_connectionId_is(String connectionId, String stateStr) throws Throwable {
        smh = NSI_SM_Holder.getInstance();

        NSI_Prov_SM psm = smh.findNsiProvSM(connectionId);

        SM_State state = psm.getState();
        assertThat(state.value(), is(stateStr));
    }


    @When("^I submit the Provisioning event \"([^\"]*)\" for connectionId: \"([^\"]*)\"$")
    public void I_submit_the_Provisioning_event(String arg1, String connectionId) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        NSI_Prov_SM psm = smh.findNsiProvSM(connectionId);

        NSI_Prov_Event ev = NSI_Prov_Event.valueOf(arg1);
        psmExceptions.put(connectionId, false);
        try {
            psm.process(ev);
        } catch (Exception ex) {
            psmExceptions.put(connectionId, true);
        }

    }


}
