package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.beans.NSIConnection;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.*;

import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.nsibridge.beans.ResvRequest;
import org.apache.log4j.Logger;

public class ProcNSIResvRequest extends Task  {
    private static final Logger log = Logger.getLogger(ProcNSIResvRequest.class);

    private String connId = "";

    private ProcNSIResvRequest() {}
    public ProcNSIResvRequest(String connId) {
        this.scope = "nsi";
        this.connId = connId;
    }

    public void onRun() throws TaskException {
        super.onRun();
        log.debug(this.id + " starting");




        RequestHolder rh = RequestHolder.getInstance();
        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();


        ResvRequest req = rh.findResvRequest(connId);
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
        NSIConnection conn = ch.findConnection(connId);


        if (conn != null) {
            log.debug("found connection entry for connId: "+connId);
        }

        if (req != null) {
            log.debug("found request for connId: "+connId);
        }
        if (rsm != null) {
            log.debug("found state machine for connId: "+connId);
        }


        ResCreateContent rc = NSI_OSCARS_Translation.makeOscarsResv(req);

        try {
            CreateReply reply = OscarsProxy.getInstance().sendCreate(rc);
            if (reply.getStatus().equals("FAILED")) {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_FAILED);
            } else {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_CONFIRMED);
            }
        } catch (OSCARSServiceException e) {
            try {
                rsm.process(NSI_Resv_Event.LOCAL_RESV_FAILED);
            } catch (StateException e1) {
                e.printStackTrace();
            }
            e.printStackTrace();
        } catch (StateException e) {
            e.printStackTrace();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
