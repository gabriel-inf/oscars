package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.task.ProvMonitor;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;
import org.apache.log4j.Logger;

import javax.xml.ws.Holder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProvisionSteps {

    private static Logger log = Logger.getLogger(ProvisionSteps.class);

    private int provReqCount;

    @Given("^that I know the count of all pending provisioning requests$")
    public void that_I_know_the_count_of_all_pending_provisioning_requests() throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        provReqCount = 0;
        for (SimpleRequest sr : rh.getSimpleRequests().values()) {
            if (sr.getRequestType().equals(SimpleRequestType.PROVISION)) provReqCount++;
        }

    }

    @When("^I submit provision$")
    public void I_submit_provision() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();

        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");
        SimpleRequest commRequest = NSIRequestFactory.getSimpleRequest(connId, corrId, SimpleRequestType.PROVISION);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();

        try {
            log.debug("submitting provision connId:"+connId+" corrId: "+corrId);
            cp.provision(connId, commRequest.getInHeader(), outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);
    }


    @When("^I submit release")
    public void I_submit_release() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();

        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");
        SimpleRequest commRequest = NSIRequestFactory.getSimpleRequest(connId, corrId, SimpleRequestType.RELEASE);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();

        try {
            log.debug("submitting release connId:"+connId+" corrId: "+corrId);
            cp.release(connId, commRequest.getInHeader(), outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);
    }


    @Then("^the count of pending provisioning requests has changed by (\\d+)$")
    public void the_count_of_pending_provisioning_requests_has_changed_by(int arg1) throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        int newCount = 0;
        for (SimpleRequest sr : rh.getSimpleRequests().values()) {
            if (sr.getRequestType().equals(SimpleRequestType.PROVISION)) newCount++;
        }
        assertThat(newCount, is(provReqCount+arg1));

    }


    @Then("^the provMonitor has started \"([^\"]*)\"$")
    public void the_provMonitor_has_started(String action) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        String inspect = OscarsProvQueue.getInstance().getInspect().get(connId);
        assertThat(inspect, is("SETUP"));
    }


}
