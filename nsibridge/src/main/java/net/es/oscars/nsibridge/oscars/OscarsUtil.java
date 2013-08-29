package net.es.oscars.nsibridge.oscars;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.TranslationException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.utils.soap.OSCARSServiceException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OscarsUtil {
    private static final Logger log = Logger.getLogger(OscarsUtil.class);

    public static void submitResv(ResvRequest resvRequest) throws TranslationException, ServiceException  {
        log.debug("submitResv start");
        String connId = resvRequest.getReserveType().getConnectionId();
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);

        ResCreateContent rc = null;
        try {
            rc = NSI_OSCARS_Translation.makeOscarsResv(resvRequest);
            log.debug("translated NSI to OSCARS");
        } catch (TranslationException ex) {
            log.debug(ex);
            log.debug("could not translate NSI request");
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw ex;
        }

        if (rc == null) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw new TranslationException("null result in translation");
        }

        try {
            CreateReply reply = OscarsProxy.getInstance().sendCreate(rc);
            log.debug("connId: "+connId+" gri: "+reply.getGlobalReservationId());
            addOscarsRecord(cr, reply.getGlobalReservationId(), new Date(), reply.getStatus());
        } catch (OSCARSServiceException e) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            log.debug(e);
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
        if (cp != null) {
            try {
                CreatePathResponseContent reply = OscarsProxy.getInstance().sendSetup(cp);
            } catch (OSCARSServiceException e) {
                addOscarsRecord(cr, null, new Date(), "FAILED");
                log.error(e);
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
            log.debug(ex);
            log.debug("could not translate NSI request");

        }
        if (cp != null) {
            try {
                TeardownPathResponseContent reply = OscarsProxy.getInstance().sendTeardown(cp);
            } catch (OSCARSServiceException e) {
                addOscarsRecord(cr, null, new Date(), "FAILED");
                log.error(e);
                throw new ServiceException("could not submit setup");
            }
        }
        log.debug("submitTeardown complete");
    }




    public static void submitCancel(ConnectionRecord cr) throws ServiceException  {
        log.debug("submitCancel start");
        String oscarsGri = cr.getOscarsGri();


        CancelResContent rc = NSI_OSCARS_Translation.makeOscarsCancel(oscarsGri);
        try {
            CancelResReply reply = OscarsProxy.getInstance().sendCancel(rc);
        } catch (OSCARSServiceException e) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            log.error(e);
            throw new ServiceException("could not submit cancel");
        }
        log.debug("submitCancel complete");
    }

    public static void submitQuery(ConnectionRecord cr) throws TranslationException {
        String oscarsGri = cr.getOscarsGri();
        if (oscarsGri == null || oscarsGri.equals("")) {
            throw new TranslationException("could not find OSCARS GRI for connId: "+cr.getConnectionId());
        }
        QueryResContent qc = NSI_OSCARS_Translation.makeOscarsQuery(oscarsGri);

        if (qc != null) {
            try {
                QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc);
                String oscStatus =  reply.getReservationDetails().getStatus();

                log.debug("query result for connId: "+cr.getConnectionId()+" gri: "+oscarsGri+" status: "+oscStatus);
                addOscarsRecord(cr, oscarsGri, new Date(), oscStatus);


            } catch (OSCARSServiceException e) {
                e.printStackTrace();
            }
        } else {
            throw new TranslationException("could not translate to OSCARS query");
        }
    }

    public static void submitModify(ResvRequest resvRequest) throws TranslationException, ServiceException  {
        log.debug("submitModify start");
        String connId = resvRequest.getReserveType().getConnectionId();
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);

        ModifyResContent mc = null;
        try {
            mc = NSI_OSCARS_Translation.makeOscarsModify(resvRequest);
            log.debug("translated NSI to OSCARS");
        } catch (TranslationException ex) {
            log.debug(ex);
            log.debug("could not translate NSI request");
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw ex;
        }

        if (mc == null) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw new TranslationException("null result in translation");
        }

        try {
            ModifyResReply reply = OscarsProxy.getInstance().sendModify(mc);
            log.debug("connId: "+connId+" gri: "+reply.getGlobalReservationId());
            addOscarsRecord(cr, reply.getGlobalReservationId(), new Date(), reply.getStatus());
        } catch (OSCARSServiceException e) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            log.debug(e);
            throw new ServiceException("Failed to modify reservation");
        }
    }

    public static void addOscarsRecord(ConnectionRecord cr, String gri, Date date, String status) {
        String connId = cr.getConnectionId();
        log.debug("addOscarsRecord connId: "+connId+" gri: "+gri+" status: "+status);
        EntityManager em = PersistenceHolder.getEntityManager();

        em.getTransaction().begin();
        OscarsStatusRecord or = new OscarsStatusRecord();
        or.setDate(date);
        cr.getOscarsStatusRecords().add(or);
        cr.setOscarsGri(gri);
        or.setStatus(status);
        em.persist(cr);
        em.getTransaction().commit();
    }





    public static OscarsLogicAction pollUntilOpAllowed(OscarsOps op, ConnectionRecord cr) throws TranslationException {
        HashSet<OscarsOps> ops = new HashSet<OscarsOps>();
        ops.add(op);
        return pollUntilAnOpAllowed(ops, cr);
    }

    public static OscarsLogicAction pollUntilAnOpAllowed(Set<OscarsOps> ops, ConnectionRecord cr) throws TranslationException {
        TimingConfig tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);

        Double pollInterval = tc.getOscarsTimingConfig().getPollInterval() * 1000;
        Double pollTimeout = tc.getOscarsTimingConfig().getPollTimeout() * 1000;

        double elapsed = 0;
        double timeout = pollTimeout;
        OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);

        OscarsLogicAction result = OscarsLogicAction.ASK_LATER;
        HashMap<OscarsOps, OscarsLogicAction> allActions = new HashMap<OscarsOps, OscarsLogicAction>();
        if (or != null) {
            for (OscarsOps op : ops) {
                OscarsLogicAction anAction = OscarsStateLogic.isOperationAllowed(op, OscarsStates.valueOf(or.getStatus()));
                allActions.put(op, anAction);
                log.debug("op: "+op+" action:"+anAction);
            }
        } else {
            for (OscarsOps op : ops) {
                OscarsLogicAction anAction = OscarsStateLogic.isOperationAllowed(op, OscarsStates.UNSUBMITTED);
                allActions.put(op, anAction);
                log.debug("op: "+op+" action:"+anAction);
            }
        }

        // return YES if it is a yes for any op
        boolean foundYes = false;
        boolean foundYesOrAskLater = false;
        for (OscarsOps op : ops) {
            log.debug("op: "+op+" action:"+allActions.get(op));

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
                Thread.sleep(pollInterval.longValue());
                elapsed += pollInterval;
                OscarsUtil.submitQuery(cr);
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

        double elapsed = 0;
        double timeout = pollTimeout;


        OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
        if (or == null) {
            try {
                OscarsUtil.submitQuery(cr);
            } catch (TranslationException ex) {
                log.error(ex);
                throw new ServiceException("could not poll");
            }
        }
        OscarsStates os = OscarsStates.valueOf(or.getStatus());
        boolean stable = false;


        while (!stable && timeout > elapsed) {
            if (OscarsStateLogic.isStateSteady(os)) {
                stable = true;
            } else {
                try {
                    Thread.sleep(pollInterval.longValue());
                    elapsed += pollInterval;
                    OscarsUtil.submitQuery(cr);
                } catch (InterruptedException ex) {
                    throw new ServiceException("interrupted");
                } catch (TranslationException ex) {
                    log.error(ex);
                    throw new ServiceException("could not poll");
                }
            }
        }

        // timed out waiting
        if (elapsed > timeout && !stable) {
            throw new ServiceException("timed out");
        }

        return os;
    }
}
