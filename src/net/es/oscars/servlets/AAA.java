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


public class AAA extends HttpServlet {
    private Logger log;
    private String dbname;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {


        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
        this.log.debug("AAA:start");
        String methodName = "AAA";
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        String[] ops = request.getQueryString().split("&");
        if (ops.length != 2) {
            utils.handleFailure(out, "incorrect input from AAA page",
                                methodName, null, null);
        }
        String[] tableParams = ops[0].split("=");
        if (tableParams.length != 2) {
            utils.handleFailure(out, "incorrect input from AAA page",
                                methodName, null, null);
        }
        String tableName = tableParams[1];
        this.log.info("table is " + tableName);
        String[] opParams = ops[1].split("=");
        if (opParams.length != 2) {
            utils.handleFailure(out, "incorrect input from AAA page",
                                methodName, null, null);
        }
        String opName = opParams[1];
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
            utils.handleFailure(out, "no permission to modify AAA tables",
                                methodName, aaa, null);
        }
        Map outputMap = new HashMap();
        if (tableName.equals("institution")) {
            if (opName.equals("list")) {
                outputMap.put("status", "Institution list");
                this.outputInstitutions(outputMap);
            }
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("AAA:finish");
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
