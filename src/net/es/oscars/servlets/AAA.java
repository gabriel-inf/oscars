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
    private String dbname;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {


        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
        this.log.debug("Institutions:start");
        String methodName = "Institutions";
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            utils.handleFailure(out, "incorrect input from Institutions page",
                                methodName, null, null);
        }
        String opName = ops[1];
        this.log.info("op is " + opName);

        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        aaa.beginTransaction();     
        UserManager mgr = new UserManager(this.dbname);
        
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        if (authVal != AuthValue.ALLUSERS) {
            utils.handleFailure(out, "no permission to modify Institutions table",
                                methodName, aaa, null);
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Institutions management");
        if (opName.equals("list")) {
            this.outputInstitutions(outputMap);
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("Institutions:finish");
    }

    /**
     * outputInstitutions - gets the initial list of institutions.
     *  
     * @param outputMap Map containing JSON data
     */
    public void outputInstitutions(Map outputMap) {

        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        List<Institution> institutions = institutionDAO.list();
        ArrayList<HashMap<String,String>> institutionList =
            new ArrayList<HashMap<String,String>>();
        for (Institution institution: institutions) {
            HashMap<String,String> instMap = new HashMap<String,String>();
            instMap.put("institutionName", institution.getName());
            institutionList.add(instMap);
        }
        outputMap.put("items", institutionList);
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
