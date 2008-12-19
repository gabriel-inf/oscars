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
import net.es.oscars.aaa.Authorization;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.rmi.aaa.AaaRmiInterface;


public class AuthorizationList extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationList.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "AuthorizationList";
        this.log.debug("servlet.start");
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            AaaRmiInterface rmiClient = ServletUtils.getCoreRmiClient(methodName, log, out);
            AuthValue authVal = ServletUtils.getAuth(userName, "AAA", "list", rmiClient, methodName, log, out);

            if (authVal == AuthValue.DENIED) {
                String errorMsg = "User "+userName+" has no permission to list authorizations";
                this.log.error(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
            this.outputAuthorizations(outputMap, request, rmiClient, out);

        } catch (RemoteException ex) {
            return;
        }
        outputMap.put("status", "Authorization list");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.debug("servlet.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void outputAttributeMenu(Map<String, Object> outputMap,
                                    AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {

        String methodName = "AuthorizationList.outputAttributeMenu";
        List<Attribute> attributes = ServletUtils.getAllAttributes(rmiClient, out, log);
        List<String> attributeList = new ArrayList<String>();
        attributeList.add("Any");
        attributeList.add("true");
        for (Attribute attr: attributes) {
            attributeList.add(attr.getName());
            attributeList.add("false");
        }
        outputMap.put("attributeSelectMenu", attributeList);
    }

    /**
     * Sets the list of authorizations to display in a grid.
     *
     * @param outputMap Map containing JSON data
     * @param request HttpServletRequest form parameters
     * @throws AAAException
     */
    public void outputAuthorizations(Map<String, Object> outputMap,
                        HttpServletRequest request, AaaRmiInterface rmiClient,
                        PrintWriter out)
            throws RemoteException {

        String methodName = "AuthorizationList.outputAuthorizations";
        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        } else {
            attributeName = "";
        }
        String attrsUpdated = request.getParameter("authListAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        String listType = "";
        if (attributeName.equals("") || (attributeName.equals("Any"))) {
            listType = "ordered";
        } else {
            listType = "byAttrName";
        }
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("listType", listType);
        rmiParams.put("attributeName", attributeName);
        rmiParams.put("objectType", ModelObject.AUTHORIZATION);
        rmiParams.put("operation", ModelOperation.LIST);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
        List<Authorization> auths = (List<Authorization>) rmiResult.get("authorizations");
        if (auths == null) {
            auths = new ArrayList<Authorization>();
        }
        if (attributeName.equals("") ||
            ((attrsUpdated != null) && !attrsUpdated.equals(""))) {
            this.outputAttributeMenu(outputMap, rmiClient, out);
        }
        ArrayList<HashMap<String,String>> authList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (Authorization auth: auths) {
            HashMap<String,String> authMap = new HashMap<String,String>();
            authMap.put("id", Integer.toString(ctr));
            Attribute attr = auth.getAttribute();
            authMap.put("attribute", attr.getName());
            Resource resource = auth.getResource();
            authMap.put("resource", resource.getName());
            Permission perm = auth.getPermission();
            authMap.put("permission", perm.getName());
            String constraintName = auth.getConstraint().getName();
            authMap.put("constraint", constraintName);
            String constraintValue = auth.getConstraintValue();
            String constraintType = auth.getConstraint().getType();
            if (constraintValue == null) {
                if (constraintType.equals("boolean") &&
                    !constraintName.equals("none")) {
                    authMap.put("constraintVal", "true");
                } else {
                    authMap.put("constraintVal", "");
                }
            } else {
                authMap.put("constraintVal", constraintValue);
            }
            authList.add(authMap);
            ctr++;
        }
        outputMap.put("authData", authList);
    }
}
