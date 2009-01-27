package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.*;


public class AuthorizationAdd extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationAdd.class);
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("servlet.start");

        String methodName = "AuthorizationAdd";
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }

        String attributeName = request.getParameter("authAttributeName");
        String permissionName  = request.getParameter("permissionName");
        String resourceName  = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");
        String constraintValue = null;
        if (constraintName != null) {
            constraintValue = request.getParameter("constraintValue");
        }
        this.log.debug("Adding attribute: " + attributeName  +" resource: " + resourceName  + " permission: "
                + permissionName  + " constraintName: " + constraintName + " constraintValue: " + constraintValue);

        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("attributeName", attributeName);
        rmiParams.put("permissionName", permissionName);
        rmiParams.put("resourceName", resourceName);
        rmiParams.put("constraintName", constraintName);
        rmiParams.put("constraintValue", constraintValue);
        rmiParams.put("objectType", ModelObject.AUTHORIZATION);
        rmiParams.put("operation", ModelOperation.ADD);

        try {
            AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
            AuthValue authVal =
                rmiClient.checkAccess(userName, "AAA", "modify");
            if (authVal == AuthValue.DENIED)  {
                String errorMsg = "User "+userName+" is not allowed to add an authorization";
                this.log.error(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
            HashMap<String, Object> rmiResult = new HashMap<String, Object>();

            rmiResult = ServletUtils.manageAaaObject(rmiClient, "addAuthorization", log, out, rmiParams);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, null, e, methodName);
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "Added authorization");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.debug("servlet.end");
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
