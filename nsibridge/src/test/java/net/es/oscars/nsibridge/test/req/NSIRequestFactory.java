package net.es.oscars.nsibridge.test.req;
import net.es.oscars.nsibridge.beans.QueryRequest;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.*;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.*;


import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairListType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairType;
import net.es.oscars.nsibridge.test.cuke.HelperSteps;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;


public class NSIRequestFactory {

    public static QueryRequest getQueryRequest() {
        QueryRequest req = new QueryRequest();
        QueryType q = new QueryType();
        req.setQuery(q);

        CommonHeaderType inHeader = makeHeader(null);
        req.setInHeader(inHeader);
        return req;
    }

    public static SimpleRequest getSimpleRequest(String connectionId, String correlationId, SimpleRequestType type) {
        SimpleRequest pq = new SimpleRequest ();
        pq.setConnectionId(connectionId);
        CommonHeaderType inHeader = makeHeader(correlationId);
        pq.setInHeader(inHeader);
        pq.setRequestType(type);
        return pq;
    }

    public static ResvRequest getRequest() throws DatatypeConfigurationException {
        Long threeMins = 3 * 60 * 1000L;
        Long tenMins = 10 * 60 * 1000L;
        Date now = new Date();
        Date sDate = new Date();
        sDate.setTime(now.getTime() + threeMins);
        Date eDate = new Date();
        eDate.setTime(sDate.getTime() + tenMins);

        ResvRequest req = new ResvRequest();
        ReservationRequestCriteriaType crit = new ReservationRequestCriteriaType();
        // PathType pt = new PathType();
        StpType srcStp = new StpType();
        StpType dstStp = new StpType();
        ScheduleType sch = new ScheduleType();

        XMLGregorianCalendar sTime = asXMLGregorianCalendar(sDate);
        XMLGregorianCalendar eTime = asXMLGregorianCalendar(eDate);


        sch.setStartTime(sTime);
        sch.setEndTime(eTime);
        srcStp.setNetworkId("esnet");
        srcStp.setLocalId("urn:ogf:network:stp:esnet.ets:chi-80");

        dstStp.setLocalId("urn:ogf:network:stp:esnet.ets:ps-80");
        dstStp.setNetworkId("esnet");

        EthernetVlanType evtp = new EthernetVlanType();
        crit.getAny().add(evtp);
        evtp.setCapacity(100);
        evtp.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        evtp.setSourceSTP(srcStp);
        evtp.setDestSTP(dstStp);
        evtp.setSourceVLAN(400);
        evtp.setDestVLAN(500);


        crit.setSchedule(sch);

        String connId = UUID.randomUUID().toString();
        req.setReserveType(new ReserveType());
        req.getReserveType().setConnectionId(connId);
        req.getReserveType().setDescription("test description");
        req.getReserveType().setGlobalReservationId("some GRI");
        req.getReserveType().setCriteria(crit);


        CommonHeaderType inHeader = makeHeader(null);
        req.setInHeader(inHeader);
        return req;
    }

    public static CommonHeaderType makeHeader(String corrId) {
        CommonHeaderType inHeader = new CommonHeaderType();
        inHeader.setProtocolVersion("http://schemas.ogf.org/nsi/2012/03/connection");
        if (corrId == null || corrId.equals("")) {
            inHeader.setCorrelationId("urn:" + UUID.randomUUID().toString());
        } else {
            inHeader.setCorrelationId(corrId);
        }
        if (HelperSteps.getValue("requesterNSA") != null) {
            inHeader.setRequesterNSA(HelperSteps.getValue("requesterNSA"));
        } else {
            inHeader.setRequesterNSA("urn:ogf:network:nsa:starlight");
        }

        if (HelperSteps.getValue("requesterNSA") != null) {
            inHeader.setProviderNSA(HelperSteps.getValue("providerNSA"));
        } else {
            inHeader.setProviderNSA("urn:ogf:network:nsa:esnet");
        }

        if (HelperSteps.getValue("replyTo") != null) {
            inHeader.setReplyTo(HelperSteps.getValue("replyTo"));
        } else {
            inHeader.setReplyTo("http://localhost:8088/ConnectionRequester");
        }
        return inHeader;
    }


    public static XMLGregorianCalendar asXMLGregorianCalendar(java.util.Date date) throws DatatypeConfigurationException {
        DatatypeFactory df = DatatypeFactory.newInstance();

        if (date == null) {
            return null;
        } else {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(date.getTime());
            return df.newXMLGregorianCalendar(gc);
        }
    }

}
