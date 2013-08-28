package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.OscarsConfig;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.utils.task.sched.Workflow;
import org.junit.Assert;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OscarsSteps {
    private OscarsProxy op;
    private String gri;
    private EntityManager em = PersistenceHolder.getEntityManager();

    @Given("^I have set up the OSCARS proxy$")
    public void I_have_set_up_the_OSCARS_proxy() throws Throwable {
        op = OscarsProxy.getInstance();

        ApplicationContext ax = SpringContext.getInstance().getContext();
        OscarsConfig oc =  ax.getBean("oscarsConfig", OscarsConfig.class);
        OscarsStubConfig os = ax.getBean("oscarsStubConfig", OscarsStubConfig.class);

        op.setOscarsConfig(oc);
        OscarsProxy.getInstance().setOscarsStubConfig(os);

        assertThat(op.getOscarsConfig(), notNullValue());
        assertThat(op.getOscarsStubConfig(), notNullValue());

    }

    @When("^submit a new OSCARS reserve\\(\\) request$")
    public void submit_a_new_OSCARS_reserve_request() throws Throwable {
        ResCreateContent rcc = new ResCreateContent();
        CreateReply cr = op.sendCreate(rcc);
        gri = cr.getGlobalReservationId();
    }

    @Then("^I can save the new OSCARS GRI in a connectionRecord for connectionId: \"([^\"]*)\"$")
    public void I_can_save_the_new_OSCARS_GRI_in_a_connectionRecord_for_connectionId(String connId) throws Throwable {

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);

        assertThat(cr, nullValue());

        em.getTransaction().begin();
        cr = new ConnectionRecord();
        cr.setConnectionId(connId);
        cr.setOscarsGri(gri);
        em.persist(cr);
        em.getTransaction().commit();

        assertThat(cr, notNullValue());

    }

    @When("^I submit an OSCARS query for connectionId: \"([^\"]*)\"$")
    public void I_submit_an_OSCARS_query_for_connectionId(String connId) throws Throwable {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());

        QueryResContent qc = NSI_OSCARS_Translation.makeOscarsQuery(connGri);
        QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc);
        EntityManager em = PersistenceHolder.getEntityManager();

        OscarsStatusRecord or = new OscarsStatusRecord();
        em.getTransaction().begin();
        or.setDate(new Date());
        or.setStatus(reply.getReservationDetails().getStatus());
        cr.getOscarsStatusRecords().add(or);
        em.persist(cr);
        em.getTransaction().commit();

    }


    @Then("^I have saved an OSCARS state for connectionId: \"([^\"]*)\"$")
    public void I_have_saved_an_OSCARS_state_for_connectionId(String connId) throws Throwable {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());
        OscarsStatusRecord  or = ConnectionRecord.getLatestStatusRecord(cr);
        assertThat(or, notNullValue());
    }

    @Then("^the latest OSCARS state for connectionId: \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_latest_OSCARS_state_for_connectionId_is(String connId, String state) throws Throwable {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());
        OscarsStatusRecord  or = ConnectionRecord.getLatestStatusRecord(cr);
        assertThat(or, notNullValue());
        assertThat(or.getStatus(), is(state));

    }

    @When("^I set the OSCARS stub state for connectionId: \"([^\"]*)\" to \"([^\"]*)\"$")
    public void I_set_the_OSCARS_stub_state_for_connectionId_to(String connId, String oscarsState) throws Throwable {
        OscarsProxy op = OscarsProxy.getInstance();
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());
        op.getStubStates().put(connGri, OscarsStates.valueOf(oscarsState));

   }

    @Then("^I know the OSCARS gri for connectionId: \"([^\"]*)\"$")
    public void I_know_the_OSCARS_gri_for_connectionId(String arg1) throws Throwable {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(arg1);

        Assert.assertThat(cr.getOscarsGri(), notNullValue());
    }


}
