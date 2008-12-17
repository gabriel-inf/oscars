package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.util.SerializationHelper;

import net.sf.json.*;

import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.User;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;


public class UserList extends HttpServlet {
    private Logger log = Logger.getLogger(UserList.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.log.debug("UserList.start");
        String methodName = "UserList";

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
            AuthValue authVal = ServletUtils.getAuth(userName, "Users", "query", rmiClient, methodName, log, out);

            // if allowed to see all users, show help information on clicking on
            // row to see user details
            if  (authVal == AuthValue.ALLUSERS) {
                outputMap.put("userRowSelectableDisplay", Boolean.TRUE);
            } else {
                outputMap.put("userRowSelectableDisplay", Boolean.FALSE);
            }
            outputMap.put("status", "User list");
            this.outputUsers(outputMap, userName, request,rmiClient, out);
        } catch (RemoteException e) {
            return;
        }

        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.debug("UserList.end");
    }

    /**
     * Checks access and gets the list of users if allowed.
     *
     * @param outputMap Map containing JSON data
     * @param userName String containing name of user making request
     * @param request HttpServletRequest with form parameters
     * @throws AAAException
     */
    public void outputUsers(Map<String, Object> outputMap, String userName,
                            HttpServletRequest request, AaaRmiInterface rmiClient, PrintWriter out)
            throws RemoteException {
        String methodName = "UserList.outputUsers";


        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        } else {
            attributeName = "";
        }
        String attrsUpdated = request.getParameter("userListAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }

        AuthValue authVal = ServletUtils.getAuth(userName, "Users", "list", rmiClient, methodName, log, out);
        AuthValue aaaVal = ServletUtils.getAuth(userName, "AAA", "list", rmiClient, methodName, log, out);


        String listType = "plain";

        if (authVal == AuthValue.ALLUSERS) {
            // check to see if need to (re)display menu
            if (attributeName.equals("") ||
                ((attrsUpdated != null) && !attrsUpdated.equals(""))) {
                if (aaaVal != AuthValue.DENIED) {
                    outputMap.put("attributeInfoDisplay", Boolean.TRUE);
                    outputMap.put("attributeMenuDisplay", Boolean.TRUE);
                    this.outputAttributeMenu(outputMap, rmiClient, out);
                } else {
                    outputMap.put("attributeInfoDisplay", Boolean.FALSE);
                    outputMap.put("attributeMenuDisplay", Boolean.FALSE);
                }
            }
            if (attributeName.equals("") || (attributeName.equals("Any"))) {
                listType = "plain";
            } else {
                if (aaaVal == AuthValue.DENIED) {
                    listType = "plain";
                } else {
                    listType = "byAttr";
                }
            }
        } else if (authVal== AuthValue.SELFONLY) {
            listType = "single";
        } else {
            throw new RemoteException("no permission to list users");
        }


        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.USER);
        rmiParams.put("operation", ModelOperation.LIST);
        rmiParams.put("listType", listType);
        rmiParams.put("username", userName);
        rmiParams.put("attributeName", attributeName);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);

        List<User> users = (List<User>) rmiResult.get("users");

        ArrayList<ArrayList<String>> userList = new ArrayList<ArrayList<String>>();
        for (User user: users) {
            ArrayList<String> userEntry = new ArrayList<String>();
            userEntry.add(user.getLogin());
            userEntry.add(user.getLastName());
            userEntry.add(user.getFirstName());
            userEntry.add(user.getInstitution().getName());
            userEntry.add(user.getPhonePrimary());
            userList.add(userEntry);
        }
        outputMap.put("userData", userList);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void outputAttributeMenu(Map<String, Object> outputMap, AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {
        String methodName = "UserList.outputAttributeMenu";

        List<Attribute> attributes = ServletUtils.getAllAttributes(rmiClient, out, log);

        List<String> attrList = new ArrayList<String>();
        attrList.add("Any");
        attrList.add("true");
        for (Attribute attr: attributes) {
            attrList.add(attr.getName());
            attrList.add("false");
        }
        outputMap.put("attributeMenu", attrList);
    }
}
