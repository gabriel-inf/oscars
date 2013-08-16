package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

public class OscarsTeardownTask extends Task  {
    private static final Logger log = Logger.getLogger(OscarsTeardownTask.class);

    private String connId = "";

    public OscarsTeardownTask(String connId) {
        this.scope = "oscars";
        this.connId = connId;
    }

    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            super.onRun();
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);

            RequestHolder rh = RequestHolder.getInstance();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();

            String oscarsGri = cr.getOscarsGri();
            if (oscarsGri == null || oscarsGri.equals("")) {
                throw new TaskException("could not find OSCARS GRI for connId: "+connId);
            }

            TeardownPathContent tp = null;
            try {
                tp = NSI_OSCARS_Translation.makeOscarsTeardown(oscarsGri);
            } catch (TranslationException ex) {
                log.debug(ex);
                log.debug("could not translate NSI request");

            }
            if (tp != null) {
                try {
                    TeardownPathResponseContent reply = OscarsProxy.getInstance().sendTeardown(tp);
                    log.debug("connId: "+connId+"gri: "+reply.getGlobalReservationId());
                    // TODO
                } catch (OSCARSServiceException e) {

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }


}
