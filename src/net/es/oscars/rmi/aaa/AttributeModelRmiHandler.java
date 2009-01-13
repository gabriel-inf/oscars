package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Session;
import org.hibernate.Hibernate;

import net.es.oscars.aaa.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.model.*;
import net.es.oscars.servlets.ServletUtils;


public class AttributeModelRmiHandler extends ModelRmiHandlerImpl {
    private AAACore core = AAACore.getInstance();
    private Logger log;

    public AttributeModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.debug("listAttributes.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        List<Attribute> attributes;
        String listBy = (String) parameters.get("listBy");
        if (listBy == null || listBy.equals("plain")) {
            AttributeDAO attributeDAO = new AttributeDAO(core.getAaaDbName());
            attributes = attributeDAO.list();
        } else if (listBy.equals("username")) {
            String username = (String) parameters.get("username");
            if (username == null) {
                aaa.getTransaction().rollback();
                throw new RemoteException("Invalid parameter to AttributeRmiHandler.list: No username");
            }
            UserManager mgr = core.getUserManager();
            attributes = mgr.getAttributesForUser(username);
        } else {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.list: unknown listBy: " + listBy);
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (Attribute attr: attributes) {
            Hibernate.initialize(attr);
        }
        aaa.getTransaction().commit();
        result.put("attributes", attributes);
        this.log.debug("listAttributes.end");
        return result;
    }

    public HashMap<String, Object> add(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.info("addAttribute.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();
        AttributeDAO dao = new AttributeDAO(core.getAaaDbName());
        Attribute attribute = (Attribute) parameters.get("attribute");
        if (attribute == null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.add: No attribute set");
        }
        Attribute oldAttribute = dao.queryByParam("name", attribute.getName());
        if (oldAttribute != null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("already found attribute with name:" +
                                      attribute.getName());
        }
        dao.create(attribute);
        aaa.getTransaction().commit();
        this.log.info("addAttribute.end");
        return result;
    }

    public HashMap<String, Object> modify(HashMap<String, Object> parameters)
           throws RemoteException {

        this.log.debug("modifyAttribute.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();
        Attribute attribute = (Attribute) parameters.get("attribute");
        if (attribute == null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.modify: No attribute set");
        }
        String oldName = (String) parameters.get("oldName");
        if (oldName == null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.modify: oldName not set");
        }
        AttributeDAO dao = new AttributeDAO(core.getAaaDbName());
        Attribute oldAttribute = dao.queryByParam("name", oldName);
        if (oldAttribute == null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.modify: Attribute " + oldName + " does not exist to be modified");
        }
        oldAttribute.setName(attribute.getName());
        oldAttribute.setDescription(attribute.getDescription());
        oldAttribute.setAttrType(attribute.getAttrType());
        dao.update(oldAttribute);
        aaa.getTransaction().commit();
        this.log.debug("modifyAttribute.end");
        return result;
    }

    public HashMap<String, Object> delete(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.debug("deleteAttribute.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();
        String attributeName = (String) parameters.get("name");
        if (attributeName == null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.delete: Attribute name not set");
        }
        AttributeDAO dao = new AttributeDAO(core.getAaaDbName());
        UserAttributeDAO userAttributeDAO =
            new UserAttributeDAO(core.getAaaDbName());
        AuthorizationDAO authDAO = new AuthorizationDAO(core.getAaaDbName());
        Attribute attribute = dao.queryByParam("name", attributeName);
        boolean existingUsers = false;
        boolean existingAuthorizations = false;
        if (attribute == null) {
            aaa.getTransaction().rollback();
            throw new RemoteException("Invalid parameter to AttributeRmiHandler.delete: Attribute " + 
                    attributeName + " does not exist to be deleted");
        }
        try {
            List<User> users =
                userAttributeDAO.getUsersByAttribute(attributeName);
            StringBuilder sb = new StringBuilder();
            if (users.size() != 0) {
                sb.append(attributeName + " has existing users: ");
                for (User user: users) {
                    sb.append(user.getLogin() + " ");
                }
                existingUsers = true;
            }
            List<Authorization> auths = authDAO.listAuthByAttr(attributeName);
            if (auths.size() != 0) {
                if (existingUsers) {
                    sb.append(".  There are " + auths.size() +
                            " existing authorizations with this attribute.");
                } else {
                    sb.append(attributeName + " has " + auths.size() +
                            " associated authorizations.  Cannot delete.");
                }
                existingAuthorizations = true;
            }
            if (existingUsers || existingAuthorizations) {
                throw new AAAException(sb.toString());
            }
        } catch (AAAException ex) {
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        }
        dao.remove(attribute);
        aaa.getTransaction().commit();
        this.log.debug("deleteAttribute.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.debug("findAttribute.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        AttributeDAO AttributeDAO = new AttributeDAO(core.getAaaDbName());
        Attribute attribute = AttributeDAO.findById(id, false);
        Hibernate.initialize(attribute);
        result.put("attribute", attribute);
        aaa.getTransaction().commit();
        this.log.debug("findAttribute.end");
        return result;
    }
}
