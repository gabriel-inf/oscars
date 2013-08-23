package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ReserveSteps {
    private EntityManager em = PersistenceHolder.getInstance().getEntityManager();
    private int resvReqCount;
    private List<ConnectionRecord> connectionRecords;
    private String theConnectionId;

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


    @Given("^I know all the connectionRecords$")
    public void I_know_all_the_connectionRecords() throws Throwable {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c ";
        connectionRecords = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
    }

    @Then("^I can find exactly (\\d+) new connectionRecords")
    public void I_can_find_exactly_N_connectionRecords(int arg1) throws Throwable {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();

        List<ConnectionRecord> newRecords = new ArrayList<ConnectionRecord>();
        for (ConnectionRecord cr : recordList) {
            if (!connectionRecords.contains(cr)) {
                newRecords.add(cr);
            }
        }
        assertThat(newRecords.size(), is(arg1));
        theConnectionId = newRecords.get(0).getConnectionId();

    }

    @Then("^I know the assigned connectionId$")
    public void I_know_the_assigned_connectionId() throws Throwable {
        assertThat(theConnectionId, notNullValue());
        assert (!theConnectionId.isEmpty());

    }

    @Then("^there is a reservation state machine for the assigned connectionId$")
    public void there_is_a_reservation_state_machine_for_the_assigned_connectionId() throws Throwable {

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.findNsiResvSM(theConnectionId);
        assertThat(rsm, notNullValue());

    }

    @Then("^the ResvRequest for connectionId: \"([^\"]*)\" has OscarsOp: \"([^\"]*)\"$")
    public void the_ResvRequest_for_connectionId_has_OscarsOp(String arg1, String arg2) throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        ResvRequest rr = rh.findResvRequest(arg1);
        assertThat(rr, notNullValue());
        assertThat(rr.getOscarsOp().toString(), is(arg2));

    }


    @When("^I wait until the LocalResvTask for connectionId: \"([^\"]*)\" has completed$")
    public void I_wait_until_the_LocalResvTask_for_connectionId_has_completed(String arg1) throws Throwable {
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.findNsiResvSM(arg1);

        while (rsm.getState().value().equals("ReserveChecking")) {
            System.out.println("waiting..");
            Thread.sleep(250);
        }

    }

}
