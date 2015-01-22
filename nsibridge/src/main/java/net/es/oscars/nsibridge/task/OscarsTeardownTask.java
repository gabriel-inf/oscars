package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;

import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import org.apache.log4j.Logger;


public class OscarsTeardownTask extends OscarsTask {
    private static final Logger log = Logger.getLogger(OscarsTeardownTask.class);

    public void submitOscars(ConnectionRecord cr) throws ServiceException {
        log.info("submitting teardown");
        OscarsUtil.submitTeardown(cr);
        log.info("submitted teardown");

        try {
            OscarsStates os = OscarsUtil.pollUntilResvStable(cr);
            ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);

            DB_Util.updateDataplaneRecord(cr, os, rr.getVersion());
        } catch (ServiceException ex) {
            log.error(ex.getMessage(), ex);
            return;
        }
    }



}
