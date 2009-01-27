package net.es.oscars.servlets;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;

public class Attributes extends HttpServlet {
    private Logger log = Logger.getLogger(Attributes.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.log.debug("Attributes.start");

        String methodName = "Attributes";
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();

        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            this.log.error("Incorrect input from Attributes page");
            ServletUtils.handleFailure(out, "incorrect input from Attributes page", methodName);
            return;
        }
        String opName = ops[1];
        response.setContentType("application/json");

        Map<String, Object> outputMap = new HashMap<String, Object>();
        String saveAttrName = request.getParameter("saveAttrName");
        if (saveAttrName != null) {
            saveAttrName = saveAttrName.trim();
        }
        String attributeEditName = request.getParameter("attributeEditName").trim();
        String attributeEditDescr = request.getParameter("attributeEditDescription").trim();
        String attributeEditType = request.getParameter("attributeTypes").trim();
        try {
            AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
            AuthValue authVal = rmiClient.checkAccess(userName, "AAA", "modify");
            if (authVal != null && authVal == AuthValue.DENIED) {
                String errorMsg = "User "+userName+" does not have permission to modify attributes.";
                this.log.error(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }
            if (opName.equals("add")) {
                methodName = "AttributeAdd";
                this.addAttribute(attributeEditName, attributeEditDescr, attributeEditType, rmiClient, out);
                outputMap.put("status", "Added attribute: " + attributeEditName);
            } else if (opName.equals("modify")) {
                methodName = "AttributeModify";
                this.modifyAttribute(saveAttrName, attributeEditName, attributeEditDescr, attributeEditType, rmiClient, out);
                if (!saveAttrName.equals(attributeEditName)) {
                    outputMap.put("status", "Changed attribute name from " + saveAttrName + " to " + attributeEditName);
                } else {
                    outputMap.put("status", "Modified attribute " + saveAttrName);
                }
            } else if (opName.equals("delete")) {
                methodName = "AttributeDelete";
                this.deleteAttribute(attributeEditName,  rmiClient, out);
                outputMap.put("status", "Deleted attribute: " + attributeEditName);
            } else {
                methodName = "AttributeList";
                outputMap.put("status", "Attributes management");
            }
            this.outputAttributes(outputMap, rmiClient, out);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, null, e, methodName);
            return;
        }
        // always output latest list
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.debug("Attributes.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * outputAttributes - gets the initial list of attributes.
     *
     * @param outputMap Map containing JSON data
     */
    public void outputAttributes(Map<String, Object> outputMap,
                                 AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {

        String methodName = "Attributes.outputAttributes";
        List<Attribute> attributes = ServletUtils.getAllAttributes(rmiClient, out, log);
        ArrayList<HashMap<String,String>> attributeList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (Attribute attribute: attributes) {
            HashMap<String,String> attributeMap = new HashMap<String,String>();
            attributeMap.put("id", Integer.toString(ctr));
            attributeMap.put("name", attribute.getName());
            attributeMap.put("description", attribute.getDescription());
            attributeMap.put("type", attribute.getAttrType());
            attributeList.add(attributeMap);
            ctr++;
        }
        outputMap.put("attributeData", attributeList);
    }

    /**
     * addAttribute - add an attribute if it doesn't already exist.
     *
     * @param newName string with name of new attribute
     * @param newDescription string with description of new attribute
     * @param newType string with type of new attribute
     * @throws AAAException
     */
    public void addAttribute(String newName, String newDescription,
                     String newType, AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException{

        String methodName = "Attributes.addAttribute";
        Attribute attribute = new Attribute();
        attribute.setName(newName);
        attribute.setDescription(newDescription);
        attribute.setAttrType(newType);
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.ATTRIBUTE);
        rmiParams.put("operation", ModelOperation.ADD);
        rmiParams.put("attribute", attribute);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log,
                                                 out, rmiParams);
    }

    /**
     * modifyAttribute - change an attribute's name, description, and/or type.
     *
     * @param oldName string with old name of attribute
     * @param newName string new name of attribute
     * @param descr string with attribute description
     * @param attrType string with type of attribute
     * @throws AAAException
     */
    public void modifyAttribute(String oldName, String newName, String descr,
                    String attrType, AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {

        String methodName = "Attributes.modifyAttribute";
        Attribute attribute = new Attribute();
        attribute.setName(newName);
        attribute.setDescription(descr);
        attribute.setAttrType(attrType);
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.ATTRIBUTE);
        rmiParams.put("operation", ModelOperation.MODIFY);
        rmiParams.put("attribute", attribute);
        rmiParams.put("oldName", oldName);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log,
                                                 out, rmiParams);
    }

    /**
     * deleteAttribute - delete an attribute, but only if no users
     *     currently belong to it
     *
     * @param attributeName string with name of attribute to delete
     * @throws AAAException
     */
    public void deleteAttribute(String attributeName, AaaRmiInterface rmiClient,
                                PrintWriter out)
            throws RemoteException {

        String methodName = "Attributes.deleteAttribute";
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.ATTRIBUTE);
        rmiParams.put("operation", ModelOperation.DELETE);
        rmiParams.put("name", attributeName);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log,
                                                 out, rmiParams);

    }
}
