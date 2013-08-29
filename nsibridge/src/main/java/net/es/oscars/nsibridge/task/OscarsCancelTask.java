package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import org.apache.log4j.Logger;

public class OscarsCancelTask extends OscarsTask  {
    private static final Logger log = Logger.getLogger(OscarsCancelTask.class);


    public void submitOscars(ConnectionRecord cr) throws ServiceException {
        log.info("submitting cancel");
        OscarsUtil.submitCancel(cr);
        log.info("submitted cancel");
    }



}
