package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.DataplaneStatusRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;
import org.apache.log4j.Logger;

import javax.xml.ws.Holder;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ProvisionSteps {

    private static Logger log = Logger.getLogger(ProvisionSteps.class);
    private static DataplaneStatusRecord dsr;

    private int provReqCount;

    @Given("^that I know the count of all pending provisioning requests$")
    public void that_I_know_the_count_of_all_pending_provisioning_requests() throws Throwable {
        RequestHolder rh = RequestHolder.getInstance();
        provReqCount = 0;
        for (SimpleRequest sr : rh.getSimpleRequests().values()) {
            if (sr.getRequestType().equals(SimpleRequestType.PROVISION)) provReqCount++;
        }

    }

    @When("^I submit provision$")
    public void I_submit_provision() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();

        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");
        SimpleRequest commRequest = NSIRequestFactory.getSimpleRequest(connId, corrId, SimpleRequestType.PROVISION);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();

        try {
            log.debug("submitting provision connId:"+connId+" corrId: "+corrId);
            cp.provision(connId, commRequest.getInHeader(), outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);
    }


    @When("^I submit release")
    public void I_submit_release() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();

        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");
        SimpleRequest commRequest = NSIRequestFactory.getSimpleRequest(connId, corrId, SimpleRequestType.RELEASE);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();

        try {
            log.debug("submitting release connId:"+connId+" corrId: "+corrId);
            cp.release(connId, commRequest.getInHeader(), outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);
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

    @When("^I wait up to (\\d+) ms until provMonitor schedules \"([^\"]*)\"$")
    public void I_wait_up_to_ms_until_provMonitor_schedules(Integer ms, String opStr) throws Throwable {
        String connId = HelperSteps.getValue("connId");
        Long timeout = ms.longValue();
        Long elapsed = 0L;

        boolean found = false;

        int interval = 100;
        while ((elapsed < timeout) && !found ) {
            // log.debug ("sleeping "+interval+" ms (elapsed: "+elapsed+") until provMonitor schedules "+actStr);
            Thread.sleep(interval);
            elapsed += interval;
            OscarsOps op = OscarsProvQueue.getInstance().getScheduled(connId);
            if (op != null && op.toString().equals(opStr)) {
                found = true;
            }
        }

        if (elapsed > timeout && found == false) {
            throw new Exception("timed out "+elapsed+" ms waiting for provMonitor to schedule "+opStr);
        }

        log.debug("waited for "+elapsed+" ms until provMonitor scheduled "+opStr);


        assertThat(found, is(true));
    }

    @Then("^the dataplane record \"([^\"]*)\" active$")
    public void dataplane_record_is_active(String is) throws Throwable {
        boolean testTrue = false;
        if (is.equals("is")) {
            testTrue = true;
        }
        assertThat(dsr.isActive(), is(testTrue));
    }



    @Then("^I can get the dataplane record with version (\\d+)$")
    public void get_dataplanerecord(Integer version) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
        ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
        DataplaneStatusRecord dr = null;
        for (DataplaneStatusRecord adr :cr.getDataplaneStatusRecords()) {
            log.debug(connId+" v: "+adr.getVersion()+" a:"+adr.isActive());
            if (adr.getVersion() == version) {
                dr = adr;
            }
        }
        assertThat(dr, notNullValue());
        dsr = dr;
    }
}
