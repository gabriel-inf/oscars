package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Session;

import net.es.oscars.aaa.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.model.*;


public class PermissionModelRmiHandler extends ModelRmiHandlerImpl  {
    private AAACore core = AAACore.getInstance();
    private Logger log;


    public PermissionModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("listPermissions.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        PermissionDAO permissionDAO = new PermissionDAO(core.getAaaDbName());
        List<Permission> permissions = permissionDAO.list();
        result.put("permissions", permissions);

        aaa.getTransaction().commit();
        this.log.debug("listPermission.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("findPermission.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        PermissionDAO permissionDAO = new PermissionDAO(core.getAaaDbName());
        Permission permission = permissionDAO.findById(id, false);
        result.put("permission", permission);
        aaa.getTransaction().commit();
        this.log.debug("findPermission.end");
        return result;
    }

}
