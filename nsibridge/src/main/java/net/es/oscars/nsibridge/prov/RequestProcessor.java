package net.es.oscars.nsibridge.prov;


import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.task.QueryTask;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;
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
        log.debug("startReserve for connId: "+connId);

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
        rh.getResvRequests().add(request);


        try {
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ);
            request.getTaskIds().addAll(taskIds);
            for (UUID taskId : request.getTaskIds()) {
                log.debug("   task id:  " +taskId);
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
        rh.getSimpleRequests().add(request);
        Set<UUID> taskIds;
        try {
            switch (request.getRequestType()) {
                case RESERVE_ABORT:
                    taskIds = smh.getResvStateMachines().get(connId).process(NSI_Resv_Event.RECEIVED_NSI_RESV_AB);
                    request.getTaskIds().addAll(taskIds);
                    break;
                case RESERVE_COMMIT:
                    taskIds = smh.getResvStateMachines().get(connId).process(NSI_Resv_Event.RECEIVED_NSI_RESV_CM);
                    request.getTaskIds().addAll(taskIds);
                    break;
                case PROVISION:
                    taskIds = smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ);
                    request.getTaskIds().addAll(taskIds);
                break;
                case RELEASE:
                    taskIds = smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ);
                    request.getTaskIds().addAll(taskIds);
                break;
                case TERMINATE:
                    taskIds= smh.getLifeStateMachines().get(connId).process(NSI_Life_Event.RECEIVED_NSI_TERM_RQ);
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
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();

        for (String connId: request.getQuery().getConnectionId()) {
            QuerySummaryResultType resultType = new QuerySummaryResultType();
            
            //lookup connection record
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if(cr == null){
                //do not fail if we don't find the reservation per the spec
                return result;
            }
            resultType.setConnectionId(connId);
            resultType.setRequesterNSA(cr.getRequesterNSA());
            resultType.setGlobalReservationId(cr.getNsiGlobalGri());
            
            //Set connection states
            ConnectionStatesType cst = new ConnectionStatesType();
            NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
            NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
            NSI_Prov_SM psm = smh.findNsiProvSM(connId);
            LifecycleStateEnumType      lst = (LifecycleStateEnumType) lsm.getState().state();
            ProvisionStateEnumType      pst = (ProvisionStateEnumType) psm.getState().state();
            ReservationStateEnumType    rst = (ReservationStateEnumType) rsm.getState().state();
            cst.setProvisionState(pst);
            cst.setLifecycleState(lst);
            cst.setReservationState(rst);
            resultType.setConnectionStates(cst);
            // TODO: actually implement this
            DataPlaneStatusType dst = new DataPlaneStatusType();
            dst.setActive(true);
            dst.setVersion(1);
            
            //Set details of reservation based on query
            try {
                QueryResContent qc = NSI_OSCARS_Translation.makeOscarsQuery(cr.getOscarsGri());
                QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc);
                if(reply == null || reply.getReservationDetails() == null){
                    throw new ServiceException("No matching OSCARS reservation found with oscars GRI " + cr.getOscarsGri());
                }
                //set description
                resultType.setDescription(reply.getReservationDetails().getDescription());
                //set criteria
                resultType.getCriteria().add(
                        NSI_OSCARS_Translation.makeNSIQuerySummaryCriteria(reply.getReservationDetails()));
            } catch (OSCARSServiceException e) {
                e.printStackTrace();
                throw new ServiceException("Error returned from OSCARS query: " + e.getMessage());
            } catch (TranslationException e) {
                e.printStackTrace();
                throw new ServiceException("Unable to translate query request for OSCARS: " + e.getMessage());
            }

            result.getReservation().add(resultType);
        }



        return result;

    }

    public void asyncQuery(QueryRequest request) throws ServiceException, TaskException {
        // TODO:
        RequestHolder rh = RequestHolder.getInstance();
        rh.getQueryRequests().add(request);


        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        QueryTask queryTask = new QueryTask(request);

        try {
            wf.schedule(queryTask , now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
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
