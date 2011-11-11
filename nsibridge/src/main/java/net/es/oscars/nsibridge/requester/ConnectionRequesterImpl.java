package net.es.oscars.nsibridge.requester;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.log4j.Logger;

import net.es.oscars.nsibridge.bridge.NSAFactory;
import net.es.oscars.nsibridge.soap.gen.ifce.ForcedEndRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.GenericAcknowledgmentType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.requester.ConnectionRequesterPort;

/**
 * This class was generated by Apache CXF 2.4.0
 * 2011-09-11T19:11:04.310-07:00
 * Generated source version: 2.4.0
 * 
 */

@SuppressWarnings({ "unused" })
@javax.jws.WebService(
                      serviceName = "ConnectionServiceRequester",
                      portName = "ConnectionServiceRequesterPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2011/07/connection/requester",
                      wsdlLocation = "file:/Users/haniotak/helios/fenius/nsibridge/schema/ogf_nsi_connection_requester_v1_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.requester.ConnectionRequesterPort")
                      
public class ConnectionRequesterImpl implements ConnectionRequesterPort {

    private static final Logger LOG = Logger.getLogger(ConnectionRequesterImpl.class);
    
    
    public GenericAcknowledgmentType reserveConfirmed(ReserveConfirmedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation reservationConfirmed");
        try {
            String nsaId = parameters.getReserveConfirmed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).reserveConfirmed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;

        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public GenericAcknowledgmentType reserveFailed(ReserveFailedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation reservationFailed");
        try {
            String nsaId = parameters.getReserveFailed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).reserveFailed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }


    public GenericAcknowledgmentType provisionConfirmed(ProvisionConfirmedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation provisionConfirmed");
        try {
            String nsaId = parameters.getProvisionConfirmed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).provisionConfirmed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public GenericAcknowledgmentType provisionFailed(ProvisionFailedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation provisionFailed");
        System.out.println(parameters);
        try {
            String nsaId = parameters.getProvisionFailed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).provisionFailed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public GenericAcknowledgmentType releaseConfirmed(ReleaseConfirmedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation releaseConfirmed");
        try {
            String nsaId = parameters.getReleaseConfirmed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).releaseConfirmed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }


    public GenericAcknowledgmentType releaseFailed(ReleaseFailedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation releaseFailed");
        try {
            String nsaId = parameters.getReleaseFailed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).releaseFailed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }


    public GenericAcknowledgmentType terminateConfirmed(TerminateConfirmedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation terminateConfirmed");
        try {
            String nsaId = parameters.getTerminateConfirmed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).terminateConfirmed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public GenericAcknowledgmentType terminateFailed(TerminateFailedRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation terminateFailed");
        try {
            String nsaId = parameters.getTerminateFailed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).terminateFailed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public GenericAcknowledgmentType queryConfirmed(QueryConfirmedRequestType parameters) throws ServiceException    { 
        throw new ServiceException("operation not supported");
        /*
        LOG.info("Executing operation queryConfirmed");
        try {
            String nsaId = parameters.getQueryConfirmed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).queryConfirmed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }*/
    }

    public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException    { 
        throw new ServiceException("operation not supported");
        /*
        LOG.info("Executing operation query");
        try {
            GenericAcknowledgmentType _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        */
    }
    public GenericAcknowledgmentType queryFailed(QueryFailedRequestType parameters) throws ServiceException    { 
        throw new ServiceException("operation not supported");
        /*
        LOG.info("Executing operation queryFailed");
        try {
            String nsaId = parameters.getQueryFailed().getRequesterNSA();
            NSAFactory.getInstance().getNSA(nsaId).queryFailed(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }*/
    }
    
    public GenericAcknowledgmentType forcedEnd(ForcedEndRequestType parameters) throws ServiceException    { 
        throw new ServiceException("operation not supported");
        /*
        LOG.info("Executing operation forcedEnd");
        System.out.println(parameters);
        try {
            GenericAcknowledgmentType _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        */
    }
    

}
