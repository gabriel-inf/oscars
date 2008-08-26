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


public class UserAddForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        List<Institution> institutions = null;
        Logger log = Logger.getLogger(this.getClass());
        log.debug("servlet.start");

        String methodName = "UserAddForm";
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.error("No user session: cookies invalid");
            return;
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        if (authVal != AuthValue.ALLUSERS) {
            log.error("Not allowed to add a new user");
            Utils.handleFailure(out, "not allowed to add a new user",
                                methodName, aaa);
            return;
        }
        institutions = mgr.getInstitutions();

        Map outputMap = new HashMap();
        outputMap.put("status", "Add a user");
        this.outputAttributeMenu(outputMap);
        this.outputInstitutionMenu(outputMap, institutions);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        log.debug("servlet.end");      
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void
        outputAttributeMenu(Map outputMap) {

        AttributeDAO dao = new AttributeDAO(Utils.getDbName());
        List<Attribute> attributes = dao.list();
        List<String> attributeList = new ArrayList<String>();
        // default is none 
        attributeList.add("None");
        attributeList.add("true");
        for (Attribute a: attributes) {
            attributeList.add(a.getName() + " -> " + a.getDescription());
            attributeList.add("false");
        }
        outputMap.put("newAttributeNameMenu", attributeList);
    }

    public void
        outputInstitutionMenu(Map outputMap, List<Institution> insts) {

        List<String> institutionList = new ArrayList<String>();
        int ctr = 0;
        // default is first in list
        for (Institution i: insts) {
            institutionList.add(i.getName());
            if (ctr == 0) {
                institutionList.add("true");
            } else {
                institutionList.add("false");
            }
            ctr++;
        }
        outputMap.put("newInstitutionMenu", institutionList);
    }
}
