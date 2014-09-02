package net.es.oscars.nsibridge.prov;


import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.OscarsCertInInterceptor;
import net.es.oscars.nsibridge.soap.impl.OscarsSecurityContext;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.task.QueryRecursiveJob;
import net.es.oscars.nsibridge.task.QuerySummaryJob;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Schedule;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

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

        SubjectAttributes attrs = OscarsSecurityContext.getInstance().getSubjectAttributes();
        if (attrs == null) {
            String msg = "no OSCARS user attributes!";
            log.error(msg);
            throw new ServiceException(msg);
        }



        log.debug("startReserve for connId: "+connId+" corrId:"+corrId);

        DB_Util.createConnectionRecordIfNeeded(connId, request.getInHeader().getRequesterNSA(), request.getReserveType().getGlobalReservationId(), request.getInHeader().getReplyTo(), attrs);

        if (!DB_Util.restoreStateMachines(connId)) {
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


        if (request.getReserveType().getCriteria().getSchedule().getStartTime() == null) {
            try {
                GregorianCalendar gc = new GregorianCalendar();
                XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
                request.getReserveType().getCriteria().getSchedule().setStartTime(xgc);
            } catch (DatatypeConfigurationException ex) {
                log.error(ex);
            }
        }

        if (request.getReserveType().getCriteria().getSchedule().getStartTime() == null) {
            try {
                GregorianCalendar gc = new GregorianCalendar();
                gc.set(2050, 12, 31);
                XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
                request.getReserveType().getCriteria().getSchedule().setStartTime(xgc);
            } catch (DatatypeConfigurationException ex) {
                log.error(ex);
            }
        }



        try {
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
            NSI_Resv_SM rsm = smh.findNsiResvSM(connId);

            NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
            NSI_Life_State lsmState = (NSI_Life_State) lsm.getState();
            LifecycleStateEnumType lsmEnumState = (LifecycleStateEnumType) lsmState.state();

            if (!lsmEnumState.equals(LifecycleStateEnumType.CREATED)) {
                throw new ServiceException("cannot reserve: lifecycle state: "+lsm.getState().value());
            }

            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ, corrId);
            request.getTaskIds().clear();
            request.getTaskIds().addAll(taskIds);
            for (UUID taskId : request.getTaskIds()) {
                log.debug("connId: "+connId+"  task id:  " +taskId);
            }
            try {
                DB_Util.createResvRecord(connId, request.getReserveType());
            } catch (ServiceException ex) {
                log.error(ex);
                throw new ServiceException(ex.getMessage());
            }

        } catch (StateException ex) {
            log.error(ex);
            ServiceException state_error = ServiceExceptionUtil.makeException(ex.getMessage(), connId, "00201");
            throw state_error;
        } finally {
            DB_Util.persistStateMachines(connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);

    }

    public void processSimple(SimpleRequest request) throws ServiceException, TaskException  {
        String connId = request.getConnectionId();
        String corrId = request.getInHeader().getCorrelationId();

        log.debug("processSimple: connId: " + connId + " corrId: " + corrId+ " type: "+request.getRequestType());
        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
        if (cr == null) {
            throw new ServiceException("internal error: could not find existing connection for new reservation with connectionId: "+connId);
        }

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (!smh.hasStateMachines(connId)) {
            log.info("loading state machines for "+connId);
            if (!DB_Util.restoreStateMachines(connId)) {
                throw new ServiceException("internal error: could not initialize state machines for connectionId: "+connId);
            }
        }

        // if we are terminating / terminated, no operations other than query are allowed
        NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
        NSI_Life_State lsmState = (NSI_Life_State) lsm.getState();
        LifecycleStateEnumType lsmEnumState = (LifecycleStateEnumType) lsmState.state();

        if (lsmEnumState.equals(LifecycleStateEnumType.TERMINATED) || lsmEnumState.equals(LifecycleStateEnumType.TERMINATING)) {
            ServiceException state_error = ServiceExceptionUtil.makeException("cannot perform "+request.getRequestType()+" - lifecycle state is "+lsmEnumState.value(), connId, "00201");
            throw state_error;
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
                    taskIds = smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_REL_RQ, corrId);
                    request.getTaskIds().addAll(taskIds);
                break;
                case TERMINATE:
                    taskIds= smh.getLifeStateMachines().get(connId).process(NSI_Life_Event.RECEIVED_NSI_TERM_RQ, corrId);
                    request.getTaskIds().addAll(taskIds);
                    taskIds = smh.getProvStateMachines().get(connId).process(NSI_Prov_Event.RECEIVED_NSI_TERM_RQ, corrId);
                    request.getTaskIds().addAll(taskIds);
                break;
            }
        } catch (StateException ex) {
            log.error(ex);
            ServiceException state_error = ServiceExceptionUtil.makeException(ex.getMessage(), connId, "00201");
            throw state_error;
        } finally {
            DB_Util.persistStateMachines(connId);
        }
        log.debug("processSimple: "+connId+" corrId: "+corrId+" taskIds: "+ StringUtils.join(request.getTaskIds(), ","));

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }

    public QuerySummaryConfirmedType syncQuerySum(QueryRequest request) throws ServiceException, TaskException {
        QuerySummaryConfirmedType result = new QuerySummaryConfirmedType();
        List<ConnectionRecord> connRecords = new ArrayList<ConnectionRecord>();
        boolean hasFilters = false;
        
        //get requester NSA
        if(request.getInHeader() == null || request.getInHeader().getRequesterNSA() == null){
            throw new ServiceException("Request must specify the requester");
        }
        String requester = request.getInHeader().getRequesterNSA();
        
        //lookup based on connection ids
        for(String connId : request.getQuery().getConnectionId()){
            hasFilters = true;
            ConnectionRecord cr = DB_Util.getConnectionRecord(connId, requester);
            if (cr != null){
                connRecords.add(cr);
            } else {
                log.info("no record found for connId: " + request.getQuery().getConnectionId() + " reqNSA: " + requester);
            }
        }

        //lookup based on NSA GRI
        for(String gri : request.getQuery().getGlobalReservationId()){
            hasFilters = true;
            connRecords.addAll(DB_Util.getConnectionRecordsByGri(gri, requester));
        }

        //send list
        if(!hasFilters){
            connRecords = DB_Util.getConnectionRecords(requester);
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

    public void asyncQuery(QueryRequest request) throws ServiceException {
        Schedule ts = Schedule.getInstance();
        String jobId = "asyncQuerySumm-" + UUID.randomUUID().toString();
        
        SimpleTrigger trigger = new SimpleTrigger(jobId, "asyncQuery");
        JobDetail jobDetail = new JobDetail(jobId, "asyncQuery", QuerySummaryJob.class);
        jobDetail.getJobDataMap().put(QuerySummaryJob.PARAM_REQUEST, request);
        try {
            ts.getScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new ServiceException("Unable to schedule query job: " + e.getMessage());
        }

    }

    public void recursiveQuery(QueryRequest request) throws ServiceException {
        Schedule ts = Schedule.getInstance();
        String jobId = "recursiveQuerySumm-" + UUID.randomUUID().toString();

        SimpleTrigger trigger = new SimpleTrigger(jobId, "recursiveQuery");
        JobDetail jobDetail = new JobDetail(jobId, "recursiveQuery", QueryRecursiveJob.class);
        jobDetail.getJobDataMap().put(QueryRecursiveJob.PARAM_REQUEST, request);
        try {
            ts.getScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new ServiceException("Unable to schedule recursive query job: " + e.getMessage());
        }
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
