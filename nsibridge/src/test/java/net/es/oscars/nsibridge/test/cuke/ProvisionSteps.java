package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProvisionSteps {
    private EntityManager em = PersistenceHolder.getEntityManager();
    private int provReqCount;
    private boolean thrownException = false;

    @Given("^that I know the count of all pending provisioning requests$")
    public void that_I_know_the_count_of_all_pending_provisioning_requests() throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        provReqCount = 0;
        for (SimpleRequest sr : rh.getSimpleRequests().values()) {
            if (sr.getRequestType().equals(SimpleRequestType.PROVISION)) provReqCount++;
        }

    }

    @When("^I submit provision\\(\\) with connectionId: \"([^\"]*)\"$")
    public void I_submit_provision_with_connectionId(String arg1) throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();
        CommonHeaderType inHeader = NSIRequestFactory.makeHeader();
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        try {
            cp.provision(arg1, inHeader, outHolder);
        } catch (Exception ex) {
            thrownException = true;
            // this should fail
        }

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
    @Then("^the provision\\(\\) call has thrown an exception$")
    public void the_provision_call_has_thrown_an_exception() throws Throwable {
        assertThat(thrownException, is(true));
    }

}
