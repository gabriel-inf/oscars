package net.es.oscars.rmi.bss;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.pss.PathSetupManager;

public class EventRmiHandler {
    private OSCARSCore core;
    private Logger log;
    
    final public static String RESOURCE_SCHED_CLASS = "RESERVATION";
    final public static String PATH_SIGNAL_CLASS = "PATH";
    final public static String CREATE_OP = "CREATE";
    final public static String MODIFY_OP = "MODIFY";
    final public static String CANCEL_OP = "CANCEL";
    final public static String SETUP_OP = "SETUP";
    final public static String REFRESH_OP = "REFRESH";
    final public static String TEARDOWN_OP = "TEARDOWN";
    final public static String CONFIRMED_OP_TYPE = "CONFIRMED";
    final public static String COMPLETED_OP_TYPE = "COMPLETED";
    final public static String UPSTREAM_OP_TYPE = "UPSTREAM";
    final public static String DOWNSTREAM_OP_TYPE = "DOWNSTREAM";
    final public static String FAILED_OP_TYPE = "FAILED";
    
    public EventRmiHandler(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }
    
    public void handleEvent(OSCARSEvent event) throws RemoteException{
        this.log.debug("handleEvent.start");
        
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try{
            String producerDomainId = this.checkSubscriptionId(
                    event.getProducerUrl(), event.getSubscriptionId());
            String[] eventType = event.getType().split("_");
            ReservationDAO resvDAO = new ReservationDAO(this.core.getBssDbName());
            if(eventType == null || eventType.length <3){
                this.log.warn("Unknown event " + event.getType());
            }else if(eventType[0].equals(EventRmiHandler.RESOURCE_SCHED_CLASS)){
                this.handleResourceScheduling(eventType[1], eventType[2], producerDomainId, event);
            }else if(event.getType().contains(EventRmiHandler.PATH_SIGNAL_CLASS)){
                this.handlePathSignaling(producerDomainId, event);
            }else{
                this.log.debug("Received unknown event " + eventType);
            }
            Reservation resv = resvDAO.query(event.getReservation().getGlobalReservationId());
            this.core.getStateEngine().safeHibernateCommit(resv, bss);
        }catch(Exception e){
            bss.getTransaction().rollback();
            throw new RemoteException(e.getMessage());
        }
        
    }
    
    private void handleResourceScheduling(String op, String opType, String producerDomainId, OSCARSEvent event) throws BSSException{
        ReservationManager rm = this.core.getReservationManager();
        
        /* Determine the resource scheduling operation to perform */
        String requiredStatus = null;
        if(op.equals(EventRmiHandler.CREATE_OP)){
            requiredStatus = StateEngine.INCREATE;
        }else if(op.equals(EventRmiHandler.MODIFY_OP)){
            requiredStatus = StateEngine.INMODIFY;
        }else if(op.equals(EventRmiHandler.CANCEL_OP)){
            requiredStatus = StateEngine.RESERVED;
        }else{
            this.log.warn("Unknown operation : " + op);
            return;
        }
        
        /* Call reservation manager */
        if(opType.equals(EventRmiHandler.CONFIRMED_OP_TYPE)){
            rm.submitResvJob(producerDomainId, requiredStatus, true, event);
        }else if(opType.equals(EventRmiHandler.COMPLETED_OP_TYPE)){
            rm.submitResvJob(producerDomainId, requiredStatus, false, event);
        }else if(opType.equals(EventRmiHandler.FAILED_OP_TYPE)){
            rm.submitFailed(producerDomainId, requiredStatus, event);
        }else{
            this.log.debug("Discarding resource scheduling event " + event.getType());
        }
    }
    
    private void handlePathSignaling(String producerDomainId, 
            OSCARSEvent event) throws BSSException{
        PathSetupManager pm = this.core.getPathSetupManager();
        /* Determine the path signaling operation to perform */
        String requiredStatus = null;
        String failedEventType = null;
        if(event.getType().contains(EventRmiHandler.SETUP_OP)){
            failedEventType = OSCARSEvent.PATH_SETUP_FAILED;
            requiredStatus = StateEngine.INSETUP;
        }else if(event.getType().contains(EventRmiHandler.TEARDOWN_OP)){
            failedEventType = OSCARSEvent.PATH_TEARDOWN_FAILED;
            requiredStatus = StateEngine.INTEARDOWN;
        }else{
            this.log.warn("Unknown operation : " + event.getType());
            return;
        }
        
        /* Call reservation manager */
        String gri = event.getReservation().getGlobalReservationId();
        if(event.getType().contains(EventRmiHandler.UPSTREAM_OP_TYPE)){
            pm.handleEvent(gri, producerDomainId, requiredStatus, true);
        }else if(event.getType().contains(EventRmiHandler.DOWNSTREAM_OP_TYPE)){
            pm.handleEvent(gri, producerDomainId, requiredStatus, false);
        }else if(event.getType().contains(EventRmiHandler.FAILED_OP_TYPE)){
            pm.handleFailed(gri, producerDomainId, event.getSource(),
               event.getErrorCode(), event.getErrorMessage(), failedEventType);
        }else{
            this.log.debug("Discarding path signaling event " + event.getType());
        }
    }
    
    private String checkSubscriptionId(String producerUrl, 
            String subscriptionId) throws RemoteException{
        ServiceManager sm = this.core.getServiceManager();
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        Domain producer = domainDAO.queryByParam("url", producerUrl);
        if(producer == null){
            throw new RemoteException("Event producer not found in Notify message.");
        }
        EndpointReferenceType smSubscrRef = (EndpointReferenceType) sm.getServiceMapData("NB", producer.getTopologyIdent());
        if(smSubscrRef == null){
            throw new RemoteException("Subscription in Notify message not found.");
        }
        String smSubscrId = smSubscrRef.getReferenceParameters().getSubscriptionId();
        if(smSubscrId == null || subscriptionId == null || (!smSubscrId.equals(subscriptionId))){
            this.log.error("Subscription ID in Notify message invalid.");
            this.log.error("Found " +subscriptionId+", expected " + smSubscrId);
        }
        
        return producer.getTopologyIdent();
    }
}
