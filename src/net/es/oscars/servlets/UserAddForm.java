package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.*;


public class UserAddForm extends HttpServlet {
    private Logger log = Logger.getLogger(UserAddForm.class);

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();


        List<Institution> institutions = null;
        log.debug("servlet.start");

        String methodName = "UserAddForm";
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.error("No user session: cookies invalid");
            return;
        }


        AaaRmiInterface rmiClient = ServletUtils.getAaaRmiClient(methodName, log, out);
        AuthValue authVal = ServletUtils.getAuth(userName, "Users", "modify", rmiClient, methodName, log, out);

        if (authVal != AuthValue.ALLUSERS) {
            String errorMsg = "User "+userName+" is not allowed to add a new user";
            log.error(errorMsg);
            ServletUtils.handleFailure(out, errorMsg, methodName);
            return;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            this.outputAttributeMenu(outputMap, rmiClient, out);
            this.outputInstitutionMenu(outputMap, rmiClient, out);
        } catch (RemoteException ex) {
            return;
        }


        outputMap.put("status", "Add a user");
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




    public void outputAttributeMenu(Map<String, Object> outputMap, AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {

        String methodName = "UserAddForm.outputAttributeMenu";
        List<Attribute> attributes = ServletUtils.getAllAttributes(rmiClient, out, log);


        List<String> attributeList = new ArrayList<String>();
        // default is none
        attributeList.add("None");
        attributeList.add("true");
        for (Attribute a: attributes) {
            attributeList.add(a.getName() + " -> " + a.getDescription());
            attributeList.add("false");
        }
        outputMap.put("newAttributeNameMenu", attributeList);
    }

    public void outputInstitutionMenu(Map<String, Object> outputMap, AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {
        String methodName = "UserAddForm.outputInstitutionMenu";

        List<Institution> institutions = ServletUtils.getAllInstitutions(rmiClient, out, log);

        List<String> institutionList = new ArrayList<String>();
        int ctr = 0;
        // default is first in list
        for (Institution i: institutions) {
            institutionList.add(i.getName());
            if (ctr == 0) {
                institutionList.add("true");
            } else {
                institutionList.add("false");
            }
            ctr++;
        }
        outputMap.put("newInstitutionMenu", institutionList);
    }
}
