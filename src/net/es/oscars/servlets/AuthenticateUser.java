package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;

import org.apache.log4j.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;
import net.es.oscars.aaa.Resource;
import net.es.oscars.aaa.Permission;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;

public class AuthenticateUser extends HttpServlet {
    private Logger log = Logger.getLogger(AuthenticateUser.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        log.debug("AuthenticateUser.start");
        String methodName = "AuthenticateUser";

        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        UserSession userSession = new UserSession();
        String userName;
        String sesUserName = userSession.checkSession(null, request, methodName);
        if (sesUserName != null) {
            userName = sesUserName;
        } else {
            userName = request.getParameter("userName");
        }
        String password = request.getParameter("initialPassword");
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
        try {
            AaaRmiInterface rmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            String loginUserName =
                rmiClient.verifyLogin(userName, password, sessionName);
            userName = (String) loginUserName;
            if (userName == null) {
                ServletUtils.handleFailure(out, "Login not allowed", methodName);
                return;
            }
            this.handleDisplay(rmiClient, userName, outputMap, out);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, null, e, methodName);
            return;
        }
        log.info("setting cookie name to " + userName);
        userSession.setCookie("userName", userName, response);
        log.info("setting session name to " + sessionName);
        userSession.setCookie("sessionName", sessionName, response);

        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", userName + " signed in.  Use tabs " + "to navigate to different pages.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&& " + jsonObject);
        log.info("AuthenticateUser: user " + userName + " logged in");
        log.debug("AuthenticateUser.end");

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
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
    public void handleDisplay(AaaRmiInterface rmiClient, String userName, Map<String, Object> outputMap, PrintWriter out) throws RemoteException {
        String methodName = "handleDisplay";

        Map<String, Object> authorizedTabs = new HashMap<String, Object>();


        // for special cases where user may be able to view grid but not
        // go to a different tab upon selecting a row
        Map<String, Object> selectableRows = new HashMap<String, Object>();

        HashMap<String, ArrayList<String>> permsToCheck = new HashMap<String, ArrayList<String>>();

        ArrayList<String> lqcPerms = new ArrayList<String>();
        lqcPerms.add("list");
        lqcPerms.add("query");
        lqcPerms.add("create");

        ArrayList<String> modPerms = new ArrayList<String>();
        modPerms.add("modify");
        permsToCheck.put("Reservations", lqcPerms);
        permsToCheck.put("Users", lqcPerms);
        permsToCheck.put("AAA", modPerms);

        AuthMultiValue resourcePerms = rmiClient.checkMultiAccess(userName, permsToCheck);
        AuthValue createResAuth = rmiClient.checkModResAccess(userName, "Reservations", "create", 0, 0, false, false);

        this.log.info("Resources:");
        Iterator<String> resIt = resourcePerms.keySet().iterator();
        while (resIt.hasNext()) {
            String res = resIt.next();
            this.log.info(res);
        }


        AuthValue authVal = resourcePerms.get("Reservations").get("list");
        if (authVal == null) {
            authVal = AuthValue.DENIED;
        }
        if (authVal != AuthValue.DENIED)  {
            authorizedTabs.put("reservationsPane", Boolean.TRUE);
            outputMap.put("reservationsDisplay", Boolean.TRUE);
        } else {
            outputMap.put("reservationsDisplay", Boolean.FALSE);
        }


        authVal = resourcePerms.get("Reservations").get("query");
        if (authVal == null) {
            authVal = AuthValue.DENIED;
        }
        this.log.info("Reservations:query:"+authVal.toString());

        if (authVal != AuthValue.DENIED)  {
            authorizedTabs.put("reservationDetailsPane", Boolean.TRUE);
        }

        if (createResAuth  == null) {
            createResAuth = AuthValue.DENIED;
        }
        if (createResAuth != AuthValue.DENIED)  {
            authorizedTabs.put("reservationCreatePane", Boolean.TRUE);
            outputMap.put("createReservationDisplay", Boolean.TRUE);
        } else {
            outputMap.put("createReservationDisplay", Boolean.FALSE);
        }



        AuthValue authQueryVal = resourcePerms.get("Users").get("query");
        if (authQueryVal == null) {
            authQueryVal = AuthValue.DENIED;
        }
        this.log.info("Users:query:"+authVal.toString());

        if (authQueryVal != AuthValue.DENIED)  {
            authorizedTabs.put("userProfilePane", Boolean.TRUE);
        }

        authVal = resourcePerms.get("Users").get("list");
        if (authVal == null) {
            authVal = AuthValue.DENIED;
        }
        this.log.info("Users:list:"+authVal.toString());

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

        authVal = resourcePerms.get("Users").get("create");
        this.log.info("Users:create:"+authVal.toString());
        if (authVal == null) {
            authVal = AuthValue.DENIED;
        }
        if (authVal != AuthValue.DENIED)  {
            authorizedTabs.put("userAddPane", Boolean.TRUE);
            outputMap.put("addUserDisplay", Boolean.TRUE);
        } else {
            outputMap.put("addUserDisplay", Boolean.FALSE);
        }

        authVal = (AuthValue) resourcePerms.get("AAA").get("modify");
        this.log.info("AAA:modify:"+authVal.toString());
        if (authVal == null) {
            authVal = AuthValue.DENIED;
        }
        if (authVal != AuthValue.DENIED)  {
            authorizedTabs.put("institutionsPane", Boolean.TRUE);
            authorizedTabs.put("attributesPane", Boolean.TRUE);
            authorizedTabs.put("authorizationsPane", Boolean.TRUE);
            authorizedTabs.put("authDetailsPane", Boolean.TRUE);
        }
        outputMap.put("authorizedTabs", authorizedTabs);
        outputMap.put("selectableRows", selectableRows);
    }
}
