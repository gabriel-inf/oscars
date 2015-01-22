package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.services.point2point.P2PServiceBaseType;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelperSteps {
    private static HashMap<String, String> store = new HashMap<String, String>();
    private static boolean submitException = false;
    private static ResvRequest resvRequest;

    public static ResvRequest getResvRequest() {
        return resvRequest;
    }

    public static String getValue(String key) {
        return store.get(key);
    }

    public static void setSubmitException(boolean val) {
        submitException = val;
    }

    @When("^I set the current NSA requester to \"([^\"]*)\"$")
    public void I_set_the_current_NSA_requester_to(String arg1) throws Throwable {
        store.put("requesterNSA", arg1);
    }

    @When("^I set the current NSA provider to \"([^\"]*)\"$")
    public void I_set_the_current_NSA_provider_to(String arg1) throws Throwable {
        store.put("providerNSA", arg1);
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


    @When("^I assign random connId and corrId")
    public void I_assign_a_random_connId_and_corrId() throws Throwable {
        String connId = UUID.randomUUID().toString();
        String corrId = UUID.randomUUID().toString();
        store.put("connId", connId);
        store.put("corrId", corrId);
    }

    @When("^I assign a random connId")
    public void I_assign_a_random_connId() throws Throwable {
        String connId = UUID.randomUUID().toString();
        store.put("connId", connId);
    }

    @When("^I assign a random corrId")
    public void I_assign_a_random_corrId() throws Throwable {
        String corrId = UUID.randomUUID().toString();
        store.put("corrId", corrId);
    }

    @When("^I generate a reservation request$")
    public void I_generate_a_reservation_request() throws Throwable {
        resvRequest = NSIRequestFactory.getRequest();
    }

    @When("^I set the version to (\\d+)$")
    public void I_set_the_version_to(int version) throws Throwable {
        resvRequest.getReserveType().getCriteria().setVersion(version);
    }

    @When("^I set the capacity to (\\d+)$")
    public void I_set_the_capacity_to(int arg1) throws Throwable {
        P2PServiceBaseType p2pType = (P2PServiceBaseType) resvRequest.getReserveType().getCriteria().getAny().get(0);
        p2pType.setCapacity(arg1);

    }

    @When("^I set \"([^\"]*)\" time to (\\d+) sec$")
    public void I_set_time_to_sec(String timeType, int secs) throws Throwable {
        long ms = 1000L * secs;

        Date now = new Date();
        Date when = new Date(now.getTime()+ms);

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(when);
        XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        if (timeType.equals("start")) {
            resvRequest.getReserveType().getCriteria().getSchedule().setStartTime(xgc);

        } else if (timeType.equals("end")) {

            resvRequest.getReserveType().getCriteria().getSchedule().setEndTime(xgc);
        } else {
            throw new Exception("unknown time type");
        }

    }

    public static void store(String key, String value) {
        store.put(key, value);
    }


}
