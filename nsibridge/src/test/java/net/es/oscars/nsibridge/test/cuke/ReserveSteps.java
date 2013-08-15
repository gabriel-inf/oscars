package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReserveSteps {
    private EntityManager em = PersistenceHolder.getInstance().getEntityManager();
    private int resvReqCount;

    @When("^I submit reserve\\(\\) with connectionId: \"([^\"]*)\"$")
    public void I_submit_reserve_with_connectionId(String arg1) throws Throwable {
        ResvRequest resvRequest = NSIRequestFactory.getRequest();
        ReserveType rt = resvRequest.getReserveType();
        rt.setConnectionId(arg1);
        ConnectionProvider cp = new ConnectionProvider();
        Holder<String> connHolder = new Holder<String>();
        connHolder.value = rt.getConnectionId();
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        cp.reserve(connHolder, rt.getGlobalReservationId(), rt.getDescription(), rt.getCriteria(), resvRequest.getInHeader(), outHolder);
    }




    @Given("^that I know the count of all pending reservation requests$")
    public void that_I_know_the_count_of_all_pending_reservation_requests() throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        resvReqCount = rh.getResvRequests().size();
    }

    @Then("^the count of pending reservation requests has changed by (\\d+)$")
    public void the_count_of_pending_reservation_requests_has_changed_by(int arg1) throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        int newCount = rh.getResvRequests().size();
        assertThat(newCount, is(resvReqCount+arg1));

    }
}
