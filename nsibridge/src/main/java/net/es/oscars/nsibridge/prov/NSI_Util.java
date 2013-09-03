package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.EthernetVlanType;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NSI_Util {

    private static final Logger log = Logger.getLogger(NSI_Util.class);

    public static void createResvRecord(String connId, ReserveType rt) throws ServiceException {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        ResvRecord latest = ConnectionRecord.getCommittedResvRecord(cr);

        ResvRecord rr = new ResvRecord();
        Integer version = rt.getCriteria().getVersion();
        if (version == null) {
            version = 0;
        }

        if (latest != null) {
            if (latest.getVersion() >= version) {
                throw new ServiceException("requested version: "+version+" <= committed version: "+latest.getVersion());
            }
        }

        ReservationRequestCriteriaType crit = rt.getCriteria();
        EthernetVlanType evts = null;
        for (Object o : crit.getAny()) {
            if (o instanceof EthernetVlanType ) {
                evts = (EthernetVlanType) o;
            } else {
                try {

                    JAXBElement<EthernetVlanType> payload = (JAXBElement<EthernetVlanType>) o;
                    evts = payload.getValue();
                } catch (ClassCastException ex) {
                    log.error(ex);
                    evts = null;
                }
            }
        }

        if (evts == null) {
            throw new ServiceException("no evts element!");
        }

        Long capacity = evts.getCapacity();

        rr.setCapacity(capacity);
        rr.setStartTime(crit.getSchedule().getStartTime().toGregorianCalendar().getTime());
        rr.setEndTime(crit.getSchedule().getEndTime().toGregorianCalendar().getTime());


        rr.setVersion(version);
        rr.setCommitted(false);
        rr.setSubmittedAt(new Date());


        cr.getResvRecords().add(rr);

        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
        log.debug("saved a new resv record for connId: "+cr.getConnectionId()+" v:"+version);

    }


    public static void commitResvRecord(String connId) throws ServiceException {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        List<ResvRecord> uncom = ConnectionRecord.getUncommittedResvRecords(cr);
        if (uncom.size() == 0) {
            throw new ServiceException("zero resvRecords to commit for connId: "+connId);
        } else if (uncom.size() >1) {
            throw new ServiceException("multiple resvRecords to commit for connId: "+connId);
        }

        ResvRecord rr = uncom.get(0);
        rr.setCommitted(true);
        for (ResvRecord rec : cr.getResvRecords()) {
            if (rec.equals(rr)) {
                rec.setCommitted(true);
            }
        }

        Date submittedAt = rr.getSubmittedAt();

        ApplicationContext ax = SpringContext.getInstance().getContext();
        TimingConfig tx = ax.getBean("timingConfig", TimingConfig.class);

        Long resvTimeout = new Double(tx.getResvTimeout()).longValue();
        Date now = new Date();

        if (submittedAt.getTime() + resvTimeout < now.getTime()) {
            throw new ServiceException("commit after timeout");
        }

        rr.setCommittedAt(new Date());


        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        for (ResvRecord rec : cr.getResvRecords()) {
            em.persist(rec);
        }
        em.persist(cr);
        em.getTransaction().commit();
        log.debug("committed resvRecord for connId: "+cr.getConnectionId()+" v:"+rr.getVersion()+ " com: "+rr.isCommitted());

    }

    public static void abortResvRecord(String connId) throws ServiceException  {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        List<ResvRecord> uncom = ConnectionRecord.getUncommittedResvRecords(cr);
        if (uncom.size() == 0) {
            throw new ServiceException("zero resvRecords to abort for connId: "+connId);
        } else if (uncom.size() >1) {
            throw new ServiceException("multiple resvRecords to abort for connId: "+connId);
        }

        ResvRecord rr = uncom.get(0);
        cr.getResvRecords().remove(rr);

        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
        log.debug("aborted resvRecord for connId: "+cr.getConnectionId()+" v:"+rr.getVersion());
    }


    public static void createConnectionRecordIfNeeded(String connId, String requesterNSA, String nsiGlobalGri, String notifyUrl) throws ServiceException {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        if (cr != null) {
            log.info("connection record was found while starting reserve() for connectionId: " + connId);
        } else {
            EntityManager em = PersistenceHolder.getEntityManager();
            log.info("creating new connection record for connectionId: " + connId);
            em.getTransaction().begin();
            cr = new ConnectionRecord();
            cr.setConnectionId(connId);
            cr.setNsiGlobalGri(nsiGlobalGri);
            cr.setRequesterNSA(requesterNSA);
            cr.setNotifyUrl(notifyUrl);
            em.persist(cr);
            em.getTransaction().commit();
        }
    }


    public static void persistStateMachines(String connId) throws ServiceException {
        log.info("persisting state machines for connId: "+connId);
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);


        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
        NSI_Prov_SM psm = smh.findNsiProvSM(connId);
        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);

        cr.setLifecycleState(LifecycleStateEnumType.fromValue(lsm.getState().value()));
        cr.setProvisionState(ProvisionStateEnumType.fromValue(psm.getState().value()));
        cr.setReserveState(ReservationStateEnumType.fromValue(rsm.getState().value()));

        log.debug("  saving lsm state: " + lsm.getState().value());
        log.debug("  saving psm state: " + psm.getState().value());
        log.debug("  saving rsm state: " + rsm.getState().value());

        // save
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
    }



    public static void makeNewStateMachines(String connId) throws ServiceException {
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (!smh.hasStateMachines(connId)) {
            smh.makeStateMachines(connId);
        }

    }

    public static boolean restoreStateMachines(String connId) throws ServiceException {
        log.info("restoring state machines for connId: "+connId);
        boolean restoredLife = false;
        boolean restoredProv = false;
        boolean restoredResv = false;
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        if (cr == null) {
            throw new ServiceException("could not locate connection record for "+connId);
        }


        NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
        if (lsm == null) {
            lsm = new NSI_Life_SM(connId);
        }
        NSI_Prov_SM psm = smh.findNsiProvSM(connId);
        if (psm == null) {
            psm = new NSI_Prov_SM(connId);
        }
        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
        if (rsm == null) {
            rsm = new NSI_Resv_SM(connId);
        }

        // if we have restored the state
        if (cr.getLifecycleState() != null) {
            NSI_Life_State ls = new NSI_Life_State();
            ls.setState(cr.getLifecycleState());
            lsm.setState(ls);
            log.debug("  restored lsm state: "+lsm.getState().value());
            restoredLife = true;
        }

        if (cr.getProvisionState() != null) {
            NSI_Prov_State ps = new NSI_Prov_State();
            ps.setState(cr.getProvisionState());
            psm.setState(ps);
            log.debug("  restored psm state: "+psm.getState().value());

            restoredProv = true;
        }

        if (cr.getReserveState() != null) {
            NSI_Resv_State rs = new NSI_Resv_State();
            rs.setState(cr.getReserveState());
            rsm.setState(rs);
            log.debug("  restored rsm state: "+rsm.getState().value());

            restoredResv = true;
        }

        return (restoredLife && restoredProv && restoredResv);
    }



    public static boolean needNewOscarsResv(String connId) throws ServiceException {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        OscarsStatusRecord or = cr.getOscarsStatusRecord();
        if (or == null) {
            return true;

        } else {
            if (or.getStatus().equals(OscarsStates.CANCELLED)) {
                return true;
            } else if (or.getStatus().equals(OscarsStates.FAILED)) {
                return true;
            } else if (or.getStatus().equals(OscarsStates.UNKNOWN)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static ServiceExceptionType makeServiceException(String error) {
        ServiceExceptionType st = new ServiceExceptionType();
        // st.setNsaId();

        return st;
    }

    public static ConnectionRecord getConnectionRecord(String connectionId) throws ServiceException {
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c WHERE c.connectionId  = '"+connectionId+"'";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        em.getEntityManagerFactory().getCache().evictAll();
        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new ServiceException("internal error: found multiple connection records ("+recordList.size()+") with connectionId: "+connectionId);
        } else {
            em.refresh(recordList.get(0));
            // should not be needed
            for (ResvRecord rr : recordList.get(0).getResvRecords()) {
                em.refresh(rr);
            }
            return recordList.get(0);
        }
    }

    public static List<ConnectionRecord> getConnectionRecords() throws ServiceException {
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c";

        List<ConnectionRecord> results = new ArrayList<ConnectionRecord>();

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        for (ConnectionRecord cr: recordList) {
            em.refresh(cr);
            results.add(cr);
        }

        
        return recordList;
    }

    public static List<ConnectionRecord> getConnectionRecordsByGri(String nsiGlobalGri) throws ServiceException {
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c WHERE c.nsiGlobalGri = '"+nsiGlobalGri+"'";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        
        return recordList;
    }

    public static CommonHeaderType makeNsiOutgoingHeader(CommonHeaderType ph) {


        CommonHeaderType ht = new CommonHeaderType();
        ht.setCorrelationId(ph.getCorrelationId());
        ht.setProtocolVersion(ph.getProtocolVersion());
        ht.setProviderNSA(ph.getProviderNSA());
        ht.setRequesterNSA(ph.getRequesterNSA());

        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        HttpConfig hc = ax.getBean("httpConfig", HttpConfig.class);



        ht.setReplyTo(hc.getUrl()+"/ConnectionService");
        return ht;

    }
    public static Holder makeHolder(CommonHeaderType hd) {
        Holder h = new Holder();
        h.value = hd;
        return h;
    }

    public static void isConnectionOK(String connectionId) throws ServiceException {

        ConnectionRecord cr = NSI_Util.getConnectionRecord(connectionId);


        RequestHolder rh = RequestHolder.getInstance();
        if (rh == null) {
            throw new ServiceException("no requestHolder");
        }
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (smh == null) {
            throw new ServiceException("no stateMachineHolder");

        }
        if (!smh.hasStateMachines(connectionId)) {
            throw new ServiceException("no stateMachines for "+connectionId);
        }

    }

}
