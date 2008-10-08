package net.es.oscars.servlets;

import java.io.PrintWriter;
import javax.servlet.http.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.AttributeDAO;


public class Utils {

    public static String getDbName() {
        // hard-wire in only one place
        return "aaa";
    }

    public static void handleFailure(PrintWriter out, String message,
                                     String method, Session aaa) {

        if (aaa != null) { aaa.getTransaction().rollback(); }
        Map errorMap = new HashMap();
        errorMap.put("success", Boolean.FALSE);
        errorMap.put("status", message);
        errorMap.put("method", method);
        JSONObject jsonObject = JSONObject.fromObject(errorMap);
        out.println("/* " + jsonObject + " */");
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
