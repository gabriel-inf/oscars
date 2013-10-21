package net.es.oscars.nsibridge.client.cli.handlers;

import net.es.oscars.nsibridge.client.cli.output.QueryOutputter;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;


import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.ifce.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.headers.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.types.*;

@WebService(
        serviceName = "ConnectionServiceRequester",
        portName = "ConnectionServiceRequesterPort",
        targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/requester",
        endpointInterface = "net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort")

// wsdlLocation = "file:schema/2013_07/ogf_nsi_connection_requester_v2_0.wsdl",

public class QueryHandler implements ConnectionRequesterPort {
    private QueryOutputter outputter;
    
    public QueryHandler(QueryOutputter outputter){
        super();
        this.outputter = outputter;
    }
    @Override
    public void querySummaryFailed(
            @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        this.outputter.outputFailed(serviceException);
        HandlerExitUtil.exit(1);
    }

    @Override
    public void querySummaryConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QuerySummaryResultType> reservation, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        this.outputter.outputSummary(reservation);
        HandlerExitUtil.exit(0);
    }

    @Override
    public void queryRecursiveFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        this.outputter.outputFailed(serviceException);
        HandlerExitUtil.exit(1);
    }

    @Override
    public void queryRecursiveConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QueryRecursiveResultType> reservation, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        this.outputter.outputRecursive(reservation);
        HandlerExitUtil.exit(0);
    }

    /**
     * IGNORE METHODS BELOW NOT HANDLED BY QUERIES
     */
    public void error(
            @WebParam(name = "serviceException", targetNamespace = "")
            ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
            CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
            javax.xml.ws.Holder<CommonHeaderType> header1
    ) throws ServiceException {

    }
    @Override
    public void errorEvent(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "notificationId", targetNamespace = "") int notificationId,
            @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp,
            @WebParam(name = "event", targetNamespace = "") EventEnumType event,
            @WebParam(name = "additionalInfo", targetNamespace = "") TypeValuePairListType additionalInfo,
            @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public void messageDeliveryTimeout(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "correlationId", targetNamespace = "") String correlationId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId, @WebParam(name = "description", targetNamespace = "") String description, @WebParam(name = "criteria", targetNamespace = "") ReservationConfirmCriteriaType criteria, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveTimeout(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "timeoutValue", targetNamespace = "") int timeoutValue, @WebParam(name = "originatingConnectionId", targetNamespace = "") String originatingConnectionId, @WebParam(name = "originatingNSA", targetNamespace = "") String originatingNSA, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveFailed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveCommitConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveCommitFailed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveAbortConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void provisionConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void terminateConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void releaseConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dataPlaneStateChange(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "dataPlaneStatus", targetNamespace = "") DataPlaneStatusType dataPlaneStatus, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void queryNotificationFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public GenericAcknowledgmentType queryNotificationConfirmed(@WebParam(partName = "queryNotificationConfirmed", name = "queryNotificationConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types") QueryNotificationConfirmedType queryNotificationConfirmed, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}