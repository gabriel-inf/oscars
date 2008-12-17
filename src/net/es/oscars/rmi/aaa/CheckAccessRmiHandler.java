package net.es.oscars.rmi.aaa;


import java.util.*;
import java.rmi.RemoteException;
import org.apache.log4j.*;
import org.hibernate.Session;

import net.es.oscars.aaa.*;


public class CheckAccessRmiHandler {
    private AAACore core = AAACore.getInstance();
    private Logger log = Logger.getLogger(CheckAccessRmiHandler.class);


    public AuthValue checkAccess(String userName, String resourceName, String permissionName) throws RemoteException {
        this.log.debug("checkAccess.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();
        AuthValue auth = um.checkAccess(userName, resourceName, permissionName);
        aaa.getTransaction().commit();
        this.log.debug("checkAccess.end");
        return auth;
    }

    public String getInstitution(String userName) throws RemoteException {
        this.log.debug("getInstitution.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();
        String institution = um.getInstitution(userName);
        aaa.getTransaction().commit();
        this.log.debug("getInstitution.end");
        return institution;
    }


    public AuthMultiValue checkMultiAccess(String username, HashMap<String, ArrayList<String>> resourcePermissions) throws RemoteException {
        this.log.debug("checkMultiAccess.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        AuthMultiValue result = new AuthMultiValue();

        try {
            UserManager um = core.getUserManager();
            Iterator<String> resIt = resourcePermissions.keySet().iterator();
            while (resIt.hasNext()) {
                String resourceName = resIt.next();
                HashMap<String, AuthValue> permMap = new HashMap<String, AuthValue>();
                for (String permissionName : resourcePermissions.get(resourceName)) {
                    AuthValue auth = um.checkAccess(username, resourceName, permissionName);
                    permMap.put(permissionName, auth);
                }
                result.put(resourceName, permMap);
            }
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        }
        this.log.debug("checkMultiAccess.end");
        return result;
    }


    public AuthValue checkModResAccess(String userName, String resourceName, String permissionName,
            int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI) throws RemoteException {
        this.log.debug("checkModResAccess.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();

        AuthValue auth = um.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
        aaa.getTransaction().commit();
        this.log.debug("checkModResAccess.end");
        return auth;
    }
}
