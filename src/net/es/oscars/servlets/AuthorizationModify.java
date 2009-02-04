package net.es.oscars.servlets;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;

public class AuthorizationModify extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationModify.class);

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        this.log = Logger.getLogger(this.getClass());
        log.debug("servlet.start");

        String methodName = "AuthorizationModify";
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.warn("No user session: cookies invalid");
            return;
        }

        String attributeName = request.getParameter("authAttributeName");
        String permissionName  = request.getParameter("permissionName");
        String resourceName  = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");
        String constraintValue = request.getParameter("constraintValue");
        String oldAttributeName = request.getParameter("oldAuthAttributeName");
        String oldPermissionName = request.getParameter("oldPermissionName");
        String oldResourceName  = request.getParameter("oldResourceName");
        String oldConstraintName  = request.getParameter("oldConstraintName");
        log.debug("modifying attribute: " + oldAttributeName + " to "+ attributeName +
                " resource: " + oldResourceName  + " to " + resourceName  +
                " permission: " + oldPermissionName  + " to " + permissionName  +
                " constraintName: " + oldConstraintName + " to " + constraintName );

        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("oldAttributeName", oldAttributeName);
        rmiParams.put("oldPermissionName", oldPermissionName);
        rmiParams.put("oldResourceName", oldResourceName);
        rmiParams.put("oldConstraintName", oldConstraintName);
        rmiParams.put("attributeName", attributeName);
        rmiParams.put("permissionName", permissionName);
        rmiParams.put("resourceName", resourceName);
        rmiParams.put("constraintName", constraintName);
        rmiParams.put("constraintValue", constraintValue);

        rmiParams.put("objectType", ModelObject.AUTHORIZATION);
        rmiParams.put("operation", ModelOperation.MODIFY);

        try {
            HashMap<String, Object> rmiResult = new HashMap<String, Object>();

            AaaRmiInterface rmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            AuthValue authVal =
                rmiClient.checkAccess(userName, "AAA", "list");
            if (authVal == AuthValue.DENIED)  {
                log.warn("Not allowed to modify an authorization");
                ServletUtils.handleFailure(out, "not allowed to modify an authorization", methodName);
                return;
            }
            rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "Authorization modified");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.debug("servlet.end");
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }


}
