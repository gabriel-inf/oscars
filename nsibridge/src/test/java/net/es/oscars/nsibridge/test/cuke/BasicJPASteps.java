package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BasicJPASteps {
    private EntityManager em = PersistenceHolder.getEntityManager();
    private int connCount;

    @When("^I insert a new ConnectionRecord$")
    public void insertConnectionRecord() {
        String connId = HelperSteps.getValue("connId");

        em.getTransaction().begin();
        ConnectionRecord cr = new ConnectionRecord();
        cr.setConnectionId(connId);
        em.persist(cr);
        em.getTransaction().commit();
    }

    @Given("^the count of ConnectionRecords is (\\d+)$")
    public void the_count_of_ConnectionRecords_is(int count) throws Throwable {
        em.getTransaction().begin();
        String connId = HelperSteps.getValue("connId");

        String query = "SELECT c FROM ConnectionRecord c WHERE c.connectionId  = '"+connId+"'";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();

        assertThat(recordList.size(), is(count));
        if (count > 0) {
            assertThat(recordList.get(0).getConnectionId(), is(connId));
        }
    }

    @Given("^that I know the count of all ConnectionRecords$")
    public void that_I_know_the_count_of_all_ConnectionRecords() throws Throwable {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c ";
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        connCount = recordList.size();

    }

    @Then("^the count of all ConnectionRecords has changed by (\\d+)$")
    public void the_count_of_ConnectionRecords_has_changed_by(int arg1) throws Throwable {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c ";
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        int newCount = recordList.size();
        assertThat(newCount, is(connCount+arg1));

    }

    @Then("^I can delete the ConnectionRecord$")
    public void deleteConnectionRecord() {
        String connId = HelperSteps.getValue("connId");

        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c WHERE c.connectionId  = '"+connId+"'";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();

        em.remove(recordList.get(0));
        em.getTransaction().commit();
    }





}
