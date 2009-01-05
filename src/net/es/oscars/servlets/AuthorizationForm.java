package net.es.oscars.servlets;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.Resource;
import net.es.oscars.aaa.Permission;
import net.es.oscars.aaa.Constraint;
import net.es.oscars.aaa.Rpc;
import net.es.oscars.aaa.Attribute;

import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.*;


/**
 * Class that sets menu options in attributes, resources, permissions, and
 * constraints menus.  May be called more than once, if attributes
 * modified.  Initially called on user login.
 */
public class AuthorizationForm extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationForm.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        this.log = Logger.getLogger(this.getClass());
        String methodName = "AuthorizationForm";
        log.debug("servlet.start");
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.error("No user session: cookies invalid");
            return;
        }
        String attrsUpdated = request.getParameter("authAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            AaaRmiInterface rmiClient = ServletUtils.getCoreRmiClient(methodName, log, out);
            AuthValue authVal = ServletUtils.getAuth(userName, "AAA", "modify", rmiClient, methodName, log, out);

            if (authVal == null || authVal == AuthValue.DENIED) {
                log.error("Not authorized to perform admin operations");
                ServletUtils.handleFailure(out, "not authorized to perform admin operations", methodName);
                return;
            }
            this.outputAttributeMenu(outputMap, rmiClient, out);
            String rpcParam = request.getParameter("rpc");

            // Make sure to update these exactly once.
            // rpc being unset makes sure they get updated in the beginning.
            // If just the attributes have been updated, don't redisplay.
            if (((rpcParam == null) || (rpcParam.trim().equals("")) ||
                    attrsUpdated.equals(""))) {
                this.outputResourceMenu(outputMap, rmiClient, out);
                this.outputPermissionMenu(outputMap, rmiClient, out);
                this.outputConstraintMenu(outputMap, rmiClient, out);
            }
            if ((rpcParam == null) || rpcParam.trim().equals("")) {
                this.outputRpcs(outputMap, rmiClient, out);
            }
        } catch (RemoteException ex) {
            this.log.error(ex.getMessage());
            return;
        }
        outputMap.put("method", methodName);
        // this form does not reset status
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.debug("servlet.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void outputAttributeMenu(Map<String, Object> outputMap,
            AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {

        String methodName = "AuthorizationForm.outputAttributeMenu";
        List<Attribute> attributes = ServletUtils.getAllAttributes(rmiClient, out, log);
        List<String> attributeList = new ArrayList<String>();
        int ctr = 0;
        for (Attribute attribute: attributes) {
            attributeList.add(attribute.getName());
            if (ctr == 0) {
                attributeList.add("true");
            } else {
                attributeList.add("false");
            }
            ctr++;
        }
        outputMap.put("authAttributeNameMenu", attributeList);
    }

    public void outputResourceMenu(Map<String, Object> outputMap,
            AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {

        String methodName = "AuthorizationForm.outputResourceMenu";
        List<Resource> resources = ServletUtils.getAllResources(rmiClient, out, log);
        List<String> resourceList = new ArrayList<String>();
        int ctr = 0;
        for (Resource resource: resources) {
            resourceList.add(resource.getName());
            if (ctr == 0) {
                resourceList.add("true");
            } else {
                resourceList.add("false");
            }
            ctr++;
        }
        outputMap.put("resourceNameMenu", resourceList);
    }

    public void outputPermissionMenu(Map<String, Object> outputMap,
            AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {

        String methodName = "AuthorizationForm.outputPermissionMenu";
        List<Permission> permissions = ServletUtils.getAllPermissions(rmiClient, out, log);
        List<String> permissionList = new ArrayList<String>();
        int ctr = 0;
        for (Permission permission: permissions) {
            permissionList.add(permission.getName());
            if (ctr == 0) {
                permissionList.add("true");
            } else {
                permissionList.add("false");
            }
            ctr++;
        }
        outputMap.put("permissionNameMenu", permissionList);
    }

    public void
        outputConstraintMenu(Map<String, Object> outputMap,
                AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException  {

        String methodName = "AuthorizationForm.outputConstraintMenu";
        List<Constraint> constraints = ServletUtils.getAllConstraints(rmiClient, out, log);
        List<String> constraintList = new ArrayList<String>();
        int ctr = 0;
        for (Constraint constraint: constraints) {
            constraintList.add(constraint.getName());
            if (ctr == 0) {
                constraintList.add("true");
            } else {
                constraintList.add("false");
            }
            ctr++;
        }
        outputMap.put("constraintNameMenu", constraintList);
    }

    /**
     * Outputs permitted resource/permission/constraint triplets, along
     * with constraint type.   Could
     * eventually be used in a split container on the right side of the
     * authorization details page, assuming the grid would display in
     * such a case.
     *
     * @param outputMap Map containing JSON data
     */
    public void outputRpcs(Map<String, Object> outputMap,
            AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {

        String methodName = "AuthorizationForm.outputRpcs";
        List<Rpc> rpcs = ServletUtils.getAllRpcs(rmiClient, out, log);
        ArrayList<ArrayList<String>> rpcList = new ArrayList<ArrayList<String>>();
        for (Rpc rpc: rpcs) {
            ArrayList<String> rpcEntry = new ArrayList<String>();
            if (rpc.getResource() != null) {
                rpcEntry.add(rpc.getResource().getName());
            } else {
                this.log("couldn't find resource: " +
                        rpc.getResource().getName());
                continue;
            }
            if (rpc.getPermission() != null) {
                rpcEntry.add(rpc.getPermission().getName());
            } else {
                this.log("couldn't find permission: " +
                        rpc.getPermission().getName());
                continue;
            }
            if (rpc.getConstraint() != null) {
                rpcEntry.add(rpc.getConstraint().getName());
                rpcEntry.add(rpc.getConstraint().getType());
            } else {
                this.log("couldn't find constraint: " +
                        rpc.getConstraint().getName());
                continue;
            }
            rpcList.add(rpcEntry);
        }
        outputMap.put("rpcData", rpcList);
    }
}
