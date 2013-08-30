package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.ifces.StateMachine;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_TH;
import net.es.oscars.nsibridge.state.life.NSI_Stub_Life_Impl;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.nsibridge.state.prov.NSI_Stub_Prov_Impl;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_TH;
import net.es.oscars.nsibridge.state.resv.NSI_Stub_Resv_Impl;
import org.apache.log4j.Logger;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class StateMachineSteps {
    private HashMap<String, Boolean> rsmExceptions = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> psmExceptions = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> lsmExceptions = new HashMap<String, Boolean>();
    private NSI_SM_Holder smh;
    private static Logger log = Logger.getLogger(StateMachineSteps.class);

    private enum SMTypes {
        RSM,
        PSM,
        LSM
    }

    @Given("^that I have created a new \"([^\"]*)\" state machine$")
    public void that_I_have_created_a_new_state_machine(String smt) throws Throwable {
        String connId = HelperSteps.getValue("connId");
        smh = NSI_SM_Holder.getInstance();
        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }

        switch (smtype) {
            case RSM:
                NSI_Resv_SM rsm = new NSI_Resv_SM(connId);
                rsmExceptions.put(connId, false);
                smh.getResvStateMachines().put(connId, rsm);
                break;
            case PSM:
                NSI_Prov_SM psm = new NSI_Prov_SM(connId);
                psmExceptions.put(connId, false);
                smh.getProvStateMachines().put(connId, psm);
                break;
            case  LSM:
                NSI_Life_SM lsm = new NSI_Life_SM(connId);
                lsmExceptions.put(connId, false);
                smh.getLifeStateMachines().put(connId, lsm);
                break;
        }

    }
    @Then("^the \"([^\"]*)\" state is: \"([^\"]*)\"$")
    public void the_state_is(String smt, String stateStr) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        String connId = HelperSteps.getValue("connId");
        StateMachine sm = null;
        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }

        assertThat(smtype, notNullValue());

        switch (smtype) {
            case RSM:
                sm = smh.findNsiResvSM(connId);
                break;
            case PSM:
                sm = smh.findNsiProvSM(connId);
                break;
            case LSM:
                sm = smh.findNsiLifeSM(connId);
                break;
        }
        assertThat(sm, notNullValue());

        SM_State state = sm.getState();

        assertThat(state.value(), is(stateStr));
    }

    @Given("^that I have set the \"([^\"]*)\" model implementation to be a stub$")
    public void that_I_have_set_the_model_implementation_to_be_a_stub(String smt) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        String connId = HelperSteps.getValue("connId");
        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
        assertThat(smtype, notNullValue());

        switch (smtype) {
            case RSM:
                NSI_Stub_Resv_Impl rs = new NSI_Stub_Resv_Impl(null);
                NSI_Resv_TH rth = new NSI_Resv_TH();
                rth.setMdl(rs);
                NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
                rsm.setTransitionHandler(rth);
                break;

            case PSM:
                NSI_Stub_Prov_Impl ps = new NSI_Stub_Prov_Impl(null);
                NSI_Prov_TH pth = new NSI_Prov_TH();
                pth.setMdl(ps);
                NSI_Prov_SM psm = smh.findNsiProvSM(connId);
                psm.setTransitionHandler(pth);

                break;
            case LSM:
                NSI_Stub_Life_Impl ls = new NSI_Stub_Life_Impl(null);
                NSI_Life_TH lth = new NSI_Life_TH();
                lth.setMdl(ls);
                NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
                lsm.setTransitionHandler(lth);
                break;
        }
    }

    @When("^I submit the \"([^\"]*)\" event: \"([^\"]*)\"$")
    public void I_submit_the_event(String smt, String evt) throws Throwable {
        smh = NSI_SM_Holder.getInstance();
        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");
        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
        switch (smtype) {
            case RSM:
                NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
                NSI_Resv_Event rev = NSI_Resv_Event.valueOf(evt);
                rsmExceptions.put(corrId, false);
                try {
                    rsm.process(rev, corrId);
                } catch (Exception ex) {
                    log.debug("RSM exception for corrId: "+corrId);

                    rsmExceptions.put(corrId, true);
                }
                break;
            case PSM:
                NSI_Prov_SM psm = smh.findNsiProvSM(connId);
                NSI_Prov_Event pev = NSI_Prov_Event.valueOf(evt);
                psmExceptions.put(corrId, false);
                try {
                    psm.process(pev, corrId);
                } catch (Exception ex) {
                    log.debug("PSM exception for corrId: "+corrId);
                    psmExceptions.put(corrId, true);
                }
                break;

            case LSM:
                NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
                NSI_Life_Event lev = NSI_Life_Event.valueOf(evt);
                lsmExceptions.put(corrId, false);
                try {
                    lsm.process(lev, corrId);
                } catch (Exception ex) {
                    log.debug("LSM exception for corrId: "+corrId);
                    lsmExceptions.put(corrId, true);
                }
                break;
        }
    }

    @Then("^the \"([^\"]*)\" has not thrown an exception$")
    public void the_has_not_thrown_an_exception(String smt) throws Throwable {
        String corrId = HelperSteps.getValue("corrId");
        // log.debug("no exception? corrId: "+corrId);

        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
        assertThat(smtype, notNullValue());

        switch (smtype) {
            case RSM:
                assertThat(rsmExceptions.get(corrId), is(false));
                break;
            case PSM:
                assertThat(psmExceptions.get(corrId), is(false));
                break;
            case LSM:
                assertThat(lsmExceptions.get(corrId), is(false));
                break;
        }


    }

    @Then("^the \"([^\"]*)\" has thrown an exception$")
    public void the_has_thrown_an_exception(String smt) throws Throwable {
        String corrId = HelperSteps.getValue("corrId");
        // log.debug("has exception? corrId: "+corrId);
        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
        assertThat(smtype, notNullValue());

        switch (smtype) {
            case RSM:
                assertThat(rsmExceptions.get(corrId), is(true));
                break;
            case PSM:
                assertThat(psmExceptions.get(corrId), is(true));
                break;
            case LSM:
                assertThat(lsmExceptions.get(corrId), is(true));
                break;
        }

    }

    @Then("^I wait up to (\\d+) ms until the \"([^\"]*)\" state is: \"([^\"]*)\"$")
    public void I_wait_ms_until_I_know_the_sm_state_is(Integer ms, String smt, String stateStr) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        Long timeout = ms.longValue();
        Long elapsed = 0L;
        smh = NSI_SM_Holder.getInstance();

        StateMachine sm = null;
        SMTypes smtype = null;
        try {
            smtype = SMTypes.valueOf(smt);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }

        assertThat(smtype, notNullValue());

        switch (smtype) {
            case RSM:
                sm = smh.findNsiResvSM(connId);
                break;
            case PSM:
                sm = smh.findNsiProvSM(connId);
                break;
            case LSM:
                sm = smh.findNsiLifeSM(connId);
                break;
        }
        assertThat(sm, notNullValue());

        SM_State state = sm.getState();

        boolean haveDesiredState = state.value().equals(stateStr);
        int interval = 100;
        while ((elapsed < timeout) && !haveDesiredState ) {
            log.debug ("sleeping "+interval+" ms (elapsed: "+elapsed+") for "+smt+" state ("+state.value()+") to becomes "+stateStr);
            Thread.sleep(interval);
            elapsed += interval;
            state = sm.getState();
            haveDesiredState = state.value().equals(stateStr);
        }

        if (elapsed > timeout && !haveDesiredState) {
            throw new Exception("timed out waiting for "+smt+" state ("+state.value()+") to become "+stateStr);
        }

        log.debug("waited for "+elapsed+" ms until "+smt+" state for connId: "+connId+" became "+state.value());

        assertThat(state.value(), is(stateStr));
    }

}
