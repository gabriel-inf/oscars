package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.config.SpringContext;
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
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.xml.ws.Holder;
import java.util.Date;
import java.util.List;

public class NSI_Util {
    public static OscarsStatusRecord getLatestOscarsRecord(String connectionId) throws ServiceException {
        EntityManager em = PersistenceHolder.getInstance().getEntityManager();
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

    public static ServiceExceptionType makeServiceException(String error) {
        ServiceExceptionType st = new ServiceExceptionType();
        // st.setNsaId();

        return st;
    }

    public static ConnectionRecord getConnectionRecord(String connectionId) throws ServiceException {
        EntityManager em = PersistenceHolder.getInstance().getEntityManager();

        em.getTransaction().begin();
        String query = "SELECT c FROM ConnectionRecord c WHERE c.connectionId  = '"+connectionId+"'";

        List<ConnectionRecord> recordList = em.createQuery(query, ConnectionRecord.class).getResultList();
        em.getTransaction().commit();
        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new ServiceException("internal error: found multiple connection records ("+recordList.size()+") with connectionId: "+connectionId);
        } else {
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
