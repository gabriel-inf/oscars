package net.es.oscars.interdomain;

import org.apache.axis2.AxisFault;
import java.rmi.RemoteException;
import java.util.*;


import net.es.oscars.LogWrapper;
import net.es.oscars.oscars.OSCARSStub;
import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.bss.Domain;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.*;

/**
 * Forwarding client.
 */
public class Forwarder  extends Client {
    private LogWrapper log;
    private ReservationManager rm;

    public Forwarder() {
    	rm = new ReservationManager();
    }

    private void setup(Reservation resv, Domain  nextDomain) 
    throws InterdomainException {
        String url =nextDomain.getUrl();
        String catalinaHome = System.getProperty("catalina.home");
        String repo = catalinaHome + "shared/oscars.conf/axis2.repo/";
        this.log.info("Forwarder.setup, next Domain: " + url + "repo: " , repo );
 
        try {
            super.setUp(true, url, repo, repo + "axis2.xml");
        } catch (AxisFault af) {
      		this.log.error("axis setup failed ", af.getMessage());
        	throw new InterdomainException("failed to reach remote domain:" + url +  af.getMessage());
        }
    }

    public CreateReply create(Reservation resv, Domain nextDomain) 
    throws  InterdomainException {
    	
        if (nextDomain == null) { return null; }
        this.log.info("Forwarding createReservation to ", nextDomain.getUrl());
        ForwardReply reply = this.forward("createReservation", resv, nextDomain);
        return reply.getCreateReservation();
    }

    public ResDetails query(Reservation resv, Domain nextDomain)
    throws  InterdomainException {
    	
        if (nextDomain == null) { return null; }
        this.log.info("Forwarding queryReservation to ", nextDomain.getUrl());
        ForwardReply reply = this.forward("queryReservation", resv, nextDomain);
       return reply.getQueryReservation();
    }

    public String cancel(Reservation resv, Domain nextDomain)
    throws  InterdomainException {
    	
        if (nextDomain == null) { return null; }
        this.log.info("Forwarding cancelReservation to ", nextDomain.getUrl());
         ForwardReply reply = this.forward("cancelReservation", resv, nextDomain);
         return reply.getCancelReservation();
    }


    public ForwardReply forward(String operation, Reservation resv, Domain nextDomain)
           throws  InterdomainException {
    	
    	setup(resv, nextDomain);
        String login = resv.getLogin();
        ForwardReply reply = null;
        Forward fwd =  new Forward();;
        ForwardPayload forPayload = new ForwardPayload();
        fwd.setPayloadSender(login);
        forPayload.setContentType(operation);
 	   String url = nextDomain.getUrl();
        try {
            if (operation.equals("createReservation")) {
            	forPayload.setCreateReservation(toCreateRequest(resv));

            } else if (operation.equals("cancelReservation")) {
                ResTag rt = new ResTag();
                rt.setTag(this.rm.toTag(resv));
                forPayload.setCancelReservation(rt);

            } else if (operation.equals("queryReservation")) {
                ResTag rt = new ResTag();
                rt.setTag(this.rm.toTag(resv));         	
                forPayload.setQueryReservation(rt);
 
            }
            fwd.setPayload(forPayload);
            reply = super.forward(fwd);
            return reply;
    } catch (java.rmi.RemoteException e) {
 	   this.log.error("failed to reach remote domain: " + url,  "\n " + e.getMessage() );
 	   throw new InterdomainException("failed to reach remote domain:" + url +  e.getMessage());
 	}	catch (AAAFaultMessageException e) {
 		this.log.error ("AAAFaultMessageException",e.getMessage());
 	   throw new InterdomainException("AAAFaultMessage from :" + url +  e.getMessage());
  	}  catch (BSSFaultMessageException e) {
 		this.log.error ("BSSFaultMessageException",e.getMessage());
  	   throw new InterdomainException("BSSFaultMessage from :" + url +  e.getMessage());
  	} 	
    }
    
    public ResCreateContent toCreateRequest(Reservation resv) {

       ResCreateContent resCont = new ResCreateContent();

        resCont.setSrcHost(resv.getSrcHost());
        resCont.setDestHost(resv.getDestHost());
        //resCont.setStartTime(resv.getStartTime());  convert long to calendar
        //resCont.setEndTime(resv.getEndTime());
        resCont.setBandwidth( resv.getBandwidth().intValue());
        resCont.setProtocol(resv.getProtocol());
        resCont.setDescription(resv.getDescription());
        resCont.setCreateRouteDirection("FORWARD");
        return resCont;
    }

   
}
