package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.NSIConnection;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

public class RequestProcessor {
    private static final Logger log = Logger.getLogger(RequestProcessor.class);
    private static RequestProcessor instance;
    private RequestProcessor() {}
    public static RequestProcessor getInstance() {
        if (instance == null) instance = new RequestProcessor();
        return instance;
    }

    public void startReserve(ResvRequest request) throws ServiceException, TaskException {
        String connId = request.getConnectionId();

        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();

        rh.getResvRequests().add(request);

        NSIConnection conn = ch.findConnection(request.getConnectionId());
        if (conn != null) {
            throw new ServiceException("internal error: found existing connection for new reservation with connectionId: "+connId);
        }

        smh.makeStateMachines(connId);

        conn = new NSIConnection();
        conn.setConnectionId(connId);
        ch.getConnections().add(conn);

        try {
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
            rsm.process(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ);
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("resv state machine does not allow transition: "+connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);

    }

    private CommonHeaderType makeOutHeader(CommonHeaderType inHeader) {
        CommonHeaderType outHeader = new CommonHeaderType();
        outHeader.setCorrelationId(inHeader.getCorrelationId());
        outHeader.setProtocolVersion(inHeader.getProtocolVersion());
        outHeader.setProviderNSA(inHeader.getProviderNSA());
        outHeader.setRequesterNSA(inHeader.getRequesterNSA());

        return outHeader;

    }




}
