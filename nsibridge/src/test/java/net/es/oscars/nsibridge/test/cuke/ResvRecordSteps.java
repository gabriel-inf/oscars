package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Then;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_Util;
import org.apache.log4j.Logger;
import org.hamcrest.CoreMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class ResvRecordSteps {
    private static List<ResvRecord> rrs = new ArrayList<ResvRecord>();
    private static Logger log = Logger.getLogger(ResvRecordSteps.class);


    @Then("^I can find (\\d+) resvRecord entries$")
    public void I_can_find_resvRecord_entries(int arg1) throws Throwable {
        rrs = new ArrayList<ResvRecord>();

        String connId = HelperSteps.getValue("connId");
        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
        for (ResvRecord rec : cr.getResvRecords()) {
            log.debug(connId+" --- v:"+rec.getVersion()+ " com: "+rec.isCommitted());
            rrs.add(rec);
        }
        assertThat(cr.getResvRecords().size(), is(arg1));

    }



    @Then("^the resvRecord with version (\\d+) \"([^\"]*)\" been committed$")
    public void the_resvRecord_with_version_been_committed(int arg1, String arg2) throws Throwable {
        ResvRecord rrTocheck = null;
        for (ResvRecord rr : rrs) {
            if (rr.getVersion() == arg1) {
                rrTocheck = rr;
                break;
            }
        }
        assertThat (rrTocheck, notNullValue());
        boolean committed = false;

        if (arg2.toLowerCase().trim().equals("has")) {
            committed = true;
        }

        assertThat(rrTocheck.isCommitted(), is(committed));

    }


    @Then("^I can \"([^\"]*)\" the resvRecord entry$")
    public void I_can_the_resvRecord_entry(String arg1) throws Throwable {
        String connId = HelperSteps.getValue("connId");

        if (arg1.equals("commit")) {
            DB_Util.commitResvRecord(connId);


        } else if (arg1.toLowerCase().equals("abort")) {
            DB_Util.abortResvRecord(connId);

        } else {
            throw new Exception("invalid field value: "+arg1);
        }

        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
        List<ResvRecord> uncom = ConnectionRecord.getUncommittedResvRecords(cr);
        assertThat(uncom.size(), is(0));

    }



}
