package net.es.oscars.servlets;

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
        dbnames.add(Utils.getDbName());
        initializer.initDatabase(dbnames);
        log.info("init.end");
    }

    // This is only called once, when the server is brought down.
    public void destroy() {
        Logger log = Logger.getLogger(this.getClass());
        log.info("destroy.start");
        HibernateUtil.closeSessionFactory(Utils.getDbName());
        log.info("destroy.end");
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        PrintWriter out = null;

        String methodName = "AuthenticateUser";
        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        Logger log = Logger.getLogger(this.getClass());

        out = response.getWriter();
        String userName = request.getParameter("userName");
        String guestLogin = userSession.getGuestLogin();
        String sessionName = "";
        if (userName != null && guestLogin != null && 
            userName.equals(guestLogin)) {
            sessionName = "1234567890";
        } else {
            Random generator = new Random();
            int r = generator.nextInt();
            sessionName = String.valueOf(r);
        }
        response.setContentType("text/json-comment-filtered");
        Session aaa = 
            HibernateUtil.getSessionFactory(
                    Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        try {
            String unused =
                mgr.verifyLogin(userName,
                                request.getParameter("initialPassword"),
                                sessionName);
        } catch (AAAException e) {
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        // Used to indicate which tabs can be displayed.  All except
        // Login/Logout require some form of authorization.
        Map authorizedTabs = new HashMap();
        // for special cases where user may be able to view grid but not
        // go to a different tab upon selecting a row
        Map selectableRows = new HashMap();
        AuthValue authVal = mgr.checkAccess(userName, "Reservations", "list");
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("reservationsPane", Boolean.TRUE);
        }
        authVal = mgr.checkAccess(userName, "Reservations", "query");
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("reservationDetailsPane", Boolean.TRUE);
        }
        authVal = mgr.checkModResAccess(userName, "Reservations",
                "create", 0, 0, false, false);
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("reservationCreatePane", Boolean.TRUE);
        }
        authVal = mgr.checkAccess(userName, "Users", "query");
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("userProfilePane", Boolean.TRUE);
        }
        if (authVal == AuthValue.ALLUSERS) {
            selectableRows.put("users", Boolean.TRUE);
        }
        authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS)  { 
            authorizedTabs.put("userListPane", Boolean.TRUE);
        }
        authVal = mgr.checkAccess(userName, "Users", "create");
        if (authVal == AuthValue.ALLUSERS)  { 
            authorizedTabs.put("userAddPane", Boolean.TRUE);
        }
        authVal = mgr.checkAccess(userName, "AAA", "modify");
        // ?
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("institutionsPane", Boolean.TRUE);
        }
        userSession.setCookie("userName", userName, response);
        userSession.setCookie("sessionName", sessionName, response);
        Map outputMap = new HashMap();
        outputMap.put("method", methodName);
        outputMap.put("authorizedTabs", authorizedTabs);
        outputMap.put("selectableRows", selectableRows);
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
