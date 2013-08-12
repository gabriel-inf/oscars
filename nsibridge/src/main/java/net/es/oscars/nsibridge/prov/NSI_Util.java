package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.nsibridge.common.JettyContainer;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.state.actv.NSI_Actv_SM;
import net.es.oscars.nsibridge.state.actv.NSI_Actv_State;
import net.es.oscars.nsibridge.state.actv.NSI_Actv_StateEnum;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;

import javax.xml.ws.Holder;

public class NSI_Util {
    public static ConnectionStatesType makeConnectionStates(String connId) throws Exception {

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
        NSI_Actv_SM asm = smh.getActStateMachines().get(connId);
        NSI_Prov_SM psm = smh.getProvStateMachines().get(connId);
        NSI_Life_SM tsm = smh.getTermStateMachines().get(connId);
        if (rsm == null) {

        }
        if (asm == null) {

        }
        if (psm == null) {

        }
        if (tsm == null) {

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

        NSI_Actv_State as = (NSI_Actv_State) asm.getState();
        NSI_Actv_StateEnum ase = (NSI_Actv_StateEnum) as.state();

        NSI_Prov_State ps = (NSI_Prov_State) psm.getState();
        ProvisionStateEnumType pse = (ProvisionStateEnumType) ps.state();

        NSI_Resv_State rs = (NSI_Resv_State) rsm.getState();
        ReservationStateEnumType rse = (ReservationStateEnumType) rs.state();

        NSI_Life_State ls = (NSI_Life_State) tsm.getState();
        LifecycleStateEnumType lse = (LifecycleStateEnumType) ls.state();


        switch (ase) {
            case INACTIVE:
                dt.setActive(false);
                break;
            case ACTIVE:
                dt.setActive(true);
                break;
            case ACTIVATING:
                dt.setActive(false);
                break;
            case DEACTIVATING:
                dt.setActive(true);
                break;
            default:
                dt.setActive(false);
        }

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

    public static CommonHeaderType makeNsiOutgoingHeader(CommonHeaderType ph) {


        CommonHeaderType ht = new CommonHeaderType();
        ht.setCorrelationId(ph.getCorrelationId());
        ht.setProtocolVersion(ph.getProtocolVersion());
        ht.setProviderNSA(ph.getProviderNSA());
        ht.setRequesterNSA(ph.getRequesterNSA());


        JettyConfig jc = JettyContainer.getInstance().getConfig();
        String hostname = jc.getHttp().getHostname();
        Integer port = jc.getHttp().getPort();

        ht.setReplyTo("http://"+hostname+":"+port+"/ConnectionService");
        return ht;

    }
    public static Holder makeHolder(CommonHeaderType hd) {
        Holder h = new Holder();
        h.value = hd;
        return h;
    }

}
