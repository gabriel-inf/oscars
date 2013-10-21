package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.DataplaneStatusRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.ifces.StateMachineType;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.EventEnumType;
import net.es.oscars.nsibridge.task.*;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;


public class NSI_UP_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_UP_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private static final Logger log = Logger.getLogger(NSI_UP_Prov_Impl.class);
    private NSI_UP_Prov_Impl() {}



    @Override
    public UUID localProv(String correlationId) {
        UUID taskId = null;
        log.debug("local prov start");

        long now = new Date().getTime();

        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();

        SMTransitionTask sm = new SMTransitionTask();
        sm.setCorrelationId(correlationId);
        sm.setConnectionId(connectionId);
        sm.setSmt(StateMachineType.PSM);
        sm.setSuccessEvent(NSI_Prov_Event.LOCAL_PROV_CONFIRMED);

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            taskId = wf.schedule(sm , when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        log.debug("local prov scheduled taskId: "+taskId);
        return taskId;
    }

    @Override
    public UUID sendProvCF(String correlationId) {
        log.debug("sendProvCF start");
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.PROV_CF);

        UUID taskId = null;
        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }


    @Override
    public UUID localRel(String correlationId) {
        UUID taskId = null;
        log.debug("local release start");

        long now = new Date().getTime();




        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        Workflow wf = Workflow.getInstance();

        SMTransitionTask sm = new SMTransitionTask();
        sm.setCorrelationId(correlationId);
        sm.setConnectionId(connectionId);

        sm.setSmt(StateMachineType.PSM);
        sm.setSuccessEvent(NSI_Prov_Event.LOCAL_REL_CONFIRMED);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            taskId = wf.schedule(sm , when);
        } catch (TaskException e) {
            log.error(e);
        }
        log.debug("local release scheduled taskId: "+taskId);

        return taskId;

    }

    @Override
    public UUID sendRelCF(String correlationId) {
        log.debug("sendRelCF start");
        long now = new Date().getTime();
        UUID taskId = null;

        Workflow wf = Workflow.getInstance();

        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.REL_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            log.error(e);
        }
        return taskId;

    }


    // failures
    @Override
    public UUID notifyProvFL(String correlationId) {
        log.debug("notifyProvFL start");
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.ERROR);


        UUID taskId = null;

        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            log.error(e);
        }
        return taskId;

    }


    @Override
    public UUID notifyRelFL(String correlationId) {
        long now = new Date().getTime();
        UUID taskId = null;


        Workflow wf = Workflow.getInstance();
        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.ERROR);


        try {
            taskId = wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            log.error(e);
        }
        return taskId;
    }


    @Override
    public UUID dataplaneUpdate(String correlationId) {
        log.debug("dataplaneUpdate connId:"+connectionId);
        try {
            ConnectionRecord cr = DB_Util.getConnectionRecord(connectionId);
            OscarsStatusRecord or = cr.getOscarsStatusRecord();
            if (or == null) {
                log.debug("no oscars record found for "+connectionId);
                return null;
            }


            ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
            DataplaneStatusRecord dr = new DataplaneStatusRecord();

            dr.setVersion(rr.getVersion());
            dr.setActive(false);

            if (or.getStatus().equals("ACTIVE")) {
               dr.setActive(true);
            }
            boolean found = false;
            for (DataplaneStatusRecord aRecord : cr.getDataplaneStatusRecords()) {
                if (aRecord.getVersion() == dr.getVersion()) {
                    aRecord.setActive(dr.isActive());
                    found = true;
                }
            }
            if (!found) {
                cr.getDataplaneStatusRecords().add(dr);
                log.debug("added a new dataplane record for connId: "+cr.getConnectionId()+" v:"+dr.getVersion()+" a:"+dr.isActive());
            } else {
                log.debug("updated dataplane record for connId: "+cr.getConnectionId()+" v:"+dr.getVersion()+" a:"+dr.isActive());

            }
            EntityManager em = PersistenceHolder.getEntityManager();
            em.getTransaction().begin();
            em.persist(cr);
            em.getTransaction().commit();


            SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
            sendNsiMsg.setCorrId(correlationId);
            sendNsiMsg.setConnId(connectionId);
            sendNsiMsg.setMessage(CallbackMessages.DATAPLANE_CHANGE);

            Workflow wf = Workflow.getInstance();
            long now = new Date().getTime();
            UUID taskId = null;
            try {
                Long notId = DB_Util.makeNotification(connectionId, null, CallbackMessages.DATAPLANE_CHANGE);
                sendNsiMsg.setNotificationId(notId);
                taskId = wf.schedule(sendNsiMsg, now + 1000);
            } catch (TaskException e) {
                log.error(e);
            }
            return taskId;

        } catch (ServiceException ex) {
            log.error(ex);
        }
        return null;
    }
}
