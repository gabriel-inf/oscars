package net.es.oscars.servlets;

import java.io.PrintWriter;
import javax.servlet.http.*;

import net.es.oscars.aaa.UserManager;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.AAAException;

import org.hibernate.*;

public class Utils {

    public void tabSection(PrintWriter out, HttpServletRequest request,
                           HttpServletResponse response, String tabName) {

        UserSession userSession = new UserSession();
        String previousTab = userSession.getCookie("tabName", request);
        if (previousTab == null) { previousTab = ""; }
        if (!previousTab.equals(tabName)) {
            userSession.setCookie("tabName", tabName, response);
        }
        out.println("<tabs>");
        out.println("<active>" + tabName + "</active>");
        out.println("<previous>" + previousTab + "</previous>");
        out.println("</tabs>");
    }

    public void handleFailure(PrintWriter out, String message, Session aaa,
                              Session bss) {

        if (aaa != null) { aaa.getTransaction().rollback(); }
        if (bss != null) { bss.getTransaction().rollback(); }
        out.println("<xml><status>");
        out.println(message);
        out.println("</status></xml>");
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
}
