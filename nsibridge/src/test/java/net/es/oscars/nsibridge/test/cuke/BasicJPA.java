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

public class BasicJPA {
    private EntityManager em;
    @Given("^I have initialized JPA")
    public void initializeJPA() {
        em = PersistenceHolder.getInstance().getEntityManager();

    }
    @When("^I insert a new ConnectionRecord with id: \"([^\"]*)\"$")
    public void insertConnectionRecord(String connId) {
        em.getTransaction().begin();
        ConnectionRecord cr = new ConnectionRecord();
        cr.setConnectionId(connId);
        em.persist(cr);
        em.getTransaction().commit();
    }
    @Then("^I can find the record with id: \"([^\"]*)\"$")
    public void checkTheLetter(final String connId) {
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c WHERE c.connectionId  = '"+connId+"'";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();

        assertThat(recordList.size(), is(1));
        assertThat(recordList.get(0).getConnectionId(), is(connId));

    }
}
