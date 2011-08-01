package net.es.oscars.api.compat;

import java.lang.RuntimeException;
import java.lang.reflect.Member;
import java.net.NetPermission;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.es.oscars.api.soap.gen.v05.ResDetails;
import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.w3c.dom.Element;

public class DataTranslator05 {

    public static net.es.oscars.api.soap.gen.v06.CreateReply translate(net.es.oscars.api.soap.gen.v05.CreateReply createReply05)
            throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v06.CreateReply createReply06 = new net.es.oscars.api.soap.gen.v06.CreateReply();
        net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraint06 = new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userRequestConstraint06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        // These elements are required
        if(createReply05.getGlobalReservationId() != null){
            createReply06.setGlobalReservationId(createReply05.getGlobalReservationId());
        }else{
            throw new OSCARSServiceException("Unable to translate v05.CreateReply: GRI is null");
        }
        
        if(createReply05.getStatus() != null){
            createReply06.setStatus(createReply05.getStatus());
        }else{
            throw new OSCARSServiceException("Unable to translate v05.CreateReply: status is null");
        }
        /*
        if (createReply05.getPathInfo() != null) {
            reservedConstraint06.setPathInfo(translate(createReply05.getPathInfo()));
            userRequestConstraint06.setPathInfo(translate(createReply05.getPathInfo()));
        }else{
            throw new OSCARSServiceException("Unable to translate v05.CreateReply:pathInfo is null");
        }
        
        createReply06.setReservedConstraint(reservedConstraint06);
        createReply06.setUserRequestConstraint(userRequestConstraint06);
        */
        // These elements may be null
        createReply06.setToken(createReply05.getToken());

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createReply06.setMessageProperties(msgProps);

        return createReply06;
    }

    public static net.es.oscars.api.soap.gen.v05.CreateReply translate(net.es.oscars.api.soap.gen.v06.CreateReply createReply06)
            throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.CreateReply createReply05 = new net.es.oscars.api.soap.gen.v05.CreateReply();

        try {    // These elements are required
            createReply05.setGlobalReservationId(createReply06.getGlobalReservationId());
            createReply05.setStatus(createReply06.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.6 CreateReply");
        }

        // These elements may be null
        if (createReply06.getToken() != null) {
            createReply05.setToken(createReply06.getToken());
        }
        /*
        if (createReply06.getReservedConstraint().getPathInfo() != null) {
            createReply05.setPathInfo(translate(createReply06.getReservedConstraint().getPathInfo()));
        }
        */
        return createReply05;
    }

    public static net.es.oscars.api.soap.gen.v06.ResCreateContent translate(net.es.oscars.api.soap.gen.v05.ResCreateContent createReservation05,
             String src) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v06.ResCreateContent createReservation06 = new net.es.oscars.api.soap.gen.v06.ResCreateContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userConstraints06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06 = new net.es.oscars.api.soap.gen.v06.PathInfo();

        try {     // These elements are required
            createReservation06.setDescription(createReservation05.getDescription());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.5 ResCreateContent");
        }

        try {    // These elements are required
            userConstraints06.setBandwidth(createReservation05.getBandwidth());
            userConstraints06.setStartTime(createReservation05.getStartTime());
            userConstraints06.setEndTime(createReservation05.getEndTime());
        } catch (NumberFormatException e) {
            throw new OSCARSServiceException("Unable to translate 0.5 ResCreateContent");
        }

        // These elements may be null
        if (createReservation05.getGlobalReservationId() != null) {
            createReservation06.setGlobalReservationId(createReservation05.getGlobalReservationId());
        }

        try {  // These elements are required
            pathInfo06 = translate(createReservation05.getPathInfo());
            userConstraints06.setPathInfo(pathInfo06);
            createReservation06.setUserRequestConstraint(userConstraints06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.5 ResCreateContent");
        }

        if (src != null) {
            // This is a resCreateContent created by a 0.5 IDC (Forward message). Needs to fill up reservedConstraints
            net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraints06 =
                    new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();

            reservedConstraints06.setBandwidth(createReservation05.getBandwidth());
            reservedConstraints06.setStartTime(createReservation05.getStartTime());
            reservedConstraints06.setEndTime(createReservation05.getEndTime());
            reservedConstraints06.setPathInfo(pathInfo06);
            createReservation06.setReservedConstraint(reservedConstraints06);
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createReservation06.setMessageProperties(msgProps);
        return createReservation06;
    }

    public static net.es.oscars.api.soap.gen.v05.ResCreateContent translate(net.es.oscars.api.soap.gen.v06.ResCreateContent resCreateContent06,
            String src) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ResCreateContent resCreateContent05 = new net.es.oscars.api.soap.gen.v05.ResCreateContent();

        try {  // These elements are required
            resCreateContent05.setDescription(resCreateContent06.getDescription());
            resCreateContent05.setBandwidth(resCreateContent06.getReservedConstraint().getBandwidth());
            resCreateContent05.setStartTime(resCreateContent06.getReservedConstraint().getStartTime());
            resCreateContent05.setEndTime(resCreateContent06.getReservedConstraint().getEndTime());
            resCreateContent05.setPathInfo(translate(resCreateContent06.getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.6 ResCreateContent");
        }

        // These elements may be null
        if (resCreateContent06.getGlobalReservationId() != null) {
            resCreateContent05.setGlobalReservationId(resCreateContent06.getGlobalReservationId());
        }
        return resCreateContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.CreatePathContent translate(net.es.oscars.api.soap.gen.v05.CreatePathContent createPath05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.CreatePathContent createPathContent06 = new net.es.oscars.api.soap.gen.v06.CreatePathContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {   // These elements are required
            createPathContent06.setGlobalReservationId(createPath05.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 CreatePathContent");
        }

        // These elements may be null
        if (createPath05.getToken() != null) {
            createPathContent06.setToken(createPath05.getToken());
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createPathContent06.setMessageProperties(msgProps);
        return createPathContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.CreatePathContent translate(net.es.oscars.api.soap.gen.v06.CreatePathContent createPath06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.CreatePathContent createPathContent05 = new net.es.oscars.api.soap.gen.v05.CreatePathContent();

        try {      // These elements are required
            createPathContent05.setGlobalReservationId(createPath06.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 CreatePathContent");
        }

        // These elements may be null
        if (createPath06.getToken() != null) {
            createPathContent05.setToken(createPath06.getToken());
        }
        return createPathContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.CreatePathResponseContent translate(net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPathReply05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.CreatePathResponseContent createPathResponseClient06 = new net.es.oscars.api.soap.gen.v06.CreatePathResponseContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {    // These elements are required
            createPathResponseClient06.setGlobalReservationId(createPathReply05.getGlobalReservationId());
            createPathResponseClient06.setStatus(createPathReply05.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 CreatePathReply");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createPathResponseClient06.setMessageProperties(msgProps);
        return createPathResponseClient06;
    }

    public static net.es.oscars.api.soap.gen.v05.CreatePathResponseContent translate(net.es.oscars.api.soap.gen.v06.CreatePathResponseContent createPathReply06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPathResponseContent05 = new net.es.oscars.api.soap.gen.v05.CreatePathResponseContent();

        try {    // These elements are required
            createPathResponseContent05.setGlobalReservationId(createPathReply06.getGlobalReservationId());
            createPathResponseContent05.setStatus(createPathReply06.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 CreatePathResponseContent");
        }

        return createPathResponseContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.TeardownPathContent translate(net.es.oscars.api.soap.gen.v05.TeardownPathContent teardownPath05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.TeardownPathContent teardownPathContent06 = new net.es.oscars.api.soap.gen.v06.TeardownPathContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {   // These elements are required
            teardownPathContent06.setGlobalReservationId(teardownPath05.getGlobalReservationId());
            // TODO: status?
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 TeardownPathContent");
        }

        // These elements may be null
        if (teardownPath05.getToken() != null)  {
            teardownPathContent06.setToken(teardownPath05.getToken());
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        teardownPathContent06.setMessageProperties(msgProps);
        return teardownPathContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.TeardownPathContent translate(net.es.oscars.api.soap.gen.v06.TeardownPathContent teardownPath06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.TeardownPathContent teardownPathContent05 = new net.es.oscars.api.soap.gen.v05.TeardownPathContent();

        try {   // These elements are required
            teardownPathContent05.setGlobalReservationId(teardownPath06.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 TeardownPathContent");
        }

        // These elements may be null
        if (teardownPath06.getToken() != null)  {
            teardownPathContent05.setToken(teardownPath06.getToken());
        }

        return teardownPathContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent translate(net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPathReply05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent teardownPathResponseContent06 = new net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {   // These elements are required
            teardownPathResponseContent06.setGlobalReservationId(teardownPathReply05.getGlobalReservationId());
            teardownPathResponseContent06.setStatus(teardownPathReply05.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 TeardownPathResponseContent");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        teardownPathResponseContent06.setMessageProperties(msgProps);
        return teardownPathResponseContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent translate(net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent teardownPathReply06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPathResponseContent05 = new net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent();

        try {   // These elements are required
            teardownPathResponseContent05.setGlobalReservationId(teardownPathReply06.getGlobalReservationId());
            teardownPathResponseContent05.setStatus(teardownPathReply06.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 TeardownPathResponseContent");
        }
        return teardownPathResponseContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.CancelResContent translate(net.es.oscars.api.soap.gen.v05.GlobalReservationId cancelReservation05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.CancelResContent cancelResContent06 = new net.es.oscars.api.soap.gen.v06.CancelResContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {   // These elements are required
            cancelResContent06.setGlobalReservationId(cancelReservation05.getGri());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 GlobalReservationId");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        cancelResContent06.setMessageProperties(msgProps);
        return cancelResContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.GlobalReservationId translate(net.es.oscars.api.soap.gen.v06.CancelResContent cancelResContent06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.GlobalReservationId globalReservationId05 = new net.es.oscars.api.soap.gen.v05.GlobalReservationId();

        try {  // These elements are required
            globalReservationId05.setGri(cancelResContent06.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 CancelResContent");
        }

        return globalReservationId05;
    }

    public static net.es.oscars.api.soap.gen.v06.CancelResReply translate(String cancelReservationReply05) {
        net.es.oscars.api.soap.gen.v06.CancelResReply cancelResReply06 = new net.es.oscars.api.soap.gen.v06.CancelResReply();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        cancelResReply06.setStatus(cancelReservationReply05);

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        cancelResReply06.setMessageProperties(msgProps);
        return cancelResReply06;
    }

    public static String translate(net.es.oscars.api.soap.gen.v06.CancelResReply cancelReservationReply06)
            throws OSCARSServiceException {

        String cancelResReply = cancelReservationReply06.getMessageProperties().getGlobalTransactionId();
        if (cancelResReply == null)  {
            throw new OSCARSServiceException("Unable to translate v06.CancelResReply");
        }
        return cancelResReply;
    }

    public static net.es.oscars.api.soap.gen.v06.ResCreateContent translate(net.es.oscars.api.soap.gen.v05.ResCreateContent createReservation05)
            throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v06.ResCreateContent resCreateContent06 = new net.es.oscars.api.soap.gen.v06.ResCreateContent();
        net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraint06 = new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userRequestConstraint06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {  // These elements are required
            resCreateContent06.setDescription(createReservation05.getDescription());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ResCreateContent");
        }

        // These elements may be null
        if (createReservation05.getGlobalReservationId() != null) {
            resCreateContent06.setGlobalReservationId(createReservation05.getGlobalReservationId());
        }

        try {   // These elements are required
            userRequestConstraint06.setPathInfo(translate(createReservation05.getPathInfo()));
            userRequestConstraint06.setStartTime(createReservation05.getStartTime());
            userRequestConstraint06.setEndTime(createReservation05.getEndTime());
            userRequestConstraint06.setBandwidth(createReservation05.getBandwidth());
            resCreateContent06.setUserRequestConstraint(userRequestConstraint06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ResCreateContent");
        }

        // These elements may be null
        if (createReservation05.getPathInfo() != null) {
            reservedConstraint06.setPathInfo(translate(createReservation05.getPathInfo()));
        }

        // These elements may be undefined
        reservedConstraint06.setStartTime(createReservation05.getStartTime());
        reservedConstraint06.setEndTime(createReservation05.getEndTime());
        reservedConstraint06.setBandwidth(createReservation05.getBandwidth());
        resCreateContent06.setReservedConstraint(reservedConstraint06);

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        resCreateContent06.setMessageProperties(msgProps);
        return resCreateContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.ResCreateContent translate(ResCreateContent modifyReservation06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.ResCreateContent resCreateContent05 = new net.es.oscars.api.soap.gen.v05.ResCreateContent();

        try {    // These elements are required
            resCreateContent05.setDescription(modifyReservation06.getDescription());
            resCreateContent05.setStartTime(modifyReservation06.getReservedConstraint().getStartTime());
            resCreateContent05.setEndTime(modifyReservation06.getReservedConstraint().getEndTime());
            resCreateContent05.setBandwidth(modifyReservation06.getReservedConstraint().getBandwidth());
            resCreateContent05.setPathInfo(translate(modifyReservation06.getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 ResCreateContent");
        }

        // These elements may be null
        if (modifyReservation06.getGlobalReservationId() != null) {
            resCreateContent05.setGlobalReservationId(modifyReservation06.getGlobalReservationId());
        }
        return resCreateContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.ModifyResContent translate(net.es.oscars.api.soap.gen.v05.ModifyResContent modifyReservation05, String src)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.ModifyResContent modifyReservation06 = new net.es.oscars.api.soap.gen.v06.ModifyResContent();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userConstraints06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
       net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06 = new net.es.oscars.api.soap.gen.v06.PathInfo();

        try {    // These elements are required
            modifyReservation06.setGlobalReservationId(modifyReservation05.getGlobalReservationId());
            modifyReservation06.setDescription(modifyReservation05.getDescription());

            userConstraints06.setBandwidth(modifyReservation05.getBandwidth());
            userConstraints06.setStartTime(modifyReservation05.getStartTime());
            userConstraints06.setEndTime(modifyReservation05.getEndTime());
            pathInfo06 = translate(modifyReservation05.getPathInfo());
            userConstraints06.setPathInfo(pathInfo06);
            modifyReservation06.setUserRequestConstraint(userConstraints06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ModifyResConstraint");
        }

        if (src != null) {
            // This is a ModifyResContent created by a 0.5 IDC (Forward message). Needs to fill up reservedConstraints
            net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraints06 =
                    new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();

            reservedConstraints06.setBandwidth(modifyReservation05.getBandwidth());
            reservedConstraints06.setStartTime(modifyReservation05.getStartTime());
            reservedConstraints06.setEndTime(modifyReservation05.getEndTime());
            reservedConstraints06.setPathInfo(pathInfo06);
            modifyReservation06.setReservedConstraint(reservedConstraints06);
        }

        MessagePropertiesType msgProps = new MessagePropertiesType();
        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        modifyReservation06.setMessageProperties(msgProps);
        return modifyReservation06;
    }

    public static net.es.oscars.api.soap.gen.v05.ModifyResContent translate(net.es.oscars.api.soap.gen.v06.ModifyResContent modifyResContent06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.ModifyResContent modifyResContent05 = new net.es.oscars.api.soap.gen.v05.ModifyResContent();

        try {    // These elements are required
            modifyResContent05.setGlobalReservationId(modifyResContent06.getGlobalReservationId());
            modifyResContent05.setDescription(modifyResContent06.getDescription());
            modifyResContent05.setStartTime(modifyResContent06.getReservedConstraint().getStartTime());
            modifyResContent05.setEndTime(modifyResContent06.getReservedConstraint().getEndTime());
            modifyResContent05.setBandwidth(modifyResContent06.getReservedConstraint().getBandwidth());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 ModifyResContent");
        }

        // These elements may be null
        if (modifyResContent06.getReservedConstraint().getPathInfo() != null) {
            modifyResContent05.setPathInfo(translate(modifyResContent06.getReservedConstraint().getPathInfo()));
        }
        return modifyResContent05;
    }

    public static net.es.oscars.api.soap.gen.v05.ModifyResReply translate(net.es.oscars.api.soap.gen.v06.ModifyResReply modifyResReply06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.ModifyResReply modifyResReply05 = new net.es.oscars.api.soap.gen.v05.ModifyResReply();
        ResDetails resDetails = new ResDetails();

        try {   // These elements are required  u 05 but no longer returned by 06
            /* resDetails.setBandwidth(modifyResReply06.getReservation().getReservedConstraint().getBandwidth());
            resDetails.setCreateTime(modifyResReply06.getReservation().getCreateTime());
            resDetails.setDescription(modifyResReply06.getReservation().getDescription());
            resDetails.setLogin(modifyResReply06.getReservation().getLogin());
            resDetails.setStartTime(modifyResReply06.getReservation().getReservedConstraint().getStartTime());
            resDetails.setEndTime(modifyResReply06.getReservation().getReservedConstraint().getEndTime());
            resDetails.setStatus(modifyResReply06.getReservation().getStatus());
            resDetails.setPathInfo(translate(modifyResReply06.getReservation().getReservedConstraint().getPathInfo()));
             */
            resDetails.setStatus(modifyResReply06.getStatus());
            resDetails.setGlobalReservationId(modifyResReply06.getGlobalReservationId());
            modifyResReply05.setReservation(resDetails);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 ModifyResReply");
        }
        return modifyResReply05;
    }

    public static net.es.oscars.api.soap.gen.v06.ModifyResReply translate(net.es.oscars.api.soap.gen.v05.ModifyResReply modifyResReply05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.ModifyResReply modifyResReply06 = new net.es.oscars.api.soap.gen.v06.ModifyResReply();
        //net.es.oscars.api.soap.gen.v06.ResDetails resDetails06 = new net.es.oscars.api.soap.gen.v06.ResDetails();
        //net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraint06 = new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();
        //net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userRequestConstraint06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        SubjectAttributes originator = new SubjectAttributes();
        AttributeType attr = new AttributeType();

        try {    // These elements are required
            modifyResReply06.setGlobalReservationId(modifyResReply05.getReservation().getGlobalReservationId());
            modifyResReply06.setStatus(modifyResReply05.getReservation().getStatus());

            /*  no longer needed
            resDetails06.setDescription(modifyResReply05.getReservation().getDescription());
            resDetails.setCreateTime(modifyResReply05.getReservation().getCreateTime());
            resDetails.setLogin(modifyResReply05.getReservation().getLogin());

            userRequestConstraint06.setBandwidth(modifyResReply05.getReservation().getBandwidth());
            userRequestConstraint06.setStartTime(modifyResReply05.getReservation().getStartTime());
            userRequestConstraint06.setEndTime(modifyResReply05.getReservation().getEndTime());
            userRequestConstraint06.setPathInfo(translate(modifyResReply05.getReservation().getPathInfo()));
            resDetails06.setUserRequestConstraint(userRequestConstraint06);
            */
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ModifyResReply");
        }

        /* These elements no longer used
        if (modifyResReply05.getReservation().getPathInfo() != null) {
            reservedConstraint06.setPathInfo(translate(modifyResReply05.getReservation().getPathInfo()));
        }

        // These elements may be undefined
        reservedConstraint06.setBandwidth(modifyResReply05.getReservation().getBandwidth());
        reservedConstraint06.setStartTime(modifyResReply05.getReservation().getStartTime());
        reservedConstraint06.setEndTime(modifyResReply05.getReservation().getEndTime());

        resDetails06.setReservedConstraint(reservedConstraint06);

        modifyResReply06.setReservation(resDetails06);
        */
        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);

        String loginName = modifyResReply05.getReservation().getLogin();
        attr.setName(AuthZConstants.LOGIN_ID);
        attr.getAttributeValue().add(loginName);
        originator.getSubjectAttribute().add(attr);
        msgProps.setOriginator(originator);
        modifyResReply06.setMessageProperties(msgProps);
        return modifyResReply06;
    }

    public static net.es.oscars.api.soap.gen.v05.PathInfo translate(net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.PathInfo pathInfo05 = new net.es.oscars.api.soap.gen.v05.PathInfo();
        CtrlPlanePathContent ctrlPlanePathContent = new CtrlPlanePathContent();
        
        //pathSteupMode required
        if(pathInfo06.getPathSetupMode() != null){
            pathInfo05.setPathSetupMode(pathInfo06.getPathSetupMode());
        } else {
            throw new OSCARSServiceException("Unable to translate v06 PathInfo:" +
                " pathSetupMode is required by 0.5 but is null");
        }

        // These elements may be null
        if (pathInfo06.getPathType() != null) {
            pathInfo05.setPathType(pathInfo06.getPathType());
        }
        if (pathInfo06.getLayer2Info() != null) {
            pathInfo05.setLayer2Info(translate(pathInfo06.getLayer2Info()));
        }
        if (pathInfo06.getLayer3Info() != null) {
            pathInfo05.setLayer3Info(translate(pathInfo06.getLayer3Info()));
        }
        if (pathInfo06.getMplsInfo() != null) {
            pathInfo05.setMplsInfo(translate(pathInfo06.getMplsInfo()));
        }
        
        //we must have a path element to forward
        if(pathInfo06.getPath() == null){
            throw new OSCARSServiceException("Unable to translate v06 PathInfo. " +
                "There must be a path element.");
        }
        //The hop list must have at least two hops.
        //JAXB creates an empty list so no need for a null check.
        if(pathInfo06.getPath().getHop().size() < 2){
            throw new OSCARSServiceException("Unable to translate v06 PathInfo. " +
                "The path must contain at least two hops.");
        }
        //path is not null and we have hops so populate the list
        for(CtrlPlaneHopContent hop06 : pathInfo06.getPath().getHop()){
            ctrlPlanePathContent.getHop().add(hop06);
        }
        //id is required but has no semantic meaning so just generate default if null
        if (pathInfo06.getPath().getId() != null) {
            ctrlPlanePathContent.setId(pathInfo06.getPath().getId());
        }else{
            ctrlPlanePathContent.setId("path06to05");
        }
        
        // These elements may be null
        if (pathInfo06.getPath().getDirection() != null) {
            ctrlPlanePathContent.setDirection(pathInfo06.getPath().getDirection());
        }
        if (pathInfo06.getPath().getLifetime() != null) {
            ctrlPlanePathContent.setLifetime(pathInfo06.getPath().getLifetime());
        }

        pathInfo05.setPath(ctrlPlanePathContent);
        return pathInfo05;
    }

    public static net.es.oscars.api.soap.gen.v06.PathInfo translate(net.es.oscars.api.soap.gen.v05.PathInfo pathInfo05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06 = new net.es.oscars.api.soap.gen.v06.PathInfo();

        try {    // These elements are required
            pathInfo06.setPathSetupMode(pathInfo05.getPathSetupMode());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 PathInfo");
        }

        // These elements may be null
        if (pathInfo05.getPath() != null) {
            pathInfo06.setPath(pathInfo05.getPath());
        }
        if (pathInfo05.getPathType() != null) {
            pathInfo06.setPathType(pathInfo05.getPathType());
        }
        if (pathInfo05.getLayer2Info() != null) {
            pathInfo06.setLayer2Info(translate(pathInfo05.getLayer2Info()));
        }
        if (pathInfo05.getLayer3Info() != null) {
            pathInfo06.setLayer3Info(translate(pathInfo05.getLayer3Info()));
        }
        if (pathInfo05.getMplsInfo() != null) {
            pathInfo06.setMplsInfo(translate(pathInfo05.getMplsInfo()));
        }

        return pathInfo06;
    }

    public static net.es.oscars.api.soap.gen.v06.Layer2Info translate(net.es.oscars.api.soap.gen.v05.Layer2Info layer2Info05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.Layer2Info layer2Info06 = new net.es.oscars.api.soap.gen.v06.Layer2Info();

        try {   // These elements are required
            layer2Info06.setDestEndpoint(layer2Info05.getDestEndpoint());
            layer2Info06.setSrcEndpoint(layer2Info05.getSrcEndpoint());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 Layer2Info");
        }

        // These elements may be null
        if (layer2Info05.getSrcVtag() != null) {
            layer2Info06.setSrcVtag(translate(layer2Info05.getSrcVtag()));
        }
        if (layer2Info05.getDestVtag() != null) {
            layer2Info06.setDestVtag(translate(layer2Info05.getDestVtag()));
        }
        return layer2Info06;
    }

    public static net.es.oscars.api.soap.gen.v05.Layer2Info translate(net.es.oscars.api.soap.gen.v06.Layer2Info layer2Info06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.Layer2Info layer2Info05 = new net.es.oscars.api.soap.gen.v05.Layer2Info();

        try {   // These elements are required
            layer2Info05.setDestEndpoint(layer2Info06.getDestEndpoint());
            layer2Info05.setSrcEndpoint(layer2Info06.getSrcEndpoint());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 Layer2Info");
        }

        // These elements may be null
        if (layer2Info06.getSrcVtag() != null) {
            layer2Info05.setSrcVtag(translate(layer2Info06.getSrcVtag()));
        }
        if (layer2Info06.getSrcVtag() != null) {
            layer2Info05.setDestVtag(translate(layer2Info06.getSrcVtag()));
        }
        return layer2Info05;
    }

    public static net.es.oscars.api.soap.gen.v05.VlanTag translate(net.es.oscars.api.soap.gen.v06.VlanTag vlanTag06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.VlanTag vlanTag05 = new net.es.oscars.api.soap.gen.v05.VlanTag();

	if (vlanTag06.isTagged()) {
            vlanTag05.setTagged(vlanTag06.isTagged());
            vlanTag05.setValue(vlanTag06.getValue());
        }  else {
		throw new OSCARSServiceException("Unable to translate v06 VlanTag");
	}
        return vlanTag05;
    }

    public static net.es.oscars.api.soap.gen.v06.VlanTag translate(net.es.oscars.api.soap.gen.v05.VlanTag vlanTag05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.VlanTag vlanTag06 = new net.es.oscars.api.soap.gen.v06.VlanTag();

	if (vlanTag05.isTagged()) {
            vlanTag06.setTagged(vlanTag05.isTagged());
            vlanTag06.setValue(vlanTag05.getValue());
        } else {
            throw new OSCARSServiceException("Unable to translate v05 VlanTag");
        }
        return vlanTag06;
    }

    public static net.es.oscars.api.soap.gen.v06.Layer3Info translate(net.es.oscars.api.soap.gen.v05.Layer3Info layer3Info05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.Layer3Info layer3Info06 = new net.es.oscars.api.soap.gen.v06.Layer3Info();

        try {   // These elements are required
            layer3Info06.setSrcHost(layer3Info05.getSrcHost());
            layer3Info06.setDestHost(layer3Info05.getDestHost());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 Layer3Info");
        }

        // These elements may be null
        if (layer3Info05.getDscp() != null) {
            layer3Info06.setDscp(layer3Info05.getDscp());
        }
        if (layer3Info05.getProtocol() != null) {
            layer3Info06.setProtocol(layer3Info05.getProtocol());
        }
        if (layer3Info05.getSrcIpPort() != null) {
            layer3Info06.setSrcIpPort(layer3Info05.getSrcIpPort());
        }
        if (layer3Info05.getDestIpPort() != null) {
            layer3Info06.setDestIpPort(layer3Info05.getDestIpPort());
        }
        return layer3Info06;
    }

    public static net.es.oscars.api.soap.gen.v05.Layer3Info translate(net.es.oscars.api.soap.gen.v06.Layer3Info layer3Info06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.Layer3Info layer3Info05 = new net.es.oscars.api.soap.gen.v05.Layer3Info();

        try {  // These elements are required
            layer3Info05.setSrcHost(layer3Info06.getSrcHost());
            layer3Info05.setDestHost(layer3Info06.getDestHost());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 Layer3Info");
        }

        // These elements may be null
        if (layer3Info06.getDestIpPort() != null) {
            layer3Info05.setDestIpPort(layer3Info06.getDestIpPort());
        }
        if (layer3Info06.getDscp() != null) {
            layer3Info05.setDscp(layer3Info06.getDscp());
        }
        if (layer3Info06.getProtocol() != null) {
            layer3Info05.setProtocol(layer3Info06.getProtocol());
        }
        if (layer3Info06.getSrcIpPort() != null) {
            layer3Info05.setSrcIpPort(layer3Info06.getSrcIpPort());
        }

        return layer3Info05;
    }

    public static net.es.oscars.api.soap.gen.v06.MplsInfo translate(net.es.oscars.api.soap.gen.v05.MplsInfo mplsInfo05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.MplsInfo mplsInfo06 = new net.es.oscars.api.soap.gen.v06.MplsInfo();

        try {   // These elements are required
            mplsInfo06.setBurstLimit(mplsInfo05.getBurstLimit());
            mplsInfo06.setLspClass(mplsInfo05.getLspClass());
        } catch (Exception e) {
            throw new  OSCARSServiceException("Unable to translate v05 MplsInfo05");
        }

        return mplsInfo06;
    }

    public static net.es.oscars.api.soap.gen.v05.MplsInfo translate(net.es.oscars.api.soap.gen.v06.MplsInfo mplsInfo06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.MplsInfo mplsInfo05 = new net.es.oscars.api.soap.gen.v05.MplsInfo();

        try {  // These elements are required
            mplsInfo05.setBurstLimit(mplsInfo06.getBurstLimit());
            mplsInfo05.setLspClass(mplsInfo06.getLspClass());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 MplsInfo06");
        }

        return mplsInfo05;
    }

    // ToDo: check this
    public static net.es.oscars.api.soap.gen.v06.InterDomainEventContent translate(org.oasis_open.docs.wsn.b_2.Notify notify05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.InterDomainEventContent interDomainEventContent = new net.es.oscars.api.soap.gen.v06.InterDomainEventContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        
        
        if(notify05 == null || notify05.getNotificationMessage() == null || 
                notify05.getNotificationMessage().size() == 0){
            throw new OSCARSServiceException("Unable to translate v05 Notify: " +
                    "The message did not contain a NotificationMessage");
        }
        NotificationMessageHolderType notifyMsg05 = notify05.getNotificationMessage().get(0);
        if(notifyMsg05.getMessage() == null){
            throw new OSCARSServiceException("Unable to translate v05 Notify: " +
            "The message did not contain a NotificationMessage/Message element");
        }
        if(notifyMsg05.getMessage().getAny() == null || notifyMsg05.getMessage().getAny().isEmpty()){
            throw new OSCARSServiceException("Unable to translate v05 Notify: " +
            "The message contained an empty NotificationMessage/Message element");
        }
        
        net.es.oscars.api.soap.gen.v05.EventContent idcEvent05 = null;
        for(Object notifyMsgObj : notifyMsg05.getMessage().getAny()){         
            //convert string to IDC Event
            try {
                JAXBContext context = JAXBContext.newInstance(net.es.oscars.api.soap.gen.v05.EventContent.class);
                idcEvent05 = (net.es.oscars.api.soap.gen.v05.EventContent) ((JAXBElement)notifyMsgObj).getValue();
            } catch (Exception e) {
                continue;
            }
            
            break;
        }
        
        if(idcEvent05 == null){
            throw new OSCARSServiceException("Unable to translate v05 Notify: " +
                "Unable to find idc:event in message");
        }
        
        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        interDomainEventContent.setMessageProperties(msgProps);
        if(idcEvent05.getResDetails() != null){
            interDomainEventContent.setResDetails(DataTranslator05.translate(idcEvent05.getResDetails()));
        }
        interDomainEventContent.setErrorCode(idcEvent05.getErrorCode());
        interDomainEventContent.setErrorMessage(idcEvent05.getErrorMessage());
        interDomainEventContent.setErrorSource(idcEvent05.getErrorSource());
        
        //Translate even types that have changed between 0.5 and 0.6
        if(idcEvent05.getType() == null){
            throw new OSCARSServiceException("Unable to translate v05 Notify: " +
            "Unable to find idc:event/type in message");
        }else if(NotifyRequestTypes.RESV_CREATE_CONFIRMED.equals(idcEvent05.getType())){
            interDomainEventContent.setType(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED);
        }else if(NotifyRequestTypes.RESV_MODIFY_CONFIRMED.equals(idcEvent05.getType())){
            interDomainEventContent.setType(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED);
        }else{
            interDomainEventContent.setType(idcEvent05.getType());
        }
        return interDomainEventContent;
    }

    public static net.es.oscars.api.soap.gen.v06.ResDetails translate(
            net.es.oscars.api.soap.gen.v05.ResDetails resDetails05) throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.ResDetails resDetails06 = new net.es.oscars.api.soap.gen.v06.ResDetails();
        
        if(resDetails05.getGlobalReservationId() == null){
            throw new OSCARSServiceException("Unable to translate v05 resDetails: " +
                "globalreservationId cannot be null");
        }
        resDetails06.setGlobalReservationId(resDetails05.getGlobalReservationId());
        
        if(resDetails05.getLogin() == null){
            throw new OSCARSServiceException("Unable to translate v05 resDetails: " +
                "login cannot be null");
        }
        resDetails06.setLogin(resDetails05.getLogin());
        
        if(resDetails05.getDescription() == null){
            throw new OSCARSServiceException("Unable to translate v05 resDetails: " +
                "description cannot be null");
        }
        resDetails06.setDescription(resDetails05.getDescription());
        
        if(resDetails05.getStatus() == null){
            throw new OSCARSServiceException("Unable to translate v05 resDetails: " +
                "status cannot be null");
        }
        resDetails06.setStatus(resDetails05.getStatus());
        
        if(resDetails05.getPathInfo() == null){
            throw new OSCARSServiceException("Unable to translate v05 resDetails: " +
            "pathInfo cannot be null");
        }
        
        //create time is set to 0 during failure so accept anything
        resDetails06.setCreateTime(resDetails05.getCreateTime());
        
        UserRequestConstraintType userConstraint = new UserRequestConstraintType();
        userConstraint.setStartTime(resDetails05.getStartTime());
        userConstraint.setEndTime(resDetails05.getEndTime());
        userConstraint.setBandwidth(resDetails05.getBandwidth());
        userConstraint.setPathInfo(DataTranslator05.translate(resDetails05.getPathInfo()));
        resDetails06.setUserRequestConstraint(userConstraint);
        
        ReservedConstraintType resvConstraint = new ReservedConstraintType();
        resvConstraint.setStartTime(resDetails05.getStartTime());
        resvConstraint.setEndTime(resDetails05.getEndTime());
        resvConstraint.setBandwidth(resDetails05.getBandwidth());
        resvConstraint.setPathInfo(DataTranslator05.translate(resDetails05.getPathInfo()));
        resDetails06.setReservedConstraint(resvConstraint);
        
        return resDetails06;
    }
    
    // TODO: check this
    public static org.oasis_open.docs.wsn.b_2.Notify translate(net.es.oscars.api.soap.gen.v06.InterDomainEventContent eventContent06)
            throws OSCARSServiceException {
        org.oasis_open.docs.wsn.b_2.Notify notify = new org.oasis_open.docs.wsn.b_2.Notify();
        NotificationMessageHolderType notificationMessageHolder = new NotificationMessageHolderType();
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        MessageType messageType = new MessageType();
        net.es.oscars.api.soap.gen.v05.ResDetails resDetails = new net.es.oscars.api.soap.gen.v05.ResDetails();
        net.es.oscars.api.soap.gen.v05.EventContent eventContent = new net.es.oscars.api.soap.gen.v05.EventContent();

        topicExpressionType.setValue("idc:IDC");
        topicExpressionType.setDialect("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full");
        notificationMessageHolder.setTopic(topicExpressionType);

        try {
            resDetails.setPathInfo(translate(eventContent06.getResDetails().getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 InterDomainEventContent");
        }

        if (eventContent06.getResDetails().getDescription() != null) {
            resDetails.setDescription(eventContent06.getResDetails().getDescription());
        }

        resDetails.setStartTime(eventContent06.getResDetails().getReservedConstraint().getStartTime());
        resDetails.setEndTime(eventContent06.getResDetails().getReservedConstraint().getEndTime());

        if (eventContent06.getResDetails().getLogin() != null) {
            resDetails.setLogin(eventContent06.getResDetails().getLogin());
        }
        if (eventContent06.getResDetails().getStatus() != null) {
            resDetails.setStatus(eventContent06.getResDetails().getStatus());
        }

        resDetails.setBandwidth(eventContent06.getResDetails().getReservedConstraint().getBandwidth());

        if (eventContent06.getResDetails().getGlobalReservationId() != null) {
            resDetails.setGlobalReservationId(eventContent06.getResDetails().getGlobalReservationId());
        }

        eventContent.setResDetails(resDetails);

        if (eventContent06.getErrorSource() != null) {
            eventContent.setErrorSource(eventContent06.getErrorSource());
        }
        if (eventContent06.getErrorCode() != null) {
            eventContent.setErrorCode(eventContent06.getErrorCode());
        }
        if (eventContent06.getErrorMessage() != null) {
            eventContent.setErrorMessage(eventContent06.getErrorMessage());
        }
        if (eventContent06.getType() != null) {
            eventContent.setType(eventContent06.getType());
        }

        messageType.getAny().set(0, eventContent);
        notificationMessageHolder.setMessage(messageType);
        notify.getNotificationMessage().set(0, notificationMessageHolder);


        return notify;
    }
}
