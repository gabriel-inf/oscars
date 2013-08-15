package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
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
    private int connCount;

    @Given("^that I have submitted reserve\\(\\) with connectionId: \"([^\"]*)\"$")
    public void that_I_have_submitted_reserve_with_connectionId(String arg1) throws Throwable {
        ResvRequest resvRequest = NSIRequestFactory.getRequest();
        ReserveType rt = resvRequest.getReserveType();
        rt.setConnectionId(arg1);
        ConnectionProvider cp = new ConnectionProvider();
        Holder<String> connHolder = new Holder<String>();
        connHolder.value = rt.getConnectionId();
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        cp.reserve(connHolder, rt.getGlobalReservationId(), rt.getDescription(), rt.getCriteria(), resvRequest.getInHeader(), outHolder);
    }

    @Given("^that I know the count of all ConnectionRecords$")
    public void that_I_know_the_count_of_all_ConnectionRecords() throws Throwable {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c ";
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        connCount = recordList.size();

    }

    @Then("^the count of ConnectionRecords has increased by (\\d+)$")
    public void the_count_of_ConnectionRecords_has_increased_by(int arg1) throws Throwable {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c ";
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        int newCount = recordList.size();
        assertThat(newCount, is(connCount+1));

    }

}
