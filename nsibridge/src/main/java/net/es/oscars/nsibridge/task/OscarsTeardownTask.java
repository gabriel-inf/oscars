package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import org.apache.log4j.Logger;

public class OscarsTeardownTask extends OscarsTask {
    private static final Logger log = Logger.getLogger(OscarsTeardownTask.class);

    public void submitOscars(ConnectionRecord cr) throws ServiceException {
        log.info("submitting setup");
        OscarsUtil.submitTeardown(cr);
        log.info("submitted setup");
    }



}
