package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import net.es.oscars.aaa.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.model.*;


public class AuthorizationModelRmiHandler extends ModelRmiHandlerImpl {
    private AAACore core = AAACore.getInstance();
    private Logger log;


    public AuthorizationModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
    }


    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("listAuthorizations.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        String listType = (String) parameters.get("listType");
        String attrName = (String) parameters.get("attributeName");
        String userName = (String) parameters.get("userName");
        if (listType == null) {
            listType = "plain";
        }

        AuthorizationDAO authorizationDAO = new AuthorizationDAO(core.getAaaDbName());
        List<Authorization> authorizations = new ArrayList<Authorization>();
        if (listType.equals("plain")) {
            authorizations = authorizationDAO.list();
        } else if (listType.equals("byAttrName")) {
            try {
                authorizations = authorizationDAO.listAuthByAttr(attrName);
            } catch (AAAException ex) {
                this.log.error("exception: " + ex.getMessage());
                aaa.getTransaction().rollback();
                throw new RemoteException(ex.getMessage());
            }
        } else if (listType.equals("byUser")) {
            try {
                authorizations = authorizationDAO.listAuthByUser(userName);
            } catch (AAAException ex) {
                this.log.error("exception: " + ex.getMessage());
                aaa.getTransaction().rollback();
                throw new RemoteException(ex.getMessage());
            }
        } else if (listType.equals("ordered")) {
            authorizations = authorizationDAO.orderedList();
        }
        for (Authorization authorization : authorizations) {
            Hibernate.initialize(authorization);
            Hibernate.initialize(authorization.getConstraint());
            Hibernate.initialize(authorization.getPermission());
            Hibernate.initialize(authorization.getResource());
            Hibernate.initialize(authorization.getAttribute());
        }
        result.put("authorizations", authorizations);


        aaa.getTransaction().commit();
        this.log.debug("listAuthorizations.end");
        return result;
    }
    public HashMap<String, Object> add(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("addAuthorization.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        AuthorizationDAO authDAO = new AuthorizationDAO(core.getAaaDbName());
        String attributeName = (String) parameters.get("attributeName");
        String resourceName = (String) parameters.get("resourceName");
        String permissionName = (String) parameters.get("permissionName");
        String constraintName = (String) parameters.get("constraintName");
        String constraintValue = (String) parameters.get("constraintValue");
        try {
            authDAO.create(attributeName, resourceName, permissionName, constraintName, constraintValue);
        } catch ( Exception e) {
            this.log.error("exception: " + e.getMessage());
            aaa.getTransaction().rollback();
            throw new RemoteException(e.getMessage());
        }
        aaa.getTransaction().commit();
        this.log.debug("addAuthorization.end");
        return result;
    }

    public HashMap<String, Object> modify(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("modifyAuthorization.start");
        Session aaa = core.getAaaSession();
        HashMap<String, Object> result = new HashMap<String, Object>();

        aaa.beginTransaction();

        String attributeName = (String) parameters.get("attributeName");
        String resourceName = (String) parameters.get("resourceName");
        String permissionName = (String) parameters.get("permissionName");
        String constraintName = (String) parameters.get("constraintName");
        String constraintValue = (String) parameters.get("constraintValue");

        String oldAttributeName = (String) parameters.get("oldAttributeName");
        String oldResourceName = (String) parameters.get("oldResourceName");
        String oldPermissionName = (String) parameters.get("oldPermissionName");
        String oldConstraintName = (String) parameters.get("oldConstraintName");

        AuthorizationDAO authDAO = new AuthorizationDAO(core.getAaaDbName());
        try {
            Authorization auth = authDAO.query(oldAttributeName, oldResourceName, oldPermissionName, oldConstraintName);
            authDAO.update(auth, attributeName, resourceName, permissionName, constraintName, constraintValue);
        } catch ( Exception e) {
            this.log.error("exception: " + e.getMessage());
            aaa.getTransaction().rollback();
            throw new RemoteException(e.getMessage());
        }

        aaa.getTransaction().commit();
        this.log.debug("modifyAuthorization.end");
        return result;
    }

    public HashMap<String, Object> delete(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("deleteAuthorization.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();
        String attributeName = (String) parameters.get("attributeName");
        String resourceName = (String) parameters.get("resourceName");
        String permissionName = (String) parameters.get("permissionName");
        String constraintName = (String) parameters.get("constraintName");

        AuthorizationDAO authDAO = new AuthorizationDAO(core.getAaaDbName());
        try {
            authDAO.remove(attributeName, resourceName, permissionName, constraintName);
        } catch ( Exception e) {
            this.log.error("exception: " + e.getMessage());
            aaa.getTransaction().rollback();
            throw new RemoteException(e.getMessage());
        }
        aaa.getTransaction().commit();
        this.log.debug("deleteAuthorization.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("findAuthorizationById.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        AuthorizationDAO authorizationDAO = new AuthorizationDAO(core.getAaaDbName());
        Authorization authorization = authorizationDAO.findById(id, false);

        Hibernate.initialize(authorization);
        Hibernate.initialize(authorization.getConstraint());
        Hibernate.initialize(authorization.getPermission());
        Hibernate.initialize(authorization.getResource());
        Hibernate.initialize(authorization.getAttribute());


        result.put("authorization", authorization);
        aaa.getTransaction().commit();
        this.log.debug("findAuthorizationById.end");
        return result;
    }


}
