package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserAddForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager("aaa");
        Utils utils = new Utils();
        List<Institution> institutions = null;
        Logger log = Logger.getLogger(this.getClass());
        log.debug("UserAddForm: start");

        String methodName = "UserAddForm";
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        if (authVal != AuthValue.ALLUSERS) {
            utils.handleFailure(out, "not allowed to add a new user",
                                methodName, aaa, null);
            return;
        }
        institutions = mgr.getInstitutions();

        Map outputMap = new HashMap();
        outputMap.put("status", "Add a user");
        this.outputInstitutionMenu(outputMap, institutions);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        log.debug("UserAddForm: finish");      
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void
        outputInstitutionMenu(Map outputMap, List<Institution> insts) {

        String institutionName = "";
        StringBuffer sb = new StringBuffer();

        sb.append("<select class='required' name='institutionName'>");
        // use default
        institutionName = "Energy Sciences Network";
        for (Institution i: insts) {
            sb.append("<option value='" + i.getName() + "' ");
            if (i.getName().equals(institutionName)) {
                sb.append("selected='selected'" );
            }
            sb.append(">" + i.getName() + "</option>" );
        }
        sb.append("</select>");
        outputMap.put("newInstitutionMenuReplace", sb.toString());
    }
}
