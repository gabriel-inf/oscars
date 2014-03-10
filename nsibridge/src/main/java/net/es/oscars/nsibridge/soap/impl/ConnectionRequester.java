package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.requester.ConnectionRequesterPort;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.types.*;

@WebService(
        serviceName = "ConnectionServiceRequester",
        portName = "ConnectionServiceRequesterPort",
        targetNamespace = "http://schemas.ogf.org/nsi/2013/12/connection/requester",
//        wsdlLocation = "file:schema/2013_07/ogf_nsi_connection_requester_v2_0.wsdl",
        endpointInterface = "net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.requester.ConnectionRequesterPort")

public class ConnectionRequester implements ConnectionRequesterPort {

    private static final Logger LOG = Logger.getLogger(ConnectionRequester.class.getName());


    public void error(
            @WebParam(name = "serviceException", targetNamespace = "")
            ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true)
            CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true)
            javax.xml.ws.Holder<CommonHeaderType> header1
    ) throws ServiceException {

    }


    @Override
    public void reserveConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId, @WebParam(name = "description", targetNamespace = "") String description, @WebParam(name = "criteria", targetNamespace = "") ReservationConfirmCriteriaType criteria, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }

    @Override
    public void dataPlaneStateChange(String connectionId, long notificationId, XMLGregorianCalendar timeStamp, DataPlaneStatusType dataPlaneStatus, CommonHeaderType header, Holder<CommonHeaderType> header1) throws ServiceException {

    }

    @Override
    public void reserveTimeout(String connectionId, long notificationId, XMLGregorianCalendar timeStamp, int timeoutValue, String originatingConnectionId, String originatingNSA, CommonHeaderType header, Holder<CommonHeaderType> header1) throws ServiceException {

    }


    @Override
    public void reserveFailed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }

    @Override
    public void messageDeliveryTimeout(String connectionId, long notificationId, XMLGregorianCalendar timeStamp, String correlationId, CommonHeaderType header, Holder<CommonHeaderType> header1) throws ServiceException {

    }

    @Override
    public void reserveCommitConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }

    @Override
    public void reserveCommitFailed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }

    @Override
    public void errorEvent(String connectionId, long notificationId, XMLGregorianCalendar timeStamp, EventEnumType event, String originatingConnectionId, String originatingNSA, TypeValuePairListType additionalInfo, ServiceExceptionType serviceException, CommonHeaderType header, Holder<CommonHeaderType> header1) throws ServiceException {

    }

    @Override
    public void queryResultConfirmed(List<QueryResultResponseType> result, CommonHeaderType header, Holder<CommonHeaderType> header1) throws ServiceException {

    }

    @Override
    public void reserveAbortConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }


    @Override
    public void provisionConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }


    @Override
    public void terminateConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }


    @Override
    public void releaseConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }








    @Override
    public GenericAcknowledgmentType queryNotificationConfirmed(@WebParam(partName = "queryNotificationConfirmed", name = "queryNotificationConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/connection/types") QueryNotificationConfirmedType queryNotificationConfirmed, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }


    @Override
    public void querySummaryConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QuerySummaryResultType> reservation, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }


    @Override
    public void queryRecursiveConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QueryRecursiveResultType> reservation, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
    }







}