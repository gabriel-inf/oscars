package net.es.oscars.nsibridge.oscars;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsInfoRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import net.es.oscars.nsibridge.prov.TranslationException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.utils.soap.OSCARSServiceException;
import org.apache.log4j.Logger;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.persistence.EntityManager;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OscarsUtil {
    private static final Logger log = Logger.getLogger(OscarsUtil.class);

    public static void submitResv(ResvRequest resvRequest) throws TranslationException, ServiceException {
        log.debug("submitResv start");
        String connId = resvRequest.getReserveType().getConnectionId();
        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);

        ResCreateContent rc = null;
        try {
            rc = NSI_OSCARS_Translation.makeOscarsResv(resvRequest);
            log.debug("translated NSI to OSCARS");
        } catch (TranslationException ex) {
            log.error(ex.getMessage(), ex);
            log.error("could not translate NSI request");
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw ex;
        }

        if (rc == null) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw new TranslationException("null result in translation");
        }
        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        }

        try {
            CreateReply reply = OscarsProxy.getInstance().sendCreate(rc, attrs);
            log.debug("connId: " + connId + " gri: " + reply.getGlobalReservationId());
            addOscarsRecord(cr, reply.getGlobalReservationId(), new Date(), reply.getStatus());
        } catch (OSCARSServiceException ex) {
            addOscarsRecord(cr, null, new Date(), OscarsStates.FAILED.toString());
            log.error(ex.getMessage(), ex);
            throw new ServiceException("Failed to submit reservation");
        }
    }

    public static void submitSetup(ConnectionRecord cr) throws ServiceException {
        log.debug("submitSetup start");
        String oscarsGri = cr.getOscarsGri();

        CreatePathContent cp = null;
        try {
            cp = NSI_OSCARS_Translation.makeOscarsSetup(oscarsGri);
        } catch (TranslationException ex) {
            log.debug(ex);
            log.debug("could not translate NSI request");

        }

        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        }
        if (cp != null) {
            try {
                CreatePathResponseContent reply = OscarsProxy.getInstance().sendSetup(cp, attrs);
            } catch (OSCARSServiceException ex) {
                addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");
                log.error(ex.getMessage(), ex);
                throw new ServiceException("could not submit setup");
            }
        }
        log.debug("submitSetup complete");
    }

    public static void submitTeardown(ConnectionRecord cr) throws ServiceException {
        log.debug("submitTeardown start");
        String oscarsGri = cr.getOscarsGri();

        TeardownPathContent cp = null;
        try {
            cp = NSI_OSCARS_Translation.makeOscarsTeardown(oscarsGri);
        } catch (TranslationException ex) {
            log.error("could not translate NSI request", ex);

        }
        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        }
        if (cp != null) {
            try {
                TeardownPathResponseContent reply = OscarsProxy.getInstance().sendTeardown(cp, attrs);
            } catch (OSCARSServiceException ex) {
                addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");
                log.error(ex.getMessage(), ex);
                throw new ServiceException("could not submit setup");
            }
        }
        log.debug("submitTeardown complete");
    }


    public static void submitCancel(ConnectionRecord cr) throws ServiceException {
        log.debug("submitCancel start");
        String oscarsGri = cr.getOscarsGri();

        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        }
        CancelResContent rc = NSI_OSCARS_Translation.makeOscarsCancel(oscarsGri);
        try {
            CancelResReply reply = OscarsProxy.getInstance().sendCancel(rc, attrs);
        } catch (OSCARSServiceException ex) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            log.error(ex.getMessage(), ex);
            throw new ServiceException("could not submit cancel");
        }
        log.debug("submitCancel complete");
    }

    public static OscarsStatusRecord submitQuery(ConnectionRecord cr) throws TranslationException {
        String oscarsGri = cr.getOscarsGri();
        if (oscarsGri == null || oscarsGri.equals("")) {
            throw new TranslationException("could not find OSCARS GRI for connId: " + cr.getConnectionId());
        }
        QueryResContent qc = NSI_OSCARS_Translation.makeOscarsQuery(oscarsGri);
        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        }
        if (qc != null) {
            try {
                QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc, attrs);
                String oscStatus = reply.getReservationDetails().getStatus();


                log.debug("query result for connId: " + cr.getConnectionId() + " gri: " + oscarsGri + " status: " + oscStatus);
                OscarsStatusRecord or = addOscarsRecord(cr, oscarsGri, new Date(), oscStatus);

                OscarsInfoRecord ir = NSI_OSCARS_Translation.makeOscarsInfo(reply.getReservationDetails());
                updateOscarsInfoRecord(cr, ir);

                return or;

            } catch (OSCARSServiceException ex) {
                log.error(ex.getMessage(), ex);
                throw new TranslationException(ex.getMessage());
            }
        } else {
            throw new TranslationException("could not translate to OSCARS query");
        }
    }

    public static void submitModify(ResvRequest resvRequest) throws TranslationException, ServiceException {
        log.debug("submitModify start");
        String connId = resvRequest.getReserveType().getConnectionId();
        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        }
        ModifyResContent mc = null;
        try {
            mc = NSI_OSCARS_Translation.makeOscarsModify(resvRequest, cr.getOscarsGri());
            log.debug("translated NSI to OSCARS");
        } catch (TranslationException ex) {
            log.error("could not translate NSI request");

            log.error(ex.getMessage(), ex);
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");
            throw ex;
        }

        if (mc == null) {
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");
            throw new TranslationException("null result in translation");
        }

        try {
            ModifyResReply reply = OscarsProxy.getInstance().sendModify(mc, attrs);
            log.debug("connId: " + connId + " gri: " + reply.getGlobalReservationId());
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), reply.getStatus());
        } catch (OSCARSServiceException ex) {
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");

            log.error(ex.getMessage(), ex);
            throw new ServiceException("Failed to modify reservation");
        }
    }

    public static void submitRollback(ConnectionRecord cr) throws TranslationException, ServiceException {
        log.debug("submitRollback start");
        ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
        SubjectAttributes attrs = null;
        try {
            attrs = DB_Util.getAttributes(cr.getOscarsAttributes());
        } catch (XMLStreamException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);
            throw new TranslationException(ex.getMessage());
        }
        ModifyResContent mc = null;
        try {
            mc = NSI_OSCARS_Translation.makeOscarsRollback(rr, cr.getOscarsGri());
            log.debug("translated NSI to OSCARS");
        } catch (TranslationException ex) {
            log.error("could not translate NSI request");

            log.error(ex.getMessage(), ex);
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");
            throw ex;
        }

        if (mc == null) {
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");
            throw new TranslationException("null result in translation");
        }

        try {
            ModifyResReply reply = OscarsProxy.getInstance().sendModify(mc, attrs);
            log.debug("connId: " + cr.getConnectionId() + " gri: " + reply.getGlobalReservationId());
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), reply.getStatus());
        } catch (OSCARSServiceException ex) {
            addOscarsRecord(cr, cr.getOscarsGri(), new Date(), "FAILED");

            log.error(ex.getMessage(), ex);
            throw new ServiceException("Failed to modify reservation");
        }
    }

    public static void updateOscarsInfoRecord(ConnectionRecord cr, OscarsInfoRecord infoRecord) {
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getEntityManagerFactory().getCache().evictAll();

        cr.setOscarsInfoRecord(infoRecord);
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
    }

    public static OscarsStatusRecord addOscarsRecord(ConnectionRecord cr, String gri, Date date, String status) {
        String connId = cr.getConnectionId();
        log.debug("addOscarsRecord connId: " + connId + " gri: " + gri + " status: " + status + " date: " + date.getTime());
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getEntityManagerFactory().getCache().evictAll();

        OscarsStatusRecord or = new OscarsStatusRecord();
        or.setDate(date);
        or.setStatus(status);
        cr.setOscarsStatusRecord(or);
        cr.setOscarsGri(gri);

        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
        return or;

    }


    public static OscarsLogicAction pollUntilOpAllowed(OscarsOps op, ConnectionRecord cr, UUID taskId) throws TranslationException {
        HashSet<OscarsOps> ops = new HashSet<OscarsOps>();
        ops.add(op);
        return pollUntilAnOpAllowed(ops, cr, taskId);
    }

    public static OscarsLogicAction pollUntilAnOpAllowed(Set<OscarsOps> ops, ConnectionRecord cr, UUID taskId) throws TranslationException {
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);

        Double pollInterval = tc.getOscarsTimingConfig().getPollInterval() * 1000;
        Double pollTimeout = tc.getOscarsTimingConfig().getPollTimeout() * 1000;

        double elapsed = 0;
        double timeout = pollTimeout;
        OscarsStatusRecord or = cr.getOscarsStatusRecord();

        OscarsLogicAction result = OscarsLogicAction.ASK_LATER;
        HashMap<OscarsOps, OscarsLogicAction> allActions = new HashMap<OscarsOps, OscarsLogicAction>();
        if (or != null) {
            for (OscarsOps op : ops) {

                OscarsLogicAction anAction = OscarsStateLogic.isOperationAllowed(op, OscarsStates.valueOf(or.getStatus()));
                allActions.put(op, anAction);
                log.debug("op: " + op + " action:" + anAction);
            }
        } else {
            for (OscarsOps op : ops) {
                OscarsLogicAction anAction = OscarsStateLogic.isOperationAllowed(op, OscarsStates.UNSUBMITTED);
                allActions.put(op, anAction);
                log.debug("op: " + op + " action:" + anAction);
            }
        }

        // return YES if it is a yes for any op
        boolean foundYes = false;
        boolean foundYesOrAskLater = false;
        for (OscarsOps op : ops) {
            log.debug("op: " + op + " action:" + allActions.get(op));

            if (allActions.get(op).equals(OscarsLogicAction.YES)) {
                foundYes = true;
                foundYesOrAskLater = true;
            } else if (allActions.get(op).equals(OscarsLogicAction.ASK_LATER)) {
                foundYesOrAskLater = true;
            }
        }

        if (foundYes) return OscarsLogicAction.YES;
        if (!foundYesOrAskLater) {
            return OscarsLogicAction.NO;
        }

        while (result == OscarsLogicAction.ASK_LATER && elapsed < timeout) {
            try {
                log.debug("task " + taskId + " waiting...");
                Thread.sleep(pollInterval.longValue());
                elapsed += pollInterval;
                OscarsUtil.submitQuery(cr);
                or = cr.getOscarsStatusRecord();

                for (OscarsOps op : ops) {
                    OscarsLogicAction anAction = OscarsStateLogic.isOperationAllowed(op, OscarsStates.valueOf(or.getStatus()));
                    allActions.put(op, anAction);
                }

                foundYes = false;
                foundYesOrAskLater = false;
                for (OscarsOps op : ops) {
                    if (allActions.get(op).equals(OscarsLogicAction.YES)) {
                        result = OscarsLogicAction.YES;
                        foundYesOrAskLater = true;
                        foundYes = true;

                    } else if (allActions.get(op).equals(OscarsLogicAction.ASK_LATER)) {
                        foundYesOrAskLater = true;
                    }
                }

                if (foundYes) {
                    return OscarsLogicAction.YES;
                }
                if (!foundYesOrAskLater) {
                    return OscarsLogicAction.NO;
                }

            } catch (InterruptedException ex) {

                log.error(ex.getMessage(), ex);

                throw new TranslationException("interrupted");
            }
        }

        if (result == OscarsLogicAction.ASK_LATER) {
            result = OscarsLogicAction.TIMED_OUT;
        }
        return result;
    }

    public static OscarsStates pollUntilResvStable(ConnectionRecord cr) throws ServiceException {
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);

        Double pollInterval = tc.getOscarsTimingConfig().getPollInterval() * 1000;
        Double pollTimeout = tc.getOscarsTimingConfig().getPollTimeout() * 1000;
        try {
            log.debug("waiting " + pollInterval + " ms to poll for connId: " + cr.getConnectionId() + " timeout: " + pollTimeout);
            Thread.sleep(pollInterval.longValue());


        } catch (InterruptedException ex) {

            log.error(ex.getMessage(), ex);
            throw new ServiceException("interrupted");
        }

        double elapsed = 0;
        double timeout = pollTimeout;

        boolean stable = false;

        try {
            // query at least once
            OscarsStatusRecord or = OscarsUtil.submitQuery(cr);
            OscarsStates os = OscarsStates.valueOf(or.getStatus());

            while (!stable && timeout > elapsed) {
                stable = OscarsStateLogic.isStateSteady(os);

                if (!stable) {
                    try {
                        Thread.sleep(pollInterval.longValue());
                        elapsed += pollInterval;
                        or = OscarsUtil.submitQuery(cr);
                        os = OscarsStates.valueOf(or.getStatus());
                        log.debug("queried oscars, elapsed ms: " + elapsed + " state: " + os);

                    } catch (InterruptedException ex) {
                        log.error(ex.getMessage(), ex);
                        throw new ServiceException("interrupted");
                    }
                }
            }
            // timed out waiting
            if (elapsed > timeout && !stable) {
                throw new ServiceException("timed out");
            }
            log.debug("stable state: " + os);
            return os;
        } catch (TranslationException ex) {

            log.error(ex.getMessage(), ex);
            throw new ServiceException("could not poll");
        }
    }

    public static String normalizeDN(String dn) throws IllegalArgumentException, InvalidNameException {
        // SEE:

        // https://github.com/hessu/ham-cert-web-demo/issues/1

        // incoming format:
        // /C=US/ST=CA/L=Berkeley/O=ESnet/OU=ANTG/CN=MaintDB
        // desired format:
        // "CN=MaintDB, OU=ANTG, O=ESnet, L=Berkeley, ST=CA, C=US";

        //if DN does not start with / then it should already be in the desired form
        String normalized;

        if (dn.startsWith("/")) {
            LdapName ldn = new LdapName("");
            Rdn rdn;

            String regex = "(.+)?/(\\w+)=(.+)";
            Pattern pattern = Pattern.compile(regex);

            while (dn != null && !dn.equals("")) {
                Matcher matcher = pattern.matcher(dn);
                while (matcher.find()) {
                    String name = matcher.group(2);
                    String value = matcher.group(3);
                    dn = matcher.group(1);
                    if (name != null && value != null) {
                        rdn = new Rdn(name, value);
                        ldn.add(0, rdn);
                    }
                    // for (int i = 0; i <= 3; i++) {
                        // System.out.println("group " + i + ": " + matcher.group(i));
                    // }
                }
            }
            normalized = ldn.toString();
        } else {
            normalized = dn;
        }

        X500Principal principal = new X500Principal(normalized);
        return principal.getName();
    }


}
