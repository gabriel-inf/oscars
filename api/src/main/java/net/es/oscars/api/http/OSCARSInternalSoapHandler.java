
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.api.http;

import java.util.logging.Logger;

import net.es.oscars.api.forwarder.ForwarderFactory;
import net.es.oscars.api.forwarder.Forwarder;
import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;

import net.es.oscars.utils.soap.OSCARSServiceException;
import  net.es.oscars.utils.svc.ServiceNames;


@javax.jws.WebService(
        serviceName = ServiceNames.SVC_API_INTERNAL,
        portName = "OSCARSInternalPortType",
        targetNamespace = "http://oscars.es.net/OSCARS/06",
        endpointInterface = "net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class OSCARSInternalSoapHandler implements OSCARSInternalPortType {

    private static final Logger LOG = Logger.getLogger(OSCARSInternalPortTypeImpl.class.getName());

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#createPath(net.es.oscars.api.soap.gen.v06.CreatePathContent  createPath ,)java.lang.String  destDomainId )*
     */
    public CreatePathResponseContent createPath(CreatePathContent createPath,String destDomainId) throws OSCARSFaultMessage    { 
        LOG.info("Executing operation createPath");
        System.out.println(createPath);
        System.out.println(destDomainId);
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString()); 
        }
        
        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }
        
        try {
            return forwarder.createPath(createPath);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#notify(net.es.oscars.api.soap.gen.v06.EventContent  notify ,)java.lang.String  destDomainId )*
     */
    public void interDomainEvent(InterDomainEventContent interDomainEvent,String destDomainId) {
        LOG.info("Executing operation notify");
        System.out.println(interDomainEvent);
        System.out.println(destDomainId);
        Forwarder forwarder = null;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            LOG.info (e.toString()); 
        }
        
        if (forwarder == null) {
            LOG.info ("no forwarder for " + destDomainId);
        }
      
        try {
            forwarder.notify(interDomainEvent);
        } catch (OSCARSServiceException e) {
            LOG.info (e.toString());
        }
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#cancelReservation(net.es.oscars.api.soap.gen.v06.GlobalReservationId  cancelReservation ,)java.lang.String  destDomainId )*
     */
    public CancelResReply cancelReservation(CancelResContent cancelReservation,String destDomainId) throws OSCARSFaultMessage    { 
        LOG.info("Executing operation cancelReservation");
        System.out.println(cancelReservation);
        System.out.println(destDomainId);
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString()); 
        }
        
        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }
        
        try {
            return forwarder.cancelReservation (cancelReservation);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#teardownPath(net.es.oscars.api.soap.gen.v06.TeardownPathContent  teardownPath ,)java.lang.String  destDomainId )*
     */
    public TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath,String destDomainId) throws OSCARSFaultMessage    { 
        LOG.info("Executing operation teardownPath");
        System.out.println(teardownPath);
        System.out.println(destDomainId);
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString()); 
        }
        
        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }
        
        try {
            return forwarder.teardownPath (teardownPath);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#queryReservation(net.es.oscars.api.soap.gen.v06.GlobalReservationId  queryReservation ,)java.lang.String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.QueryResReply queryReservation(QueryResContent queryReservation,java.lang.String destDomainId) throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    { 
        LOG.info("Executing operation queryReservation");
        System.out.println(queryReservation);
        System.out.println(destDomainId);
        try {
            net.es.oscars.api.soap.gen.v06.QueryResReply _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.common.soap.gen.OSCARSFaultMessage("OSCARSFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#queryReservation(net.es.oscars.api.soap.gen.v06.GlobalReservationId  queryReservation ,)java.lang.String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.GetErrorReportResponseContent getErrorReport(GetErrorReportContent getErrorReportReq,
                                                                                       java.lang.String destDomainId)
            throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    {
        LOG.info("Executing operation getErrorReport");
        System.out.println("getErrorReport");
        System.out.println(destDomainId);
        try {
            GetErrorReportResponseContent _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.common.soap.gen.OSCARSFaultMessage("OSCARSFaultMessage...");
    }
    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#createReservation(net.es.oscars.api.soap.gen.v06.ResCreateContent  createReservation ,)java.lang.String  destDomainId )*
     */
    public CreateReply createReservation(ResCreateContent createReservation,String destDomainId) throws OSCARSFaultMessage { 
        LOG.info("Executing operation createReservation");
        System.out.println(createReservation);
        System.out.println(destDomainId);
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString()); 
        }
        
        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }

        try {
            return forwarder.createReservation(createReservation);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#listReservations(net.es.oscars.api.soap.gen.v06.ListRequest  listReservations ,)java.lang.String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.ListReply listReservations(ListRequest listReservations,java.lang.String destDomainId) throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    { 
        LOG.info("Executing operation listReservations");
        System.out.println(listReservations);
        System.out.println(destDomainId);
        try {
            net.es.oscars.api.soap.gen.v06.ListReply _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.common.soap.gen.OSCARSFaultMessage("OSCARSFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#refreshPath(net.es.oscars.api.soap.gen.v06.RefreshPathContent  refreshPath ,)java.lang.String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.RefreshPathResponseContent refreshPath(RefreshPathContent refreshPath,java.lang.String destDomainId) throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    { 
        LOG.info("Executing operation refreshPath");
        System.out.println(refreshPath);
        System.out.println(destDomainId);
        try {
            net.es.oscars.api.soap.gen.v06.RefreshPathResponseContent _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.common.soap.gen.OSCARSFaultMessage("OSCARSFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#getNetworkTopology(net.es.oscars.api.soap.gen.v06.GetTopologyContent  getNetworkTopology ,)java.lang.String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.GetTopologyResponseContent getNetworkTopology(GetTopologyContent getNetworkTopology,java.lang.String destDomainId) throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    { 
        LOG.info("Executing operation getNetworkTopology");
        System.out.println(getNetworkTopology);
        System.out.println(destDomainId);
        try {
            net.es.oscars.api.soap.gen.v06.GetTopologyResponseContent _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.common.soap.gen.OSCARSFaultMessage("OSCARSFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType#modifyReservation(net.es.oscars.api.soap.gen.v06.ModifyResContent  modifyReservation ,)java.lang.String  destDomainId )*
     */
    public ModifyResReply modifyReservation(ModifyResContent modifyReservation,String destDomainId) throws OSCARSFaultMessage    { 
        LOG.info("Executing operation modifyReservation");
        System.out.println(modifyReservation);
        System.out.println(destDomainId);
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString()); 
        }
        
        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }
        
        try {
            return forwarder.modifyReservation (modifyReservation);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }
    }

}
