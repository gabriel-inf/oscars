
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.nsibridge.provider;

import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

import net.es.oscars.nsibridge.bridge.NSAFactory;
import net.es.oscars.nsibridge.soap.gen.ifce.GenericAcknowledgmentType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;
import net.es.oscars.nsibridge.soap.gen.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.util.RequestPrinter;

@SuppressWarnings({ "unused", "restriction" })
@javax.jws.WebService(
                      serviceName = "ConnectionServiceProvider",
                      portName = "ConnectionServiceProviderPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2011/07/connection/provider",
                      wsdlLocation = "file:/Users/haniotak/helios/fenius/nsibridge/schema/ogf_nsi_connection_provider_v1_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.provider.ConnectionProviderPort")
                      
public class ConnectionProviderImpl implements ConnectionProviderPort {

    private static final Logger LOG = Logger.getLogger(ConnectionProviderImpl.class.getName());

    public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation query");
        try {
            String nsaId = parameters.getQuery().getProviderNSA();
            NSAFactory.getInstance().getNSA(nsaId).addQueryRequest(parameters);
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    public GenericAcknowledgmentType reserve(ReserveRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation reservation");
        System.out.println(RequestPrinter.printResvReq(parameters));
        try {
            String nsaId = parameters.getReserve().getProviderNSA();
            NSAFactory.getInstance().getNSA(nsaId).addReservationRequest(parameters);
            
            
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation provision");
        try {
            String nsaId = parameters.getProvision().getProviderNSA();
            NSAFactory.getInstance().getNSA(nsaId).addProvisionRequest(parameters);
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation terminate");
        try {
            String nsaId = parameters.getTerminate().getProviderNSA();
            NSAFactory.getInstance().getNSA(nsaId).addTerminateRequest(parameters);

            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException    { 
        LOG.info("Executing operation release");
        try {
            String nsaId = parameters.getRelease().getProviderNSA();
            NSAFactory.getInstance().getNSA(nsaId).addReleaseRequest(parameters);
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
        System.out.println(parameters);
        try {
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
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
        System.out.println(parameters);
        try {
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            _return.setCorrelationId(parameters.getCorrelationId());
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        */
    }
    


}
