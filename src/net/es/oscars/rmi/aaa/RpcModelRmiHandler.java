package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.model.*;


public class RpcModelRmiHandler extends ModelRmiHandlerImpl {
    private OSCARSCore core;
    private Logger log;


    public RpcModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("listRpcs.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        RpcDAO rpcDAO = new RpcDAO(core.getAaaDbName());
        List<Rpc> rpcs = rpcDAO.list();
        for (Rpc rpc : rpcs) {
            Hibernate.initialize(rpc);
            Hibernate.initialize(rpc.getConstraint());
            Hibernate.initialize(rpc.getPermission());
            Hibernate.initialize(rpc.getResource());
        }
        result.put("rpcs", rpcs);

        aaa.getTransaction().commit();
        this.log.debug("listRpc.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("findRpc.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        RpcDAO rpcDAO = new RpcDAO(core.getAaaDbName());
        Rpc rpc = rpcDAO.findById(id, false);

        Hibernate.initialize(rpc);
        Hibernate.initialize(rpc.getConstraint());
        Hibernate.initialize(rpc.getPermission());
        Hibernate.initialize(rpc.getResource());

        result.put("rpc", rpc);
        aaa.getTransaction().commit();
        this.log.debug("findRpc.end");
        return result;
    }

}
