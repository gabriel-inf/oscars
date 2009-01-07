package net.es.oscars.rmi.aaa;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Session;
import org.hibernate.Hibernate;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.model.*;

public class UserModelRmiHandler extends ModelRmiHandlerImpl {
    private Logger log;
    private AAACore core = AAACore.getInstance();

    public UserModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.debug("listUsers.start");
        Session aaa = core.getAaaSession();
        HashMap<String, Object> result = new HashMap<String, Object>();
        String listType = (String) parameters.get("listType");
        if (listType == null) {
            listType = "plain";
        }
        List<User> users = new ArrayList<User>();

        try {
            aaa.beginTransaction();
            if (listType.equals("plain")) {
                UserDAO userDAO = new UserDAO(core.getAaaDbName());
                users = userDAO.list();

            } else if (listType.equals("byAttr")) {
                String attributeName = (String) parameters.get("attributeName");
                if (attributeName == null) {
                    aaa.getTransaction().rollback();

                    throw new RemoteException("attributeName not specified");
                }
                UserAttributeDAO dao = new UserAttributeDAO(core.getAaaDbName());
                try {
                    users = dao.getUsersByAttribute(attributeName);
                } catch (AAAException ex) {
                    aaa.getTransaction().rollback();

                    throw new RemoteException(ex.getMessage());
                }
            } else if (listType.equals("single")) {
                String userName = (String) parameters.get("username");
                if (userName == null) {
                    aaa.getTransaction().rollback();

                    throw new RemoteException("username not specified");
                }
                UserManager mgr = core.getUserManager();
                User user = mgr.query(userName);
                users = new ArrayList<User>();
                users.add(user);
            } else {
                throw new RemoteException("unknown listType");
            }
            for (User user : users) {
                Hibernate.initialize(user);
                Hibernate.initialize(user.getInstitution());
                Hibernate.initialize(user.getInstitution().getUsers());
            }
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }
        result.put("users", users);
        this.log.debug("listUsers.end");
        return result;
    }

    public HashMap<String, Object> add(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.debug("addUser.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList<String> addRoles =
            (ArrayList<String>) parameters.get("addRoles");
        User user = (User) parameters.get("user");
        if (user == null) {
            throw new RemoteException("User not set");
        } else if (addRoles == null) {
            throw new RemoteException("Roles not set");
        }

        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        UserManager mgr = core.getUserManager();
        try {
            mgr.create(user, addRoles);
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }
        this.log.debug("addUser.end");
        return result;
    }

    public HashMap<String, Object> modify(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.debug("modifyUser.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList<String> newRoles =
            (ArrayList<String>) parameters.get("newRoles");
        ArrayList<String> curRoles =
            (ArrayList<String>) parameters.get("curRoles");

        // transient
        User modifiedUser = (User) parameters.get("user");
        Boolean setPassword = (Boolean) parameters.get("setPassword");
        if (setPassword == null) {
            setPassword = false;
        }
        if (modifiedUser == null) {
            throw new RemoteException("User not set");
        } else if (newRoles == null) {
            throw new RemoteException("Roles not set");
        }
        Session aaa = core.getAaaSession();
        try {
            aaa.beginTransaction();
            UserDAO userDAO = new UserDAO(core.getAaaDbName());
            User user = userDAO.queryByParam("login", modifiedUser.getLogin());
            if (user == null) {
                throw new RemoteException("User " + modifiedUser.getLogin() +
                                          " does not exist");
            }
            InstitutionDAO institutionDAO =
                new InstitutionDAO(core.getAaaDbName());
            Institution modifiedInst = institutionDAO.queryByParam(
                               "name", modifiedUser.getInstitution().getName());
            if (modifiedInst == null) {
                throw new RemoteException("Institution " +
                                       modifiedUser.getInstitution().getName() +
                                       " not found!");
            }
            user.setCertIssuer(modifiedUser.getCertIssuer());
            user.setCertSubject(modifiedUser.getCertSubject());
            user.setLastName(modifiedUser.getLastName());
            user.setFirstName(modifiedUser.getFirstName());
            user.setEmailPrimary(modifiedUser.getEmailPrimary());
            user.setPhonePrimary(modifiedUser.getPhonePrimary());
            if (setPassword) {
                user.setPassword(modifiedUser.getPassword());
            }
            user.setDescription(modifiedUser.getDescription());
            user.setEmailSecondary(modifiedUser.getEmailSecondary());
            user.setPhoneSecondary(modifiedUser.getPhoneSecondary());
            if (user.getInstitution().getName() !=
                  modifiedUser.getInstitution().getName()) {
                user.setInstitution(modifiedInst);
            }
            UserManager mgr = core.getUserManager();
            UserAttributeDAO userAttrDAO =
                new UserAttributeDAO(core.getAaaDbName());
            mgr.update(user, setPassword);
            for (String newRoleItem: newRoles) {
                this.log.info("new: " + newRoleItem);
                if (!curRoles.contains(newRoleItem)) {
                    this.log.info("adding user attribute");
                    this.addUserAttribute(newRoleItem, user);
                }
            }
            for (String curRoleItem: curRoles){
               if (!newRoles.contains(curRoleItem)) {
                    userAttrDAO.remove(user.getLogin(), curRoleItem);
                }
            }
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }
        this.log.debug("modifyUser.end");
        return result;
    }

    public HashMap<String, Object> delete(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("deleteUser.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        String username = (String) parameters.get("username");
        UserManager mgr = core.getUserManager();
        try {
            mgr.remove(username);
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        this.log.debug("deleteUser.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters)
            throws RemoteException {

        this.log.info("findUser.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        User user = null;
        String findBy = (String) parameters.get("findBy");
        Session aaa = core.getAaaSession();
        try {
            aaa.beginTransaction();
            if (findBy == null || findBy.equals("id")) {
                UserDAO userDAO = new UserDAO(core.getAaaDbName());
                Integer id = (Integer) parameters.get("id");
                if (id == null) {
                    aaa.getTransaction().rollback();
                    throw new RemoteException("Unknown id");
                }
                user = userDAO.findById(id, false);

            } else if (findBy.equals("username")) {
                String username = (String) parameters.get("username");
                if (username == null) {
                    aaa.getTransaction().rollback();
                    throw new RemoteException("Unknown id");
                }
                UserManager mgr = core.getUserManager();
                user = mgr.query(username);

            } else {
                aaa.getTransaction().rollback();
                throw new RemoteException("Unknown findBy");
            }
            if (user != null) {
                this.log.info("findUser.found:"+user.getLogin());
                Hibernate.initialize(user);
                Hibernate.initialize(user.getInstitution());
                Hibernate.initialize(user.getInstitution().getUsers());
                result.put("user", user);
            } else {
                throw new RemoteException("User not found");
            }
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            aaa.getTransaction().rollback();
            this.log.error(ex);
            throw new RemoteException(ex.getMessage());
        }
        this.log.info("findUser.end");
        return result;
    }

    private void addUserAttribute(String attrName, User user){

        UserAttributeDAO userAttrDAO = new UserAttributeDAO(core.getAaaDbName());
        AttributeDAO attrDAO = new AttributeDAO(core.getAaaDbName());
        UserAttribute userAttr = new UserAttribute();
        Attribute attr = attrDAO.queryByParam("name", attrName);
        userAttr.setAttribute(attr);
        userAttr.setUser(user);
        userAttrDAO.create(userAttr);
    }
}
