package net.es.oscars.nsibridge.task;


import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.EventEnumType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import org.apache.log4j.Logger;

public class OscarsSetupTask extends OscarsTask  {
    private static final Logger log = Logger.getLogger(OscarsSetupTask.class);
    public OscarsSetupTask() {
        this.scope = "oscars";
    }


    public void submitOscars(ConnectionRecord cr) throws ServiceException {
        log.info("submitting setup");
        OscarsUtil.submitSetup(cr);
        log.info("submitted setup");

        try {
            OscarsStates os = OscarsUtil.pollUntilResvStable(cr);
            ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);

            DB_Util.updateDataplaneRecord(cr, os, rr.getVersion());
            if (os != OscarsStates.ACTIVE) {
                log.debug("activation failed, saving notification");
                DB_Util.makeNotification(connectionId, EventEnumType.ACTIVATE_FAILED, CallbackMessages.ERROR_EVENT);
            }
        } catch (ServiceException ex) {
            log.error(ex.getMessage(), ex);
            return;
        }

    }


}
