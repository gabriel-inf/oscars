package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;
import net.sf.json.*;

import org.apache.log4j.*;

import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.AAAException;


public class AuthenticateUser extends HttpServlet {

    // This is only called once, the first time this servlet is
    // called.
    public void init() throws ServletException {
        Logger log = Logger.getLogger(this.getClass());
        log.info("init.start");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("aaa");
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        log.info("init.end");
    }

    // This is only called once, when the server is brought down.
    public void destroy() {
        Logger log = Logger.getLogger(this.getClass());
        log.info("destroy.start");
        HibernateUtil.closeSessionFactory("aaa");
        HibernateUtil.closeSessionFactory("bss");
        log.info("destroy.end");
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        PrintWriter out = null;

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        UserManager mgr = new UserManager("aaa");
        Logger log = Logger.getLogger(this.getClass());

        out = response.getWriter();
        String userName = request.getParameter("userName");
        Random generator = new Random();
        int r = generator.nextInt();
        String sessionName = String.valueOf(r);
        response.setContentType("text/json-comment-filtered");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        try {
            String unused =
                mgr.verifyLogin(userName, request.getParameter("password"),
                                sessionName);
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        // Used to indicate if tabbed pages requiring authorization can
        // be displayed.  Note that some tabs may be displayed by default
        // before login that don't allow actual use.
        Map authorizedTabs = new HashMap();

        boolean allUsers= false;
        AuthValue authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS)  { 
            authorizedTabs.put("usersPane", Boolean.TRUE);
            //authorizedTabs.put("resourcesPane", Boolean.TRUE);
            //authorizedTabs.put("authorizationsPane", Boolean.TRUE);           
        }
        authVal = mgr.checkAccess(userName, "Users", "modify");
        if (authVal == AuthValue.ALLUSERS)  { 
            authorizedTabs.put("userAddPane", Boolean.TRUE);
        }
        userSession.setCookie("userName", userName, response);
        userSession.setCookie("sessionName", sessionName, response);
        Map outputMap = new HashMap();
        outputMap.put("method", "AuthenticateUser");
        outputMap.put("authorizedTabs", authorizedTabs);
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", userName + " signed in.  Use tabs " +
                    "to navigate to different pages.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}