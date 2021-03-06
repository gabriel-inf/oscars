package net.es.oscars.nsibridge.prov;

import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.beans.db.*;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.services.point2point.P2PServiceBaseType;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_State;
import net.es.oscars.nsibridge.state.life.NSI_Life_TH;
import net.es.oscars.nsibridge.state.life.NSI_UP_Life_Impl;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.nsibridge.state.prov.NSI_UP_Prov_Impl;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_TH;
import net.es.oscars.nsibridge.state.resv.NSI_UP_Resv_Impl;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DB_Util {
    private static final Logger log = Logger.getLogger(DB_Util.class);
    private static HashMap<String, ConnectionRecord> connectionRecords = new HashMap<String, ConnectionRecord>();

    public static void updateDataplaneRecord(ConnectionRecord cr, OscarsStates os, Integer version) throws ServiceException {

        DataplaneStatusRecord dsr = null;
        for (DataplaneStatusRecord d : cr.getDataplaneStatusRecords()) {
            if (d.getVersion() == version) {
                log.debug("updating dataplane record: connId: ["+cr.getConnectionId()+"] status: "+os+" version: "+d.getVersion());
                dsr = d;
            }
        }
        if (dsr == null) {
            dsr = new DataplaneStatusRecord();
            dsr.setVersion(version);
            cr.getDataplaneStatusRecords().add(dsr);
            log.debug("inserting new dataplane record: connId: ["+cr.getConnectionId()+"] status: "+os);
        }
        if (os.equals(OscarsStates.ACTIVE)) {
            dsr.setActive(true);
        } else {
            dsr.setActive(false);
        }
        log.debug("updated dataplane record: connId: ["+cr.getConnectionId()+"] status: "+os+" active: "+dsr.isActive()+" version: "+dsr.getVersion());
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();


    }

    public static void createResvRecord(String connId, ReserveType rt) throws ServiceException {
        ConnectionRecord cr = getConnectionRecord(connId);
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
        P2PServiceBaseType p2pt = null;
        for (Object o : crit.getAny()) {
            if (o instanceof P2PServiceBaseType ) {
                p2pt = (P2PServiceBaseType) o;
            } else {
                try {

                    JAXBElement<P2PServiceBaseType> payload = (JAXBElement<P2PServiceBaseType>) o;
                    p2pt = payload.getValue();
                } catch (ClassCastException ex) {
                    log.error(ex);
                    p2pt = null;
                }
            }
        }

        if (p2pt == null) {
            throw new ServiceException("Missing P2PServiceBaseType element!");
        }

        Long capacity = p2pt.getCapacity();

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
        log.debug("commiting resv record for connId: "+connId);
        ConnectionRecord cr = getConnectionRecord(connId);
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

        Long resvTimeout = new Double(tx.getResvTimeout()).longValue() * 1000;
        Date now = new Date();

        if (submittedAt.getTime() + resvTimeout < now.getTime()) {
            Date timeout = new Date(submittedAt.getTime()+resvTimeout);
            log.debug("submitted at:"+submittedAt+" now: "+now+ " timeout: "+timeout);
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
        ConnectionRecord cr = getConnectionRecord(connId);
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


    public static void persistStateMachines(String connId) throws ServiceException {
        log.info("persisting state machines for connId: "+connId);
        ConnectionRecord cr = getConnectionRecord(connId);


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

    public static boolean restoreStateMachines(String connId) throws ServiceException {
        // log.info("restoring state machines for connId: "+connId);
        boolean restoredLife = false;
        boolean restoredProv = false;
        boolean restoredResv = false;
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();

        ConnectionRecord cr = getConnectionRecord(connId);
        if (cr == null) {
            throw new ServiceException("could not locate connection record for "+connId);
        }


        NSI_Life_SM lsm = smh.findNsiLifeSM(connId);
        if (lsm == null) {
            lsm = new NSI_Life_SM(connId);
            smh.getLifeStateMachines().put(connId, lsm);
            NSI_Life_TH lth = new NSI_Life_TH();
            lsm.setTransitionHandler(lth);
            NSI_UP_Life_Impl lml = new NSI_UP_Life_Impl(connId);
            lth.setMdl(lml);

        }
        NSI_Prov_SM psm = smh.findNsiProvSM(connId);
        if (psm == null) {
            psm = new NSI_Prov_SM(connId);
            smh.getProvStateMachines().put(connId, psm);
            NSI_Prov_TH pth = new NSI_Prov_TH();
            psm.setTransitionHandler(pth);
            NSI_UP_Prov_Impl pml = new NSI_UP_Prov_Impl(connId);
            pth.setMdl(pml);
        }
        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);
        if (rsm == null) {
            rsm = new NSI_Resv_SM(connId);
            smh.getResvStateMachines().put(connId, rsm);
            NSI_Resv_TH rth = new NSI_Resv_TH();
            rsm.setTransitionHandler(rth);
            NSI_UP_Resv_Impl rml = new NSI_UP_Resv_Impl(connId);
            rth.setMdl(rml);
        }

        // if we have restored the state
        if (cr.getLifecycleState() != null) {
            NSI_Life_State ls = new NSI_Life_State();
            ls.setState(cr.getLifecycleState());
            lsm.setState(ls);
            // log.debug("  restored lsm state: "+lsm.getState().value());
            restoredLife = true;
        }

        if (cr.getProvisionState() != null) {
            NSI_Prov_State ps = new NSI_Prov_State();
            ps.setState(cr.getProvisionState());
            psm.setState(ps);
            // log.debug("  restored psm state: "+psm.getState().value());

            restoredProv = true;
        }

        if (cr.getReserveState() != null) {
            NSI_Resv_State rs = new NSI_Resv_State();
            rs.setState(cr.getReserveState());
            rsm.setState(rs);
            // log.debug("  restored rsm state: "+rsm.getState().value());

            restoredResv = true;
        }

        return (restoredLife && restoredProv && restoredResv);
    }




    public static void createConnectionRecordIfNeeded(String connId, String requesterNSA, String nsiGlobalGri, String notifyUrl, SubjectAttributes attrs) throws ServiceException {
        ConnectionRecord cr = getConnectionRecord(connId);
        if (cr != null) {
            log.info("connection record was found while starting reserve() for connectionId: " + connId);
        } else {
            EntityManager em = PersistenceHolder.getEntityManager();
            log.info("creating new connection record for connectionId: " + connId+ " reqNSA: "+requesterNSA);
            cr = new ConnectionRecord();
            cr.setConnectionId(connId);
            cr.setNsiGlobalGri(nsiGlobalGri);
            cr.setRequesterNSA(requesterNSA);
            cr.setNotifyUrl(notifyUrl);
            try {
                cr.setOscarsAttributes(makeAttrString(attrs));
            } catch (JAXBException ex) {
                log.error(ex.getMessage(), ex);
                throw new ServiceException(ex.getMessage());
            }


            em.getTransaction().begin();
            em.persist(cr);
            em.getTransaction().commit();
        }
    }
    public static String makeAttrString(SubjectAttributes attrs) throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(SubjectAttributes.class);
        JAXBElement<SubjectAttributes> jaxb = new JAXBElement(new QName(SubjectAttributes.class.getSimpleName()), SubjectAttributes.class, attrs);

        Marshaller m = context.createMarshaller();
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(jaxb, writer);
        log.debug(writer.toString());
        return writer.toString();
    }

    public static SubjectAttributes getAttributes(String attrString) throws JAXBException, XMLStreamException {
        JAXBContext context = JAXBContext.newInstance(SubjectAttributes.class);
        Unmarshaller m = context.createUnmarshaller();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(attrString));

        JAXBElement<SubjectAttributes> userElement = m.unmarshal(reader, SubjectAttributes.class);
        SubjectAttributes attrs = userElement.getValue();

        return attrs;

    }


    public static ExceptionRecord getExceptionRecord(String connectionId, String correlationId)  throws ServiceException {
        ConnectionRecord cr = getConnectionRecord(connectionId);
        for (ExceptionRecord err : cr.getExceptionRecords()) {
            if (err.getCorrelationId().equals(correlationId)) {
                return err;
            }
        }
        throw new ServiceException("could not find ExceptionRecord connId: "+connectionId+" corrId: "+correlationId);
    }

    public static void saveException(String connectionId, String errorId, String correlationId, String exceptionString) throws ServiceException {
        log.debug("saving exception: connId: "+connectionId+" errorId: "+errorId+" corrId: "+correlationId+" ["+exceptionString+"]");
        ConnectionRecord cr = getConnectionRecord(connectionId);
        ExceptionRecord er = new ExceptionRecord();
        er.setErrorId(errorId);
        er.setCorrelationId(correlationId);
        er.setTimestamp(new Date());
        er.setExceptionString(exceptionString);
        cr.getExceptionRecords().add(er);

        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
    }

    public static Long makeNotification(String connectionId, EventEnumType eventType, CallbackMessages notificationType) throws ServiceException {
        ConnectionRecord cr = getConnectionRecord(connectionId);
        NotificationRecord nr = new NotificationRecord();
        nr.setEventType(EventEnumType.FORCED_END);
        nr.setTimestamp(new Date());
        nr.setNotificationType(notificationType);
        cr.getNotificationRecords().add(nr);


        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(cr);
        em.getTransaction().commit();
        return nr.getId();

    }


    public static NotificationRecord getNotificationRecord(String connectionId, Long notificationId) throws ServiceException  {
        ConnectionRecord cr = getConnectionRecord(connectionId);
        for (NotificationRecord nr : cr.getNotificationRecords()) {
            if (nr.getId().equals(notificationId)) return nr;
        }
        throw new ServiceException("could not find NotificationRecord connId: "+connectionId+" notId: "+notificationId);

    }

    public static ConnectionRecord getConnectionRecord(String connectionId) throws ServiceException {
        return DB_Util.getConnectionRecord(connectionId, null);
    }

    public static ConnectionRecord getConnectionRecord(String connectionId, String requester) throws ServiceException {

        EntityManager em = PersistenceHolder.getEntityManager();
        em.getEntityManagerFactory().getCache().evictAll();
        em.getTransaction().begin();
        em.flush();

        String query = "SELECT c FROM ConnectionRecord c WHERE c.connectionId  = '"+connectionId+"'";
        if (requester != null) {
            query += " AND c.requesterNSA = '" + requester + "'";
        }
        // log.debug(query);
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();

        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new ServiceException("internal error: found multiple connection records ("+recordList.size()+") with connectionId: "+connectionId);
        } else {
            em.refresh(recordList.get(0));
            return recordList.get(0);
        }
    }


    public static List<ConnectionRecord> getConnectionRecords() throws ServiceException {
        return getConnectionRecords(null);
    }
    
    public static List<ConnectionRecord> getConnectionRecords(String requester) throws ServiceException {

        List<ConnectionRecord> results = new ArrayList<ConnectionRecord>();

        EntityManager em = PersistenceHolder.getEntityManager();

        em.getEntityManagerFactory().getCache().evictAll();
        em.getTransaction().begin();
        em.flush();

        String query = "SELECT c FROM ConnectionRecord c";
        if(requester != null){
            query += " WHERE c.requesterNSA = '" + requester + "'";
        }
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();

        for (ConnectionRecord cr: recordList) {
            em.refresh(cr);
            results.add(cr);
        }


        return results;
    }

    public static List<ConnectionRecord> getConnectionRecordsByGri(String nsiGlobalGri, String requester) throws ServiceException {
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getEntityManagerFactory().getCache().evictAll();
        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c WHERE c.nsiGlobalGri = '"+nsiGlobalGri+"'";
        if (requester != null) {
            query += " AND c.requesterNSA = '" + requester + "'";
        }
        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();

        return recordList;
    }


}
