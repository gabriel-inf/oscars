package net.es.oscars.dojoServlets;

import java.io.PrintWriter;
import javax.servlet.http.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.AttributeDAO;


public class Utils {

    public void handleFailure(PrintWriter out, String message, Session aaa,
                              Session bss) {

        if (aaa != null) { aaa.getTransaction().rollback(); }
        if (bss != null) { bss.getTransaction().rollback(); }
        Map errorMap = new HashMap();
        errorMap.put("success", Boolean.FALSE);
        errorMap.put("status", message);
        JSONObject jsonObject = JSONObject.fromObject(errorMap);
        out.println("/* " + jsonObject + " */");
        return;
    }
    
    /**
     * Checks for proper confirmation of password change. 
     *
     * @param password  A string with the desired password
     * @param confirmationPassword  A string with the confirmation password
     * @return String containing a new password if the password and confirmationPassword
     *       agree and if the password is not null, blank or equal "********". Otherwise it 
     *       returns null and the user password should not be reset
     */
    public String checkPassword(String password, String confirmationPassword)
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
    public String checkDN(String DN) 
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

    /* Get the role names from a request and translate them to
     *   a list of AttributeIds
     *   
     *   @param request 
     */
    public ArrayList <Integer> convertRoles (String roles[]) {

        ArrayList <Integer> addRoles = new ArrayList<Integer>();
        Logger log = Logger.getLogger(this.getClass());
        String dbname="aaa";
        AttributeDAO attrDAO = new AttributeDAO(dbname);
        if (roles != null && roles.length > 0) {
            String st;
            for (String s : roles) {
                log.debug("role is " + s);
                if (s != null && !s.trim().equals("")) {
                    st=s.trim();
                    try {
                        Integer attrId = attrDAO.getAttributeId(st);
                        if (!addRoles.contains(attrId)) {
                            log.info("adding "+ attrId);
                            addRoles.add(attrId);
                        }
                    } catch (AAAException ex) {
                        log.error("Unknown attribute: ["+st+"]");
                    } catch (Exception e) {
                        log.error("exception " + e.getMessage());
                    }
                }
            }
        }
        return addRoles;
    }
}
