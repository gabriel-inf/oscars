package net.es.oscars.servlets;

import java.io.PrintWriter;
import java.util.*;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import net.sf.json.*;

import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.Authorization;
import net.es.oscars.aaa.Constraint;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.Permission;
import net.es.oscars.aaa.Resource;
import net.es.oscars.aaa.Rpc;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.User;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.rmi.RmiUtils;

public class ServletUtils {
/**
 *  Gets a new, initialized CoreRmiInterface for client calls.
 * @param methodName String - name of method that is calling us
 * @param log Logger - place to log errors
 * @param out PrinterWriter - used by handleFailure in case of error.
 * @return  CoreRmiInterface
 */
    public static AaaRmiInterface getAaaRmiClient(String methodName, Logger log, PrintWriter out) {
        AaaRmiInterface rmiClient;
        try {
            rmiClient = RmiUtils.getAaaRmiClient(methodName, log); // will log the error
        } catch (RemoteException ex) {
            ServletUtils.handleFailure(out, methodName + " internal error: " + ex.getMessage(), methodName);
            return null;
        }
        return rmiClient;
    }
    public static BssRmiInterface getBssRmiClient(String methodName, Logger log, PrintWriter out) {
        BssRmiInterface rmiClient;
        try {
            rmiClient = RmiUtils.getBssRmiClient(methodName, log); // will log the error
        } catch (RemoteException ex) {
            ServletUtils.handleFailure(out, methodName + " internal error: " + ex.getMessage(), methodName);
            return null;
        }
        return rmiClient;
    }

    public static AuthValue getAuth(String userName, String resourceName, String permissionName, AaaRmiInterface rmiClient, String methodName, Logger log, PrintWriter out) {
       HashMap<String, Object> authResult = new HashMap<String, Object>();
       AuthValue authVal;
       try {
           authVal = rmiClient.checkAccess(userName, resourceName, permissionName);
       } catch (RemoteException ex) {
           log.error("RMI exception:  " + ex.getMessage());
           ServletUtils.handleFailure(out, methodName + " RMI exception: " + ex.getMessage(), methodName);
           authVal = AuthValue.DENIED;
       }
       return authVal;
    }

    public static HashMap<String, Object> manageAaaObject(AaaRmiInterface rmiClient, String callerMethodName, Logger log, PrintWriter out, HashMap<String, Object> parameters) throws RemoteException {
        HashMap<String, Object> result = null;
        try {
            result = rmiClient.manageAaaObjects(parameters);
        } catch (RemoteException e) {
            log.warn("RMI exception: " + e.getMessage(), e);
            ServletUtils.handleFailure(out, callerMethodName + " internal error: " + e.getMessage(), callerMethodName);
            throw e;
        }

        return result;
    }

    public static User getUser(String username, AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getUser";
        HashMap<String, Object> userRmiParams = new HashMap<String, Object>();
        userRmiParams.put("objectType", ModelObject.USER);
        userRmiParams.put("operation", ModelOperation.FIND);
        userRmiParams.put("findBy", "username");
        userRmiParams.put("username", username);
        HashMap<String, Object> userRmiResult = new HashMap<String, Object>();
        userRmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, userRmiParams);

        User user = (User) userRmiResult.get("user");

        return user;
    }


    public static List<Attribute> getAttributesForUser(String username, AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllObjects";

        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", ModelObject.ATTRIBUTE);
        rmiParams.put("operation", ModelOperation.LIST);
        rmiParams.put("listBy", "username");
        rmiParams.put("username", username);
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);


        List<Attribute> attributes = (List<Attribute>) rmiResult.get("attributes");
        if (attributes == null) {
            attributes = new ArrayList<Attribute>();
        }
        return attributes;

    }




    private static Object getAllObjects(ModelObject model, String objectName, AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllObjects";
        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("objectType", model);
        rmiParams.put("operation", ModelOperation.LIST);
        HashMap<String, Object> rmiResult = new HashMap<String, Object>();
        rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
        return rmiResult.get(objectName);
    }

    public static List<Permission> getAllPermissions(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllPermissions";
        List<Permission> objects = (List<Permission>) ServletUtils.getAllObjects(ModelObject.PERMISSION, "permissions", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Permission>();
        }
        return objects;
    }
    public static List<Attribute> getAllAttributes(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllAttributes";
        List<Attribute> objects = (List<Attribute>) ServletUtils.getAllObjects(ModelObject.ATTRIBUTE, "attributes", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Attribute>();
        }
        return objects;
    }
    public static List<Constraint> getAllConstraints(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllConstraints";
        List<Constraint> objects = (List<Constraint>) ServletUtils.getAllObjects(ModelObject.CONSTRAINT, "constraints", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Constraint>();
        }
        return objects;
    }

    public static List<Resource> getAllResources(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllResources";
        List<Resource> objects = (List<Resource>) ServletUtils.getAllObjects(ModelObject.RESOURCE, "resources", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Resource>();
        }
        return objects;
    }

    public static List<Rpc> getAllRpcs(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllRpcs";
        List<Rpc> objects = (List<Rpc>) ServletUtils.getAllObjects(ModelObject.RPC, "rpcs", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Rpc>();
        }
        return objects;
    }


    public static List<Authorization> getAllAuthorizations(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllAuthorizations";
        List<Authorization> objects = (List<Authorization>) ServletUtils.getAllObjects(ModelObject.AUTHORIZATION, "authorizations", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Authorization>();
        }
        return objects;
    }


    public static List<Institution> getAllInstitutions(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllInstitutions";
        List<Institution> objects = (List<Institution>) ServletUtils.getAllObjects(ModelObject.INSTITUTION, "institutions", rmiClient, out, log);
        if (objects == null) {
            objects = new ArrayList<Institution>();
        }
        return objects;
    }

    public static List<User> getAllUsers(AaaRmiInterface rmiClient, PrintWriter out, Logger log) throws RemoteException {
        String methodName = "Utils.getAllUsers";
        List<User> objects = (List<User>) ServletUtils.getAllObjects(ModelObject.USER, "users", rmiClient, out, log);

        if (objects == null) {
            objects = new ArrayList<User>();
        }
        return objects;
    }


    public static void handleFailure(PrintWriter out, String message, String method) {

        Map<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("success", Boolean.FALSE);
        errorMap.put("status", message);
        errorMap.put("method", method);
        JSONObject jsonObject = JSONObject.fromObject(errorMap);
        if (out != null) {
            out.println("{}&&" + jsonObject);
        }
        return;
    }

    /**
     * Checks for proper confirmation of password change.
     *
     * @param password  A string with the desired password
     * @param confirmationPassword  A string with the confirmation password
     * @return String containing a new password, if the password and
     *     confirmationPassword agree and if the password is not null, blank or
     *     equal to "********".  Otherwise it returns null, and the user
     *     password should not be reset.
     */
    public static String checkPassword(String password,
                                       String confirmationPassword)
            throws AAAException {

        // If the password needs to be updated, make sure there is a
        // confirmation password, and that it matches the given password.
        if ((password != null) && (!password.equals("")) &&
                (!password.equals("********"))) {
           if (confirmationPassword == null) {
                throw new AAAException(
                    "Cannot update password without confirmation password");
            } else if (!confirmationPassword.equals(password)) {
                throw new AAAException(
                     "Password and password confirmation do not match");
            }
           return password;
        }
        return null;
    }

    /**
     * CheckDN  check for the input DN to be in comma separated format starting
     *    with the CN element.
     * @param DN string containing the input DN
     * @return String returning the DN, possibily in reverse order
     * @throws AAAException if the DN is not in comma separated form.
     */
    public static String checkDN(String DN)
        throws AAAException {

        String[] dnElems = null;

        dnElems = DN.split(",");
        if (dnElems.length < 2)  {
            /* TODO look for / separated elements */
            throw  new AAAException
                    ("Please input cert issuer and subject names as comma separated elements");
         }
        if (dnElems[0].startsWith("CN")) { return DN;}
        /* otherwise reverse the order */
        String dn = " " + dnElems[0];
        for (int i = 1; i < dnElems.length; i++) {
            dn = dnElems[i] + "," + dn;
        }
        dn = dn.substring(1);
        return dn;
    }

    /**
     * removes the description part of the authorization input form fields
     * @param inputField A string with the complete field.
     * @return A string minus the description field of the parameter
     */
    public static String dropDescription(String inputField) {
     // assumes field name has a name followed by " -> description"
        String[] namePortions = inputField.split(" ->");
        return namePortions[0];
    }
}
