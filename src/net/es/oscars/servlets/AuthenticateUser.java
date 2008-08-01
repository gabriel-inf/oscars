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
        log.info("servlet.start");

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
            log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        this.handleDisplay(mgr, userName, outputMap);
        userSession.setCookie("userName", userName, response);
        userSession.setCookie("sessionName", sessionName, response);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", userName + " signed in.  Use tabs " +
                    "to navigate to different pages.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        log.info("servlet.end: user " + userName + " logged in");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * Used to indicate which tabs can be displayed.  All except
     * Login/Logout require some form of authorization.  Also controls
     * what help information is displayed on login page.
     *
     * @param mgr UserManager instance that checks access
     * @param userName String with user's login name
     * @param outputMap map indicating what information to display based on
     *                  user authorization
     */
    public void handleDisplay(UserManager mgr, String userName,
                              Map outputMap) {

        Map authorizedTabs = new HashMap();
        // for special cases where user may be able to view grid but not
        // go to a different tab upon selecting a row
        Map selectableRows = new HashMap();
        AuthValue authVal = mgr.checkAccess(userName, "Reservations", "list");
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("reservationsPane", Boolean.TRUE);
            outputMap.put("reservationsDisplay", Boolean.TRUE);
        } else {
            outputMap.put("reservationsDisplay", Boolean.FALSE);
        }
        authVal = mgr.checkAccess(userName, "Reservations", "query");
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("reservationDetailsPane", Boolean.TRUE);
        }
        authVal = mgr.checkModResAccess(userName, "Reservations",
                "create", 0, 0, false, false);
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("reservationCreatePane", Boolean.TRUE);
            outputMap.put("createReservationDisplay", Boolean.TRUE);
        } else {
            outputMap.put("createReservationDisplay", Boolean.FALSE);
        }
        AuthValue authQueryVal = mgr.checkAccess(userName, "Users", "query");
        if (authQueryVal != AuthValue.DENIED)  { 
            authorizedTabs.put("userProfilePane", Boolean.TRUE);
        }
        authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS)  { 
            authorizedTabs.put("userListPane", Boolean.TRUE);
            if (authQueryVal == AuthValue.ALLUSERS) {
                selectableRows.put("users", Boolean.TRUE);
                outputMap.put("authUsersDisplay", Boolean.TRUE);
                outputMap.put("unAuthUsersDisplay", Boolean.FALSE);
                outputMap.put("userProfileDisplay", Boolean.FALSE);
            } else if (authQueryVal == AuthValue.SELFONLY) {
                outputMap.put("userProfileDisplay", Boolean.TRUE);
                outputMap.put("unAuthUsersDisplay", Boolean.TRUE);
                outputMap.put("authUsersDisplay", Boolean.FALSE);
            }
        } else if (authQueryVal != AuthValue.DENIED) {
            outputMap.put("userProfileDisplay", Boolean.TRUE);
            outputMap.put("authUsersDisplay", Boolean.FALSE);
            outputMap.put("unAuthUsersDisplay", Boolean.FALSE);
        }
        authVal = mgr.checkAccess(userName, "Users", "create");
        if (authVal == AuthValue.ALLUSERS)  { 
            authorizedTabs.put("userAddPane", Boolean.TRUE);
            outputMap.put("addUserDisplay", Boolean.TRUE);
        } else {
            outputMap.put("addUserDisplay", Boolean.FALSE);
        }
        authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal != AuthValue.DENIED)  { 
            authorizedTabs.put("institutionsPane", Boolean.TRUE);
            authorizedTabs.put("authorizationsPane", Boolean.TRUE);
            authorizedTabs.put("authDetailsPane", Boolean.TRUE);
        }
        outputMap.put("authorizedTabs", authorizedTabs);
        outputMap.put("selectableRows", selectableRows);
    }
}
