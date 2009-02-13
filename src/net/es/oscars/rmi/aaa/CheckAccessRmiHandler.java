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

    public Boolean
    checkDomainAccess(String userName,String institutionName, String srcTopologyId,String destTopologyId)
        throws RemoteException {
        this.log.debug("checkDomainAccess.start");
        Boolean result = false;
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        if (institutionName == null && userName != null) {
            UserManager um = core.getUserManager();
            institutionName = um.getInstitution(userName);
        }
        if (institutionName !=null) {
            InstitutionDAO instDAO = new InstitutionDAO(core.getAaaDbName());
            Institution inst = instDAO.queryByName(institutionName);
            //this.log.debug("checkDomainAccess institution " + inst.getName());
            Set<Site> sites = inst.getSites();
            //this.log.debug("found sites: " + sites.size());
            for (Site site: sites) {
                if (site.getDomain().equals(srcTopologyId) ||
                        site.getDomain().equals(destTopologyId)){
                    result = true;
                    break;
                }
            }
        }
 
        aaa.getTransaction().commit();
        this.log.debug("checkDomainAccess.end result is " + result);
        return result;
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


    /** 
     * 
     * @param username
     * @param resourcePermissions a hashmap of resource name, and an array of desired permission
     * @return AuthMultiValue a hashmap of resource names and hashmaps of permission names and authValues
     * @throws RemoteException
     */
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
