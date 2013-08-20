package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

public class OscarsCancelTask extends Task  {

    private String connId = "";
    private static final Logger log = Logger.getLogger(OscarsCancelTask.class);
    public OscarsCancelTask(String connId) {
        this.connId = connId;

        this.scope = "oscars";
    }
    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            super.onRun();
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                throw new TaskException("could not find connection entry for connId: "+connId);
            }


            RequestHolder rh = RequestHolder.getInstance();


            SimpleRequest req = rh.findSimpleRequest(connId);
            String oscarsGri = cr.getOscarsGri();


            if (req != null) {
                log.debug("found request for connId: "+connId);
            }


            CancelResContent rc = NSI_OSCARS_Translation.makeOscarsCancel(oscarsGri);

            try {
                CancelResReply reply = OscarsProxy.getInstance().sendCancel(rc);
                log.debug("cancel status: "+reply.getStatus());
            } catch (OSCARSServiceException e) {

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
