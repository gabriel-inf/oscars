package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Then;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.prov.NSI_Util;
import org.apache.log4j.Logger;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class ResvRecordSteps {
    private static ResvRecord rr;
    private static Logger log = Logger.getLogger(ResvRecordSteps.class);


    @Then("^I can find (\\d+) resvRecord entries$")
    public void I_can_find_resvRecord_entries(int arg1) throws Throwable {

        String connId = HelperSteps.getValue("connId");
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        for (ResvRecord rec : cr.getResvRecords()) {
            // log.debug(connId+" --- v:"+rec.getVersion()+ " com: "+rec.isCommitted());
            rr = rec;
        }
        assertThat(cr.getResvRecords().size(), is(arg1));

        if (arg1 > 0) assertThat(rr, notNullValue());

    }

    @Then("^the resvRecord committed field is \"([^\"]*)\"$")
    public void the_resvRecord_committed_field_is(String arg1) throws Throwable {
        if (arg1.toLowerCase().equals("true")) {
            assertThat(rr.isCommitted(), is(true));

        } else if (arg1.toLowerCase().equals("false")) {
            assertThat(rr.isCommitted(), is(false));

        } else {
            throw new Exception("invalid field value: "+arg1);
        }
    }



    @Then("^I can \"([^\"]*)\" the resvRecord entry$")
    public void I_can_the_resvRecord_entry(String arg1) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        if (arg1.equals("commit")) {
            NSI_Util.commitResvRecord(connId);


        } else if (arg1.toLowerCase().equals("abort")) {
            NSI_Util.abortResvRecord(connId);

        } else {
            throw new Exception("invalid field value: "+arg1);
        }

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        List<ResvRecord> uncom = ConnectionRecord.getUncommittedResvRecords(cr);
        assertThat(uncom.size(), is(0));

    }



}
