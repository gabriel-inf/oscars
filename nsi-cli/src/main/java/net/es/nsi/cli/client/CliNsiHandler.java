package net.es.nsi.cli.client;

import net.es.nsi.cli.cmd.NsiCliState;
import net.es.oscars.nsi.soap.util.output.QueryOutputter;
import net.es.oscars.nsi.soap.util.output.QueryPrettyOutputter;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.types.ServiceExceptionType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.types.TypeValuePairListType;
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
    public void errorEvent(String connectionId,
                           long notificationId,
                           XMLGregorianCalendar timeStamp,
                           EventEnumType event,
                           String originatingConnectionId,
                           String originatingNSA,
                           TypeValuePairListType additionalInfo,
                           ServiceExceptionType serviceException,
                           CommonHeaderType header,
                           Holder<CommonHeaderType> header1) throws ServiceException {
        String out = "";
        out += "\nReceived an errorEvent for connectionId: "+connectionId+"\n";
        System.out.println(out);
        queryOutputter.outputFailed(serviceException);
    }
    @Override
    public void messageDeliveryTimeout(String connectionId,
                                       long notificationId,
                                       XMLGregorianCalendar timeStamp,
                                       String correlationId,
                                       CommonHeaderType header,
                                       Holder<CommonHeaderType> header1) throws ServiceException {
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
    public void reserveTimeout(String connectionId,
                               long notificationId,
                               XMLGregorianCalendar timeStamp,
                               int timeoutValue,
                               String originatingConnectionId,
                               String originatingNSA,
                               CommonHeaderType header,
                               Holder<CommonHeaderType> header1) throws ServiceException {
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
        Integer oldV = NsiCliState.getInstance().getResvProfile().getVersion();
        Integer newV = oldV + 1;
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
    public void queryResultConfirmed(List<QueryResultResponseType> result,
                                     CommonHeaderType header,
                                     Holder<CommonHeaderType> header1) throws ServiceException {

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
    public void dataPlaneStateChange(String connectionId,
                                     long notificationId,
                                     XMLGregorianCalendar timeStamp,
                                     DataPlaneStatusType dataPlaneStatus,
                                     CommonHeaderType header,
                                     Holder<CommonHeaderType> header1) throws ServiceException {
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