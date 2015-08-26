package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.apache.log4j.Logger;

import javax.security.auth.x500.X500Principal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class MiscSteps {
    private static Logger log = Logger.getLogger(MiscSteps.class);

    @Given("^the incoming DN is \"([^\"]*)\"$")
    public void the_incoming_DN_is(String arg1) throws Throwable {
        HelperSteps.store("incomingDN", arg1);
    }

    @Then("^the normalized DN is \"([^\"]*)\"$")
    public void the_normalized_DN_is(String arg1) throws Throwable {
        String incoming = HelperSteps.getValue("incomingDN");
        X500Principal principal = new X500Principal(incoming);

        String normalized = principal.getName();
        assertThat(normalized, is(arg1));


    }




}
