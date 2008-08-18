package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.AuthValue;


public class Institutions extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {


        this.log = Logger.getLogger(this.getClass());
        String methodName = "Institutions";
        this.log.debug("servlet.start");
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            this.log.error("Incorrect input from Institutions page");
            Utils.handleFailure(out, "incorrect input from Institutions page",
                                methodName, null);
            return;
        }
        String opName = ops[1];

        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();     
        UserManager mgr = new UserManager(Utils.getDbName());
        
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal == AuthValue.DENIED) {
            this.log.error("No permission to modify Institutions table.");
            Utils.handleFailure(out, "no permission to modify Institutions table",
                                methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        String saveName = request.getParameter("saveName");
        if (saveName != null) {
            saveName = saveName.trim();
        }
        String institutionEditName = request.getParameter("institutionEditName").trim();
        try {
            if (opName.equals("add")) {
                methodName = "InstitutionAdd";
                this.addInstitution(institutionEditName);
                outputMap.put("status", "Added institution: " +
                                         institutionEditName);
            } else if (opName.equals("modify")) {
                methodName = "InstitutionModify";
                this.modifyInstitution(saveName, institutionEditName);
                outputMap.put("status", "Changed institution name from " +
                                       saveName + " to " + institutionEditName);
            } else if (opName.equals("delete")) {
                methodName = "InstitutionDelete";
                this.deleteInstitution(institutionEditName);
                outputMap.put("status", "Deleted institution: " +
                                         institutionEditName);
            } else {
                methodName = "InstitutionList";
                outputMap.put("status", "Institutions management");
            }
        } catch (AAAException e) {
            this.log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        // always output latest list
        this.outputInstitutions(outputMap);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("servlet.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * outputInstitutions - gets the initial list of institutions.
     *  
     * @param outputMap Map containing JSON data
     */
    public void outputInstitutions(Map outputMap) {

        InstitutionDAO institutionDAO = new InstitutionDAO(Utils.getDbName());
        List<Institution> institutions = institutionDAO.list();
        ArrayList institutionList = new ArrayList();
        for (Institution institution: institutions) {
            ArrayList institutionEntry = new ArrayList();
            institutionEntry.add(institution.getName());
            institutionList.add(institutionEntry);
        }
        outputMap.put("institutionData", institutionList);
    }

    /**
     * addInstitution - add an institution if it doesn't already exist.
     *  
     * @param String newName name of new institution
     * @throws AAAException
     */
    public void addInstitution(String newName)
           throws AAAException {

        InstitutionDAO dao = new InstitutionDAO(Utils.getDbName());
        Institution oldInstitution = dao.queryByParam("name", newName);
        if (oldInstitution != null) {
            throw new AAAException("Institution " + newName +
                                   " already exists");
        }
        Institution institution = new Institution();
        institution.setName(newName);
        dao.create(institution);
    }

    /**
     * modifyInstitution - change an institution's name.
     *  
     * @param String oldName old name of institution
     * @param String newName new name of institution
     * @throws AAAException
     */
    public void modifyInstitution(String oldName, String newName)
           throws AAAException {

        InstitutionDAO dao = new InstitutionDAO(Utils.getDbName());
        Institution institution = dao.queryByParam("name", oldName);
        if (institution == null) {
            throw new AAAException("Institution " + oldName +
                                   " does not exist to be modified");
        }
        institution.setName(newName);
        dao.update(institution);
    }

    /**
     * deleteInstitution - delete an institution, but only if no users
     *     currently belong to it
     *  
     * @param String institutionName name of institution to delete
     * @throws AAAException
     */
    public void deleteInstitution(String institutionName)
           throws AAAException {

        UserManager mgr = new UserManager(Utils.getDbName());
        InstitutionDAO dao = new InstitutionDAO(Utils.getDbName());
        Institution institution = dao.queryByParam("name", institutionName);
        if (institution == null) {
            throw new AAAException("Institution " + institutionName +
                                   " does not exist to be deleted");
        }
        Set<User> users = institution.getUsers();
        StringBuilder sb = new StringBuilder();
        if (users.size() != 0) {
            sb.append(institutionName + " has existing users: ");
            Iterator iter = users.iterator();
            while (iter.hasNext()) {
                User user = (User) iter.next();
                sb.append(user.getLogin() + " ");
            }
            throw new AAAException(sb.toString());
        }
        dao.remove(institution);
    }
}
