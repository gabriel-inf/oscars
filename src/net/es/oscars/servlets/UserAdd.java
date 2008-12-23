package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.User;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;

public class UserAdd extends HttpServlet {
    private Logger log = Logger.getLogger(UserAdd.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.log.info("UserAdd.start");


        String methodName = "UserAdd";
        UserSession userSession = new UserSession();

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        String profileName = request.getParameter("profileName");

        AaaRmiInterface rmiClient = ServletUtils.getCoreRmiClient(methodName, log, out);
        AuthValue authVal = ServletUtils.getAuth(userName, "Users", "create", rmiClient, methodName, log, out);
        this.log.info(authVal.toString());
        this.log.info(profileName + " " + userName);
        RoleUtils roleUtils = new RoleUtils();

        String errMsg = null;
        if (authVal != AuthValue.ALLUSERS) {
            errMsg = "not allowed to add a new user";
        }
        if (profileName.equals(userName)) {
            errMsg = "can't add another account for onself";
        }
        if (errMsg != null) {
            ServletUtils.handleFailure(out, errMsg, methodName);
            return;
        }
        User newUser = null;
        try {
            newUser = this.toUser(out, profileName, request);
        } catch (AAAException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, e.getMessage(), methodName);
            return;
        }


        try {
            List<Attribute> attributes = ServletUtils.getAllAttributes(rmiClient, out, log);
            ArrayList <Integer> addRoles = null;
            String roles[] = request.getParameterValues("attributeName");
            for (int i=0; i < roles.length; i++) {
                roles[i] = ServletUtils.dropDescription(roles[i].trim());
            }
            // will be only one parameter value due to constraints
            // on client side
            if (roles[0].equals("None")) {
                this.log.debug("roles = null");
                addRoles = new ArrayList<Integer>();
            } else {
                this.log.debug("number of roles input is "+roles.length);
                addRoles = roleUtils.convertRoles(roles, attributes);
            }

            HashMap<String, Object> rmiParams = new HashMap<String, Object>();
            rmiParams.put("user", newUser);
            rmiParams.put("addRoles", addRoles);
            rmiParams.put("objectType", ModelObject.USER);
            rmiParams.put("operation", ModelOperation.ADD);
            HashMap<String, Object> rmiResult = new HashMap<String, Object>();
            rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);


        } catch (RemoteException e) {
            this.log.error(e.getMessage());
            ServletUtils.handleFailure(out, e.getMessage(), methodName);
            return;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "User " + profileName +  " successfully created");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info("servlet.end");
    }



    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public User toUser(PrintWriter out, String userName, HttpServletRequest request)
         throws AAAException {

        String strParam;
        String DN;
        String password;

        User user = new User();

        Institution institution = new Institution();
        institution.setName(request.getParameter("institutionName"));
        user.setInstitution(institution);


        user.setLogin(userName);
        strParam = request.getParameter("certIssuer");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        else { DN = ""; }
        user.setCertIssuer(DN);
        strParam = request.getParameter("certSubject");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        else { DN = ""; }
        user.setCertSubject(DN);
        // required fields by client, so always filled in
        user.setLastName(request.getParameter("lastName"));
        user.setFirstName(request.getParameter("firstName"));
        user.setEmailPrimary(request.getParameter("emailPrimary"));
        user.setPhonePrimary(request.getParameter("phonePrimary"));
        password = ServletUtils.checkPassword(request.getParameter("password"), request.getParameter("passwordConfirmation"));
        user.setPassword(password);
        // doesn't matter if blank
        user.setDescription(request.getParameter("description"));
        user.setEmailSecondary(request.getParameter("emailSecondary"));
        user.setPhoneSecondary(request.getParameter("phoneSecondary"));
        // noops currently
        user.setStatus(request.getParameter("status"));
        user.setActivationKey(request.getParameter("activationKey"));
        return user;
    }
}
