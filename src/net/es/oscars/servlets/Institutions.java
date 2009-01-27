package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;


public class Institutions extends HttpServlet {
    private Logger log = Logger.getLogger(Institutions.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "Institutions";
        this.log.debug("Institutions.start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            this.log.error("Incorrect input from Institutions page");
            ServletUtils.handleFailure(out, "incorrect input from Institutions page", methodName);
            return;
        }
        String opName = ops[1];

        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            AaaRmiInterface rmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            AuthValue authVal =
                rmiClient.checkAccess(userName, "AAA", "modify");
            if (authVal == AuthValue.DENIED) {
                this.log.error("No permission to modify Institutions table.");
                ServletUtils.handleFailure(out, "no permission to modify Institutions table", methodName);
                return;
            }
            String saveName = request.getParameter("saveName");
            if (saveName != null) {
                saveName = saveName.trim();
            }
            String institutionEditName = request.getParameter("institutionEditName").trim();
            if (opName.equals("add")) {
                methodName = "InstitutionAdd";
                this.addInstitution(institutionEditName, rmiClient, out);
                outputMap.put("status", "Added institution: " + institutionEditName);
            } else if (opName.equals("modify")) {
                methodName = "InstitutionModify";
                this.modifyInstitution(saveName, institutionEditName, rmiClient, out);
                outputMap.put("status", "Changed institution name from " + saveName + " to " + institutionEditName);
            } else if (opName.equals("delete")) {
                methodName = "InstitutionDelete";
                this.deleteInstitution(institutionEditName, rmiClient, out);
                outputMap.put("status", "Deleted institution: " + institutionEditName);
            } else {
                methodName = "InstitutionList";
                outputMap.put("status", "Institutions management");
            }
            // always output latest list
            this.outputInstitutions(outputMap,rmiClient, out);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, null, e, methodName);
            return;
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.debug("Institutions.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * outputInstitutions - gets the initial list of institutions.
     *
     * @param outputMap Map containing JSON data
     */
    public void outputInstitutions(Map<String, Object> outputMap,
                                   AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {

        String methodName = "Institutions.outputInstitutions";
        List<Institution> institutions = ServletUtils.getAllInstitutions(rmiClient, out, log);
        ArrayList<HashMap<String,String>> institutionList =
            new ArrayList<HashMap<String,String>>();
        int ctr = 0;
        for (Institution institution: institutions) {
            HashMap<String,String> institutionMap =
                new HashMap<String,String>();
            institutionMap.put("id", Integer.toString(ctr));
            institutionMap.put("name", institution.getName());
            institutionList.add(institutionMap);
            ctr++;
        }
        outputMap.put("institutionData", institutionList);
    }

    /**
     * addInstitution - add an institution if it doesn't already exist.
     *
     * @param newName string with name of new institution
     * @throws AAAException
     */
    public void addInstitution(String newName, AaaRmiInterface rmiClient,
                               PrintWriter out) throws RemoteException {

        String methodName = "Institutions.addInstitution";
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.INSTITUTION);
        rmiParams.put("operation", ModelOperation.ADD);
        rmiParams.put("institutionName", newName);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
    }

    /**
     * modifyInstitution - change an institution's name.
     *
     * @param oldName string with old name of institution
     * @param newName string with new name of institution
     * @throws AAAException
     */
    public void modifyInstitution(String oldName, String newName,
                                  AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {

        String methodName = "Institutions.modifyInstitution";
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.INSTITUTION);
        rmiParams.put("operation", ModelOperation.MODIFY);
        rmiParams.put("newName", newName);
        rmiParams.put("oldName", oldName);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log,
                                                 out, rmiParams);
   }

    /**
     * deleteInstitution - delete an institution, but only if no users
     *     currently belong to it
     *
     * @param institutionName string with name of institution to delete
     * @throws AAAException
     */
    public void deleteInstitution(String institutionName,
                                  AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {

        String methodName = "Institutions.deleteInstitution";
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.INSTITUTION);
        rmiParams.put("operation", ModelOperation.DELETE);
        rmiParams.put("institutionName", institutionName);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log,
                                                 out, rmiParams);
    }
}
