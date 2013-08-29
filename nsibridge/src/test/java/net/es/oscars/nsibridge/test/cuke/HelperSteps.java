package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.When;

import java.util.HashMap;

public class HelperSteps {
    private static HashMap<String, String> store = new HashMap<String, String>();

    public static String getValue(String key) {
        return store.get(key);
    }

    @When("^I set the current connId to: \"([^\"]*)\"$")
    public void I_set_the_current_connId_to(String arg1) throws Throwable {
        store.put("connId", arg1);
    }

    @When("^I set the current corrId to: \"([^\"]*)\"$")
    public void I_set_the_current_corrId_to(String arg1) throws Throwable {
        store.put("corrId", arg1);
    }

}
