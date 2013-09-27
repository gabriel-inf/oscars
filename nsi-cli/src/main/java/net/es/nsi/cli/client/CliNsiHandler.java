package net.es.nsi.cli.client;

import net.es.nsi.cli.cmd.NsiCliState;
import net.es.oscars.nsibridge.client.cli.output.QueryOutputter;
import net.es.oscars.nsibridge.client.cli.output.QueryPrettyOutputter;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairListType;
import org.apache.log4j.Logger;

import javax.jws.WebParam;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.util.List;

public class CliNsiHandler implements ConnectionRequesterPort {
    private static final Logger log = Logger.getLogger(CliNsiHandler.class);
    private static final QueryOutputter queryOutputter = new QueryPrettyOutputter();
    public CliNsiHandler() {
        log.debug("initialized handler");
    }
    @Override
    public void querySummaryFailed(
            @WebParam(name = "serviceException", targetNamespace = "")
                ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        String out = "";
        out += "\nReceived a querySummaryFailed\n";
        System.out.println(out);
        queryOutputter.outputFailed(serviceException);
    }

    @Override
    public void querySummaryConfirmed(
            @WebParam(name = "reservation", targetNamespace = "")
                List<QuerySummaryResultType> reservation,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        String out = "";
        out += "\nReceived querySummaryConfirmed\n";
        queryOutputter.outputSummary(reservation);
        System.out.println(out);

    }

    @Override
    public void queryRecursiveFailed(
            @WebParam(name = "serviceException", targetNamespace = "")
                ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        String out = "";
        out += "\nReceived a queryRecursiveFailed\n";
        System.out.println(out);
        queryOutputter.outputFailed(serviceException);
    }

    @Override
    public void queryRecursiveConfirmed(
            @WebParam(name = "reservation", targetNamespace = "")
                List<QueryRecursiveResultType> reservation,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        String out = "";
        out += "\nReceived queryRecursiveConfirmed\n";
        System.out.println(out);
        queryOutputter.outputRecursive(reservation);
    }

    public void error(
            @WebParam(name = "serviceException", targetNamespace = "")
                ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        System.out.println("\nReceived error\n");
        queryOutputter.outputFailed(serviceException);
    }

    @Override
    public void errorEvent(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "notificationId", targetNamespace = "") int notificationId,
            @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp,
            @WebParam(name = "event", targetNamespace = "") EventEnumType event,
            @WebParam(name = "additionalInfo", targetNamespace = "") TypeValuePairListType additionalInfo,
            @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        String out = "";
        out += "\nReceived an errorEvent for connectionId: "+connectionId+"\n";
        System.out.println(out);
        queryOutputter.outputFailed(serviceException);
    }

    @Override
    public void messageDeliveryTimeout(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "notificationId", targetNamespace = "") int notificationId,
            @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp,
            @WebParam(name = "correlationId", targetNamespace = "") String correlationId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        System.out.println("\nReceived messageDeliveryTimeout for connectionId: "+connectionId+"\n");
    }

    @Override
    public void reserveConfirmed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId,
            @WebParam(name = "description", targetNamespace = "") String description,
            @WebParam(name = "criteria", targetNamespace = "") ReservationConfirmCriteriaType criteria,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        System.out.println("\nReceived reserveConfirmed for connectionId: "+connectionId+" (set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setConfirmed(connectionId, true);
    }

    @Override
    public void reserveTimeout(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "notificationId", targetNamespace = "") int notificationId,
            @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp,
            @WebParam(name = "timeoutValue", targetNamespace = "") int timeoutValue,
            @WebParam(name = "originatingConnectionId", targetNamespace = "") String originatingConnectionId,
            @WebParam(name = "originatingNSA", targetNamespace = "") String originatingNSA,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        System.out.println("\nReceived reserveTimeout for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setConfirmed(connectionId, false);
    }

    @Override
    public void reserveFailed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates,
            @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        System.out.println("\nReceived reserveFailed for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setConfirmed(connectionId, false);
    }

    @Override
    public void reserveCommitConfirmed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        System.out.println("\nReceived reserveCommitConfirmed for connectionId: "+connectionId+" (set as current).\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setCommitted(connectionId, true);
        int oldV = NsiCliState.getInstance().getResvProfile().getVersion();
        int newV = oldV++;
        NsiCliState.getInstance().getResvProfile().setVersion(newV);
        System.out.println("\nNext reserve with this profile will have version: "+newV+"\n");
    }

    @Override
    public void reserveCommitFailed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates,
            @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        System.out.println("\nReceived reserveCommitFailed for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setCommitted(connectionId, false);
    }

    @Override
    public void reserveAbortConfirmed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        System.out.println("\nReceived reserveAbortConfirmed for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setCommitted(connectionId, false);
        NsiCliState.getInstance().setConfirmed(connectionId, false);
    }

    @Override
    public void provisionConfirmed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {
        System.out.println("\nReceived provisionConfirmed for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setProvisioned(connectionId, true);
    }

    @Override
    public void terminateConfirmed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        System.out.println("\nReceived terminateConfirmed for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setProvisioned(connectionId, false);
        NsiCliState.getInstance().setCommitted(connectionId, false);
        NsiCliState.getInstance().setConfirmed(connectionId, false);
    }

    @Override
    public void releaseConfirmed(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {

        System.out.println("\nReceived releaseConfirmed for connectionId: "+connectionId+" (was set as current)\n");
        NsiCliState.getInstance().setConnectionId(connectionId);
        NsiCliState.getInstance().setProvisioned(connectionId, false);
    }

    @Override
    public void dataPlaneStateChange(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "notificationId", targetNamespace = "") int notificationId,
            @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp,
            @WebParam(name = "dataPlaneStatus", targetNamespace = "") DataPlaneStatusType dataPlaneStatus,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header1)
                    throws ServiceException {

        String statusStr = "";
        statusStr += "version: "+dataPlaneStatus.getVersion();
        if (dataPlaneStatus.isActive()) {
            statusStr += ", active";
        } else {
            statusStr += ", not active";
        }

        if (dataPlaneStatus.isVersionConsistent()) {
            statusStr += ", consistent\n";
        } else {
            statusStr += ", not consistent\n";
        }
        System.out.println("\nReceived dataPlaneStateChange for connectionId: "+connectionId+" (was set as current)\n");
        System.out.println("New dataplane status: "+statusStr);
        NsiCliState.getInstance().setConnectionId(connectionId);
    }

    @Override
    public void queryNotificationFailed(
            @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        String out = "";
        out += "\nReceived queryNotificationFailed\n";
        queryOutputter.outputFailed(serviceException);
    }
    @Override
    public GenericAcknowledgmentType queryNotificationConfirmed(
            @WebParam(partName = "queryNotificationConfirmed", name = "queryNotificationConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types")
                QueryNotificationConfirmedType queryNotificationConfirmed,
            @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
                Holder<CommonHeaderType> header)
                    throws ServiceException {
        String out = "";
        out += "\nReceived queryNotificationConfirmed\n";
        GenericAcknowledgmentType gat = new GenericAcknowledgmentType();
        return gat;
    }
}