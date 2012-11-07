package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.NSIConnection;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.ifces.SM_Event;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.LeafProviderModel;
import net.es.oscars.nsibridge.state.PSM_Event;
import net.es.oscars.nsibridge.state.PSM_TransitionHandler;
import net.es.oscars.nsibridge.state.ProviderSM;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RequestProcessor {
    private static final Logger log = Logger.getLogger(RequestProcessor.class);
    private static RequestProcessor instance;
    private RequestProcessor() {}
    public static RequestProcessor getInstance() {
        if (instance == null) instance = new RequestProcessor();
        return instance;
    }

    public void process(ResvRequest request) throws ServiceException {
        String connId = request.getConnectionId();

        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        PSM_Holder smh = PSM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();

        rh.getResvRequests().add(request);



        NSIConnection conn = ch.findConnection(request.getConnectionId());
        if (conn != null) {
            throw new ServiceException("internal error: found existing connection for new reservation with connectionId: "+connId);
        }
        ProviderSM psm = smh.findStateMachine(connId);
        if (psm != null) {
            throw new ServiceException("internal error: found existing state machine for new reservation: "+connId);
        }

        conn = new NSIConnection();
        conn.setConnectionId(connId);
        ch.getConnections().add(conn);

        ProviderSM sm = new ProviderSM(request.getConnectionId());
        PSM_TransitionHandler th = new PSM_TransitionHandler();
        th.setMdl(new LeafProviderModel());
        smh.getStateMachines().add(sm);
        try {
            sm.process(PSM_Event.RSV_RQ);
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("internal state machine does not allow transition: "+connId);
        }
    }




}
