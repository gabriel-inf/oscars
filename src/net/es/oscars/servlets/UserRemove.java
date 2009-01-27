package net.es.oscars.servlets;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;
import org.apache.log4j.Logger;

import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.aaa.AuthValue;


public class UserRemove extends HttpServlet {
    private Logger log = Logger.getLogger(UserRemove.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        log.info("UserRemove.start");
        String methodName = "UserRemove";
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) { return; }

        String profileName = request.getParameter("profileName");

        // cannot remove oneself
        if (profileName == userName) {
            log.error("User "+userName+" not allowed to remove himself");
            ServletUtils.handleFailure(out, "You may not remove your own account.", methodName);
            return;
        }

        try {
            AaaRmiInterface rmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            AuthValue authVal =
                rmiClient.checkAccess(userName, "Users", "modify");
            if (authVal != AuthValue.ALLUSERS) {
                log.error("no permission to modify users");
                ServletUtils.handleFailure(out,"You do not have the permissions to modify users", methodName);
                return;
            }

            HashMap<String, Object> rmiParams = new HashMap<String, Object>();
            rmiParams.put("username", profileName);
            rmiParams.put("objectType", ModelObject.USER);
            rmiParams.put("operation", ModelOperation.DELETE);

            HashMap<String, Object> rmiResult = new HashMap<String, Object>();
            rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
            authVal = rmiClient.checkAccess(userName, "Users", "list");

            // shouldn't be able to get to this point, but just in case
            if (!(authVal == AuthValue.ALLUSERS)) {
                log.error("no permission to list users");
                ServletUtils.handleFailure(out, "You do not have the permissions to list users", methodName);
                return;
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, null, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "User " + profileName + " successfully removed");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);

        log.info("UserRemove.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
