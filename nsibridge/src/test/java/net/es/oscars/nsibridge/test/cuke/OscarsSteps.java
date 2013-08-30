package net.es.oscars.nsibridge.test.cuke;

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
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;
import org.junit.Assert;

import javax.persistence.EntityManager;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OscarsSteps {
    private String gri;
    private EntityManager em = PersistenceHolder.getEntityManager();
    private static Logger log = Logger.getLogger(OscarsSteps.class);


    @When("^submit a new OSCARS reserve\\(\\) request$")
    public void submit_a_new_OSCARS_reserve_request() throws Throwable {
        OscarsProxy op = OscarsProxy.getInstance();
        ResCreateContent rcc = new ResCreateContent();
        CreateReply cr = op.sendCreate(rcc);
        gri = cr.getGlobalReservationId();
    }

    @Then("^I can save the new OSCARS GRI in a connectionRecord$")
    public void I_can_save_the_new_OSCARS_GRI_in_a_connectionRecord_for_connectionId() throws Throwable {
        String connId = HelperSteps.getValue("connId");

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

    @When("^I submit an OSCARS query$")
    public void I_submit_an_OSCARS_query_for_connectionId() throws Throwable {
        String connId = HelperSteps.getValue("connId");
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
        cr.setOscarsStatusRecord(or);
        em.persist(cr);
        em.getTransaction().commit();

    }


    @Then("^I have saved an OSCARS state$")
    public void I_have_saved_an_OSCARS_state_for_connectionId() throws Throwable {
        String connId = HelperSteps.getValue("connId");

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());
        OscarsStatusRecord or = cr.getOscarsStatusRecord();
        assertThat(or, notNullValue());
    }

    @Then("^the latest OSCARS state is \"([^\"]*)\"$")
    public void the_latest_OSCARS_state_for_connectionId_is(String state) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());
        OscarsStatusRecord or = cr.getOscarsStatusRecord();
        assertThat(or, notNullValue());
        assertThat(or.getStatus(), is(state));

    }

    @When("^I set the OSCARS stub state to \"([^\"]*)\"$")
    public void I_set_the_OSCARS_stub_state_for_connectionId_to(String oscarsState) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        OscarsProxy op = OscarsProxy.getInstance();
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        assertThat(cr, notNullValue());
        String connGri = cr.getOscarsGri();
        assertThat(connGri, notNullValue());
        op.getStubStates().put(connGri, OscarsStates.valueOf(oscarsState));

   }

    @Then("^I know the OSCARS gri$")
    public void I_know_the_OSCARS_gri() throws Throwable {
        String connId = HelperSteps.getValue("connId");

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        boolean haveOscarsGri = (cr.getOscarsGri() != null && !cr.getOscarsGri().equals(""));

        Assert.assertThat(haveOscarsGri, is(true));
    }


    @When("^I wait up to (\\d+) ms until I know the OSCARS gri$")
    public void I_wait_up_to_ms_until_I_know_the_OSCARS_gri(Integer ms) throws Throwable {
        String connId = HelperSteps.getValue("connId");
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        Long timeout = ms.longValue();
        Long elapsed = 0L;
        int interval = 1000;

        boolean haveOscarsGri = (cr.getOscarsGri() != null && !cr.getOscarsGri().equals(""));
        while ((elapsed < timeout) && !haveOscarsGri) {

            log.debug ("sleeping "+interval+" ms (elapsed: "+elapsed+") until I know oscarsGri for "+connId);
            Thread.sleep(interval);
            elapsed += interval;
            cr = NSI_Util.getConnectionRecord(connId);

            haveOscarsGri = (cr.getOscarsGri() != null && !cr.getOscarsGri().equals(""));
        }
        if (elapsed > timeout && !haveOscarsGri) {
            throw new Exception("timed out waiting for OSCARS GRI");
        }

        Assert.assertThat(haveOscarsGri, is(true));
    }


}
