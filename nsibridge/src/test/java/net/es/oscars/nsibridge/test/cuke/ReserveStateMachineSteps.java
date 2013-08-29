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
    @Given("^that I have created a new ReserveStateMachine$")
    public void that_I_have_created_a_new_ReserveStateMachine() throws Throwable {
        String connId = HelperSteps.getValue("connId");

        NSI_Resv_SM rsm = new NSI_Resv_SM(connId);
        smh = NSI_SM_Holder.getInstance();
        smh.getResvStateMachines().put(connId, rsm);
        rsmExceptions.put(connId, false);
    }

    @Given("^that I have set the Reserve model implementation to be a stub$")
    public void that_I_have_set_the_Reserve_model_implementation_to_be_a_stub() throws Throwable {
        NSI_Stub_Resv_Impl stub = new NSI_Stub_Resv_Impl(null);
        NSI_Resv_TH th = new NSI_Resv_TH();
        th.setMdl(stub);
        String connId = HelperSteps.getValue("connId");


        smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
        rsm.setTransitionHandler(th);
    }

    @Then("^the ReserveStateMachine state is: \"([^\"]*)\"$")
    public void the_ReserveStateMachine_state_is(String stateStr) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        String connId = HelperSteps.getValue("connId");

        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
        SM_State state = rsm.getState();

        assertThat(state.value(), is(stateStr));
    }

    @When("^I submit the Reserve event: \"([^\"]*)\"$")
    public void I_submit_the_Reserve_event(String arg1) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");
        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);

        NSI_Resv_Event ev = NSI_Resv_Event.valueOf(arg1);
        rsmExceptions.put(connId, false);
        try {
            rsm.process(ev, corrId);
        } catch (Exception ex) {
            rsmExceptions.put(corrId, true);
        }
    }

    @Then("^the ReserveStateMachine has thrown an exception$")
    public void the_ReserveStateMachine_has_thrown_an_exception() throws Throwable {
        String connId = HelperSteps.getValue("connId");

        assertThat(rsmExceptions.get(connId), is(true));
    }


    @Then("^the ReserveStateMachine has not thrown an exception$")
    public void the_ReserveStateMachine_has_not_thrown_an_exception() throws Throwable {
        String connId = HelperSteps.getValue("connId");

        assertThat(rsmExceptions.get(connId), is(false));
    }


}
