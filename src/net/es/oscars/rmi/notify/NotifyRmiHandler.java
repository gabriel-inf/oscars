package net.es.oscars.rmi.notify;

import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.*;
import org.hibernate.*;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;
import org.w3.www._2005._08.addressing.ReferenceParametersType;

import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.oscars.ReservationAdapter;
import net.es.oscars.oscars.PathSetupAdapter;
import net.es.oscars.wsdlTypes.EventContent;

public class NotifyRmiHandler {
    private OSCARSCore core;
    private Logger log = Logger.getLogger(NotifyRmiHandler.class);


    public NotifyRmiHandler() {
        this.core = OSCARSCore.getInstance();
    }
    public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) throws RemoteException {
        ServiceManager sm = this.core.getServiceManager();
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        Domain producer = domainDAO.queryByParam("url", address);
        if(producer == null){
            this.log.error("Event producer not found in Notify message.");
            return null;
        }
        EndpointReferenceType subRef = (EndpointReferenceType) sm.getServiceMapData("NB", producer.getTopologyIdent());
        if(subRef == null){
            this.log.error("Subscription in Notify message not found.");
            return null;
        }
        ReferenceParametersType refParams = subRef.getReferenceParameters();
        ReferenceParametersType msgSubRefParams = msgSubRef.getReferenceParameters();
        if(refParams == null || msgSubRefParams == null){
            this.log.error("No reference parameters for Notify message.");
            return null;
        }
        String subId = refParams.getSubscriptionId();
        String msgSubId = msgSubRefParams.getSubscriptionId();
        if(subId == null || msgSubId == null || (!subId.equals(msgSubId))){
            this.log.error("Subscription ID in Notify message invalid.");
            this.log.error("Found " +msgSubId+", expected " + subId);
            return null;
        }

        return producer.getTopologyIdent();
	}
    
    public void Notify(Notify request) throws RemoteException {
        this.log.info("Received Notify");
        NotificationMessageHolderType[] holders = request.getNotificationMessage();
        ReservationAdapter resAdapter = new ReservationAdapter();
        PathSetupAdapter psAdapter = new PathSetupAdapter();
        
        for (NotificationMessageHolderType holder : holders) {
            EndpointReferenceType prodRef = holder.getProducerReference();
            String address = prodRef.getAddress().toString();
            Session bss = core.getBssSession();
            bss.beginTransaction();
            String producerId = this.checkSubscriptionId(address, holder.getSubscriptionReference());
            if (producerId == null){
                return;
            }
            this.log.info("Found subscription for " + address);
            MessageType message = holder.getMessage();
            OMElement[] omEvents = message.getExtraElement();
            for (OMElement omEvent : omEvents) {
                try {
                    EventContent event = EventContent.Factory.parse(omEvent.getXMLStreamReaderWithoutCaching());
                    String eventType = event.getType();
                    if (eventType.startsWith("RESERVATION_CREATE")) {
                        resAdapter.handleEvent(event, producerId, StateEngine.INCREATE);
                    } else if (eventType.startsWith("RESERVATION_MODIFY")) {
                    	resAdapter.handleEvent(event, producerId, StateEngine.INMODIFY);
                    } else if (eventType.startsWith("RESERVATION_CANCEL")) {
                    	resAdapter.handleEvent(event, producerId, StateEngine.RESERVED);
                    } else if (eventType.contains("PATH_SETUP")) {
                    	psAdapter.handleEvent(event, producerId, StateEngine.INSETUP);
                    } else if (eventType.contains("PATH_REFRESH")) {

                    } else if (eventType.contains("PATH_TEARDOWN")) {
                    	psAdapter.handleEvent(event, producerId, StateEngine.INTEARDOWN);
                    } else {
                        this.log.debug("Received unkown event " + eventType);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    continue;
                }
             }
             bss.getTransaction().commit();
        }
    }

}
