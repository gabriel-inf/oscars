package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.model.*;

public class InstitutionModelRmiHandler extends ModelRmiHandlerImpl {
    private AAACore core = AAACore.getInstance();
    private Logger log;


    public InstitutionModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("listInstitutions.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        String listType = (String) parameters.get("type");

        if (listType == null) {
            listType = "plain";
        }

        InstitutionDAO institutionDAO = new InstitutionDAO(core.getAaaDbName());
        List<Institution> institutions = new ArrayList<Institution>();
        if (listType.equals("plain")) {
            institutions = institutionDAO.list();
        }
        for (Institution institution : institutions) {
            Hibernate.initialize(institution);
            Hibernate.initialize(institution.getUsers());
        }
        aaa.getTransaction().commit();

        result.put("institutions", institutions);

        this.log.debug("listInstitutions.end");
        return result;
    }



    public HashMap<String, Object> add(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("addInstitution.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        InstitutionDAO institutionDAO = new InstitutionDAO(core.getAaaDbName());
        String institutionName = (String) parameters.get("institutionName");

        Institution oldInstitution = institutionDAO.queryByParam("name", institutionName);
        if (oldInstitution != null) {
            aaa.getTransaction().rollback();

            this.log.debug("addInstitution.end");
            return result;
        }

        Institution institution = new Institution();
        institution.setName(institutionName);
        institutionDAO.create(institution);

        aaa.getTransaction().commit();

        this.log.debug("addInstitution.end");
        return result;
    }



    public HashMap<String, Object> modify(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("modifyInstitution.start");
        Session aaa = core.getAaaSession();

        HashMap<String, Object> result = new HashMap<String, Object>();
        aaa.beginTransaction();


        String newName = (String) parameters.get("newName");
        String oldName = (String) parameters.get("oldName");

        InstitutionDAO institutionDAO = new InstitutionDAO(core.getAaaDbName());
        Institution institution = institutionDAO.queryByParam("name", oldName);

        if (institution == null) {
            aaa.getTransaction().rollback();
            institution.setUsers(null);

            throw new RemoteException("Institution not found");
        }

        institution.setName(newName);
        institutionDAO.update(institution);

        aaa.getTransaction().commit();

        this.log.debug("modifyInstitution.end");
        return result;
    }



    public HashMap<String, Object> delete(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("deleteInstitution.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        String institutionName = (String) parameters.get("institutionName");
        if (institutionName == null) {
            aaa.getTransaction().rollback();

            throw new RemoteException("No institution name");
        }


        InstitutionDAO institutionDAO = new InstitutionDAO(core.getAaaDbName());

        Institution institution = institutionDAO.queryByParam("name", institutionName);
        if (institution == null) {
            aaa.getTransaction().rollback();

            throw new RemoteException("Institution " + institutionName +" does not exist to be deleted");
        }

        Set<User> users = (Set<User>) institution.getUsers();
        StringBuilder sb = new StringBuilder();
        if (users.size() != 0) {
            sb.append(institutionName + " has existing users: ");
            Iterator<User> iter = users.iterator();
            while (iter.hasNext()) {
                User user = (User) iter.next();
                sb.append(user.getLogin() + " ");
            }
            aaa.getTransaction().rollback();

            throw new RemoteException(sb.toString());
        }
        institutionDAO.remove(institution);


        aaa.getTransaction().commit();

        this.log.debug("deleteInstitution.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("findInstitution.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        InstitutionDAO institutionDAO = new InstitutionDAO(core.getAaaDbName());
        Institution institution = institutionDAO.findById(id, false);
        Hibernate.initialize(institution);
        Hibernate.initialize(institution.getUsers());
        aaa.getTransaction().commit();


        result.put("institution", institution);
        this.log.debug("findInstitution.end");
        return result;
    }



}
