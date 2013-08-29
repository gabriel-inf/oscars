package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RequestProcessor {
    private static final Logger log = Logger.getLogger(RequestProcessor.class);
    private static RequestProcessor instance;
    private RequestProcessor() {}
    public static RequestProcessor getInstance() {
        if (instance == null) instance = new RequestProcessor();
        return instance;
    }

    public void startReserve(ResvRequest request) throws ServiceException {
        String connId = request.getReserveType().getConnectionId();
        String corrId = request.getInHeader().getCorrelationId();

        log.debug("startReserve for connId: "+connId+" corrId:"+corrId);

        NSI_Util.createConnectionRecordIfNeeded(connId, request.getInHeader().getRequesterNSA(), request.getReserveType().getGlobalReservationId());

        if (!NSI_Util.restoreStateMachines(connId)) {
            NSI_Util.makeNewStateMachines(connId);
        }



        if (NSI_Util.needNewOscarsResv(connId)) {
            log.debug("OSCARS create needed for connId: "+connId);
            request.setOscarsOp(OscarsOps.RESERVE);
        } else {
            log.debug("OSCARS modify needed for connId: "+connId);
            request.setOscarsOp(OscarsOps.MODIFY);
        }

        RequestHolder rh = RequestHolder.getInstance();

        rh.getResvRequests().put(corrId, request);


        try {
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ, corrId);
            request.getTaskIds().clear();
            request.getTaskIds().addAll(taskIds);
            for (UUID taskId : request.getTaskIds()) {
                log.debug("connId: "+connId+"  task id:  " +taskId);
            }

        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("resv state machine does not allow transition: "+connId);
        } finally {
            NSI_Util.persistStateMachines(connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);

    }

    public void processSimple(SimpleRequest request) throws ServiceException, TaskException  {
        String connId = request.getConnectionId();
        String corrId = request.getInHeader().getCorrelationId();

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        if (cr == null) {
            throw new ServiceException("internal error: could not find existing connection for new reservation with connectionId: "+connId);
        }

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (!smh.hasStateMachines(connId)) {
            log.info("loading state machines for "+connId);
            if (!NSI_Util.restoreStateMachines(connId)) {
                throw new ServiceException("internal error: could not initialize state machines for connectionId: "+connId);
            }
        }

        RequestHolder rh = RequestHolder.getInstance();
        rh.getSimpleRequests().put(corrId, request);

        Set<UUID> taskIds;
        try {
            switch (request.getRequestType()) {
                case RESERVE_ABORT:
                    taskIds = smh.getResvStateMachines().get(connId).process(NSI_Resv_Event.RECEIVED_NSI_RESV_AB, corrId);
                    request.getTaskIds().addAll(taskIds);
                    break;
                case RESERVE_COMMIT:
                    taskIds = smh.getResvStateMachines().get(connId).process(NSI_Resv_Event.RECEIVED_NSI_RESV_CM, corrId);
                    request.getTaskIds().addAll(taskIds);
                    break;
                case PROVISION:
                    taskIds = smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ, corrId);
                    request.getTaskIds().addAll(taskIds);
                break;
                case RELEASE:
                    taskIds = smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ, corrId);
                    request.getTaskIds().addAll(taskIds);
                break;
                case TERMINATE:
                    taskIds= smh.getLifeStateMachines().get(connId).process(NSI_Life_Event.RECEIVED_NSI_TERM_RQ, corrId);
                    request.getTaskIds().addAll(taskIds);
                break;
            }
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("state machine does not allow transition: "+connId);
        } finally {
            NSI_Util.persistStateMachines(connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }

    public QuerySummaryConfirmedType syncQuerySum(QueryRequest request) throws ServiceException, TaskException {
        QuerySummaryConfirmedType result = new QuerySummaryConfirmedType();
        List<ConnectionRecord> connRecords = new ArrayList<ConnectionRecord>();
        boolean hasFilters = false;
        
        //lookup based on connection ids
        for(String connId : request.getQuery().getConnectionId()){
            hasFilters = true;
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if(cr != null){
                connRecords.add(cr);
            }
        }

        //lookup based on NSA GRI
        for(String gri : request.getQuery().getGlobalReservationId()){
            hasFilters = true;
            connRecords.addAll(NSI_Util.getConnectionRecordsByGri(gri));
        }

        //send list
        if(!hasFilters){
            connRecords = NSI_Util.getConnectionRecords();
        }

        //format result
        for (ConnectionRecord cr: connRecords) {
            try {
                QuerySummaryResultType resultType = NSI_OSCARS_Translation.makeNSIQueryResult(cr);
                result.getReservation().add(resultType);
            } catch (TranslationException e) {
                log.error("Unable to translate reservation: " + e.getMessage());
                //continue so one bad reservation doesn't ruin things for everybody
            }
            
        }

        return result;

    }

    public void asyncQuery(QueryRequest request) throws ServiceException, TaskException {
        // TODO:

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
