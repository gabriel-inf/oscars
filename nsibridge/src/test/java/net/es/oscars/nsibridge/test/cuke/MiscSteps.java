package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import net.es.oscars.nsibridge.oscars.OscarsUtil;
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

    @Then("^the normalized DN matches \"([^\"]*)\"$")
    public void the_normalized_DN_is(String arg1) throws Throwable {

        String incoming = HelperSteps.getValue("incomingDN");

        String normalized = OscarsUtil.normalizeDN(incoming);


        X500Principal inPrincipal = new X500Principal(normalized);
        X500Principal argPrincipal = new X500Principal(arg1);

        assertThat(inPrincipal, is(argPrincipal));
    }




}
