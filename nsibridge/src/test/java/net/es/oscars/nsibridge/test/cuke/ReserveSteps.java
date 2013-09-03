package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ReserveSteps {
    private int resvReqCount;
    private EntityManager em = PersistenceHolder.getEntityManager();
    private List<ConnectionRecord> connectionRecords;
    private String theConnectionId;
    private static Logger log = Logger.getLogger(ReserveSteps.class);

    @When("^I submit reserve$")
    public void I_submit_reserve() throws Throwable {
        ResvRequest resvRequest = HelperSteps.getResvRequest();
        ReserveType rt = resvRequest.getReserveType();

        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");

        resvRequest.getInHeader().setCorrelationId(corrId);
        rt.setConnectionId(connId);
        ConnectionProvider cp = new ConnectionProvider();
        Holder<String> connHolder = new Holder<String>();
        connHolder.value = rt.getConnectionId();
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        try {
            cp.reserve(connHolder, rt.getGlobalReservationId(), rt.getDescription(), rt.getCriteria(), resvRequest.getInHeader(), outHolder);
            log.info("submitted reserve for connection id: "+connHolder.value);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);

    }


    @When("^I submit reserveCommit$")
    public void I_submit_reserveCommit() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();
        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");


        SimpleRequest commRequest = NSIRequestFactory.getSimpleRequest(connId, corrId, SimpleRequestType.RESERVE_COMMIT);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        try {
            cp.reserveCommit(connId, commRequest.getInHeader(), outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);

    }

    @When("^I submit reserveAbort")
    public void I_submit_abort() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();
        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");

        CommonHeaderType inHeader = NSIRequestFactory.makeHeader(corrId);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        try {
            cp.reserveAbort(connId, inHeader, outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);
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

    @Then("^the ResvRequest has OscarsOp: \"([^\"]*)\"$")
    public void the_ResvRequest_has_OscarsOp(String oscarsOp) throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        String corrId = HelperSteps.getValue("corrId");

        ResvRequest rr = rh.findResvRequest(corrId);
        assertThat(rr, notNullValue());
        assertThat(rr.getOscarsOp().toString(), is(oscarsOp));

    }



    private static double prevResvTimeout;
    @When("^I set the reserveTimeout to (\\d+) seconds")
    public void I_set_the_reserveTimeout_to_ms(Integer arg1) throws Throwable {

        ApplicationContext ax = SpringContext.getInstance().getContext();

        TimingConfig tx = ax.getBean("timingConfig", TimingConfig.class);
        prevResvTimeout = tx.getResvTimeout();
        tx.setResvTimeout(arg1.doubleValue());

    }

    @Then("^I restore the reserveTimeout value$")
    public void I_restore_the_reserveTimeout_value() throws Throwable {

        ApplicationContext ax = SpringContext.getInstance().getContext();

        TimingConfig tx = ax.getBean("timingConfig", TimingConfig.class);
        tx.setResvTimeout(prevResvTimeout);
    }


}
