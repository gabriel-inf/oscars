package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ConnectionStatesType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QueryType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class QuerySteps {
    private EntityManager em = PersistenceHolder.getEntityManager();
    @Then("^querySummarySync\\(\\) returns resvState \"([^\"]*)\"$")
    public void querySummarySync_with_connectionId_returns_resvState(String resvState) throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();
        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");

        QueryType qt = new QueryType();
        CommonHeaderType ht = NSIRequestFactory.makeHeader(corrId);

        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        outHolder.value = ht;
        qt.getConnectionId().add(connId);

        QuerySummaryConfirmedType qst = cp.querySummarySync(qt, outHolder);

        assertThat(qst, notNullValue());
        assertThat(qst.getReservation(), notNullValue());
        assertThat(qst.getReservation().size(), is(1));

        String retConnId = qst.getReservation().get(0).getConnectionId();
        assertThat(qst.getReservation().get(0).getConnectionId(), is(connId));
        ConnectionStatesType cst = qst.getReservation().get(0).getConnectionStates();
        assertThat(cst, notNullValue());
        assertThat(cst.getReservationState(), notNullValue());
        assertThat(cst.getReservationState().value(), is(resvState));

    }


}
