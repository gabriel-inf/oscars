
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider;

import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.7.6
 * 2013-07-23T11:23:01.069-07:00
 * Generated source version: 2.7.6
 * 
 */

@javax.jws.WebService(
                      serviceName = "ConnectionServiceProvider",
                      portName = "ConnectionServiceProviderPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/provider",
                      wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/2013_04/ConnectionService/ogf_nsi_connection_provider_v2_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort")
                      
public class ConnectionProviderPortImpl implements ConnectionProviderPort {

    private static final Logger LOG = Logger.getLogger(ConnectionProviderPortImpl.class.getName());

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#reserveAbort(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void reserveAbort(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation reserveAbort");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#queryNotificationSync(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryNotificationType  queryNotificationSync ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header )*
     */
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryNotificationConfirmedType queryNotificationSync(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryNotificationType queryNotificationSync,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.QueryNotificationSyncFailed    { 
        LOG.info("Executing operation queryNotificationSync");
        System.out.println(queryNotificationSync);
        System.out.println(header.value);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryNotificationConfirmedType _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.QueryNotificationSyncFailed("queryNotificationSyncFailed...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#queryNotification(java.lang.String  connectionId ,)java.lang.Integer  startNotificationId ,)java.lang.Integer  endNotificationId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void queryNotification(java.lang.String connectionId,java.lang.Integer startNotificationId,java.lang.Integer endNotificationId,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation queryNotification");
        System.out.println(connectionId);
        System.out.println(startNotificationId);
        System.out.println(endNotificationId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#queryRecursive(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType  queryRecursive ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header )*
     */
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.GenericAcknowledgmentType queryRecursive(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType queryRecursive,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation queryRecursive");
        System.out.println(queryRecursive);
        System.out.println(header.value);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.GenericAcknowledgmentType _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#querySummary(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType  querySummary ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header )*
     */
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.GenericAcknowledgmentType querySummary(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType querySummary,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation querySummary");
        System.out.println(querySummary);
        System.out.println(header.value);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.GenericAcknowledgmentType _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#reserveCommit(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void reserveCommit(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation reserveCommit");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#provision(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void provision(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation provision");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#release(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void release(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation release");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#querySummarySync(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType  querySummarySync ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header )*
     */
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QuerySummaryConfirmedType querySummarySync(net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QueryType querySummarySync,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.QuerySummarySyncFailed    { 
        LOG.info("Executing operation querySummarySync");
        System.out.println(querySummarySync);
        System.out.println(header.value);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.QuerySummaryConfirmedType _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.QuerySummarySyncFailed("querySummarySyncFailed...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#terminate(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void terminate(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation terminate");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort#reserve(java.lang.String  connectionId ,)java.lang.String  globalReservationId ,)java.lang.String  description ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.ReservationRequestCriteriaType  criteria ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType  header1 )*
     */
    public void reserve(javax.xml.ws.Holder<java.lang.String> connectionId,java.lang.String globalReservationId,java.lang.String description,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.ReservationRequestCriteriaType criteria,net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException    { 
        LOG.info("Executing operation reserve");
        System.out.println(connectionId.value);
        System.out.println(globalReservationId);
        System.out.println(description);
        System.out.println(criteria);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException("serviceException...");
    }

}
