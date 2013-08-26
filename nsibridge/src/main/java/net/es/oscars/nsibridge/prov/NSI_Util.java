package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;
import java.util.Date;
import java.util.List;

public class NSI_Util {

    private static final Logger log = Logger.getLogger(NSI_Util.class);

    public static OscarsStatusRecord getLatestOscarsRecord(String connectionId) throws ServiceException {
        EntityManager em = PersistenceHolder.getEntityManager();
        ConnectionRecord cr = getConnectionRecord(connectionId);
        Date latest = null;
        OscarsStatusRecord result = null;
        for (OscarsStatusRecord or : cr.getOscarsStatusRecords()) {
            if (latest == null) {
                result = or;
            } else if (or.getDate().after(latest)) {
                result = or;
            }

        }
        return result;
    }

    public static ConnectionStatesType makeConnectionStates(String connId) throws Exception {

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
        NSI_Prov_SM psm = smh.getProvStateMachines().get(connId);
        NSI_Life_SM lsm = smh.getLifeStateMachines().get(connId);
        if (rsm == null) {

        }

        if (psm == null) {

        }
        if (lsm == null) {

        }


        ConnectionStatesType cst = new ConnectionStatesType();

        LifecycleStateEnumType lt = null;
        cst.setLifecycleState(lt);

        ReservationStateEnumType rt = null;
        cst.setReservationState(rt);

        DataPlaneStatusType dt = new DataPlaneStatusType();
        dt.setVersion(1);
        dt.setActive(false);
        cst.setDataPlaneStatus(dt);

        ProvisionStateEnumType pt = null;
        cst.setProvisionState(pt);

        NSI_Prov_State ps = (NSI_Prov_State) psm.getState();
        ProvisionStateEnumType pse = (ProvisionStateEnumType) ps.state();

        NSI_Resv_State rs = (NSI_Resv_State) rsm.getState();
        ReservationStateEnumType rse = (ReservationStateEnumType) rs.state();

        NSI_Life_State ls = (NSI_Life_State) lsm.getState();
        LifecycleStateEnumType lse = (LifecycleStateEnumType) ls.state();




        cst.setDataPlaneStatus(dt);
        cst.setLifecycleState(lse);
        cst.setProvisionState(pse);
        cst.setReservationState(rse);

        return cst;

    }

    public static void createConnectionRecordIfNeeded(String connId, String requesterNSA, String nsiGlobalGri) throws ServiceException {
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

        // TODO: not good
        ResvRecord rr = new ResvRecord();
        rr.setReservationState(ReservationStateEnumType.fromValue(rsm.getState().value()));
        rr.setDate(new Date());
        rr.setVersion(0);
        cr.getResvRecords().add(rr);

        log.debug("  saving lsm state: "+lsm.getState().value());
        log.debug("  saving psm state: "+psm.getState().value());
        log.debug("  saving rsm state: "+rsm.getState().value()+ " date: "+rr.getDate());

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
        NSI_Prov_SM psm = smh.findNsiProvSM(connId);
        NSI_Resv_SM rsm = smh.findNsiResvSM(connId);

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

        ResvRecord rr = ConnectionRecord.getLatestResvRecord(cr);
        if (rr != null) {
            NSI_Resv_State rs = new NSI_Resv_State();
            rs.setState(rr.getReservationState());
            rsm.setState(rs);
            log.debug("  restored rsm state: "+rsm.getState().value()+" date: "+rr.getDate());

            restoredResv = true;
        }

        return (restoredLife && restoredProv && restoredResv);
    }



    public static boolean needNewOscarsResv(String connId) throws ServiceException {
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        OscarsStatusRecord or = ConnectionRecord.getLatestStatusRecord(cr);
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
        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new ServiceException("internal error: found multiple connection records ("+recordList.size()+") with connectionId: "+connectionId);
        } else {
            em.refresh(recordList.get(0));
            return recordList.get(0);
        }
    }

    public static CommonHeaderType makeNsiOutgoingHeader(CommonHeaderType ph) {


        CommonHeaderType ht = new CommonHeaderType();
        ht.setCorrelationId(ph.getCorrelationId());
        ht.setProtocolVersion(ph.getProtocolVersion());
        ht.setProviderNSA(ph.getProviderNSA());
        ht.setRequesterNSA(ph.getRequesterNSA());

        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");
        HttpConfig hc = ax.getBean("httpConfig", HttpConfig.class);



        ht.setReplyTo(hc.getUrl()+"/ConnectionService");
        return ht;

    }
    public static Holder makeHolder(CommonHeaderType hd) {
        Holder h = new Holder();
        h.value = hd;
        return h;
    }

}
