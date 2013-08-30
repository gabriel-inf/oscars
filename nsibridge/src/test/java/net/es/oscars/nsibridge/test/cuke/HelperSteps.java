package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelperSteps {
    private static HashMap<String, String> store = new HashMap<String, String>();
    private static boolean submitException = false;

    public static String getValue(String key) {
        return store.get(key);
    }

    public static void setSubmitException(boolean val) {
        submitException = val;
    }


    @When("^I set the current connId to: \"([^\"]*)\"$")
    public void I_set_the_current_connId_to(String arg1) throws Throwable {
        store.put("connId", arg1);
    }

    @When("^I set the current corrId to: \"([^\"]*)\"$")
    public void I_set_the_current_corrId_to(String arg1) throws Throwable {
        store.put("corrId", arg1);
    }

    @Then("^the last submit has thrown an exception$")
    public void the_last_submit_has_thrown_an_exception() throws Throwable {
        assertThat(submitException, is(true));
    }

    @Then("^the last submit has not thrown an exception$")
    public void the_last_submit_has_not_thrown_an_exception() throws Throwable {
        assertThat(submitException, is(false));
    }


}
