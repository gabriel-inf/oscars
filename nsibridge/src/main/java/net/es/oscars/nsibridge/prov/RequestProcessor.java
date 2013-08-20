package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.ifces.StateException;
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
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;

public class RequestProcessor {
    private static final Logger log = Logger.getLogger(RequestProcessor.class);
    private static RequestProcessor instance;
    private EntityManager em = PersistenceHolder.getInstance().getEntityManager();
    private RequestProcessor() {}
    public static RequestProcessor getInstance() {
        if (instance == null) instance = new RequestProcessor();
        return instance;
    }

    public void startReserve(ResvRequest request) throws ServiceException, TaskException {
        String connId = request.getReserveType().getConnectionId();

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();

        rh.getResvRequests().add(request);

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        if (cr != null) {
            log.info("preexisting connection record found while starting reserve() for connectionId: " + connId);
        } else {
            log.info("creating new connection record for connectionId: "+connId);
            em.getTransaction().begin();
            smh.makeStateMachines(connId);
            cr = new ConnectionRecord();
            cr.setConnectionId(connId);
            em.persist(cr);
            em.getTransaction().commit();
        }


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

    public void processSimple(SimpleRequest request) throws ServiceException, TaskException  {
        String connId = request.getConnectionId();

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        if (cr == null) {
            throw new ServiceException("internal error: could not find existing connection for new reservation with connectionId: "+connId);
        }

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();
        rh.getSimpleRequests().add(request);

        try {
            switch (request.getRequestType()) {
                case RESERVE_ABORT:
                    smh.getResvStateMachines().get(connId).process(NSI_Resv_Event.RECEIVED_NSI_RESV_AB);
                    break;
                case RESERVE_COMMIT:
                    smh.getResvStateMachines().get(connId).process(NSI_Resv_Event.RECEIVED_NSI_RESV_CM);
                    break;
                case PROVISION:
                    smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ);
                break;
                case RELEASE:
                    smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ);
                break;
                case TERMINATE:
                    smh.getLifeStateMachines().get(connId).process(NSI_Life_Event.RECEIVED_NSI_TERM_RQ);
                break;
            }
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("state machine does not allow transition: "+connId);
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

            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            resultType.setConnectionId(connId);
            ConnectionStatesType cst = new ConnectionStatesType();
            resultType.setConnectionStates(cst);
            NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
            NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
            NSI_Prov_SM psm = smh.findNsiProvSM(connId);

            LifecycleStateEnumType      lst = (LifecycleStateEnumType) lsm.getState().state();
            ProvisionStateEnumType      pst = (ProvisionStateEnumType) psm.getState().state();
            ReservationStateEnumType    rst = (ReservationStateEnumType) rsm.getState().state();
            DataPlaneStatusType         dst = new DataPlaneStatusType();
            dst.setActive(true);
            dst.setVersion(1);
            cst.setProvisionState(pst);
            cst.setLifecycleState(lst);
            cst.setReservationState(rst);

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
