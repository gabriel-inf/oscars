package net.es.oscars.servlets;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import net.es.oscars.aaa.User;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserDetails {
    private Logger log;
    /**
     * writes out the user details form used by add,query and modify user profile
     * 
     * @param out  the output stream
     * @param user the user whose information is being displayed 
     * @param modifyAllowed - true if the  user displaying this information has 
     *                        permission to modify it
     * @param modifyRights true if the  user displaying this information has 
     *                     permission to modify the target user's attributes
     * @param insts list of all institutions (a constant?)
     * @param attrNames all the attributes of the target user
     * @param servletName name of the servlet that is calling this method
     */
    public void
        contentSection(PrintWriter out, User user, boolean modifyAllowed,
                       boolean modifyRights, List<Institution> insts,
                       List<String> attrNames, String servletName) {

        String nextPage = null;
        String header = null;
        String submitValue = null;
        String passwordClass = "'SOAP'";
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("contentSection: start");
        if (servletName.equals("UserAddForm")) {
            nextPage = "UserAdd";
            header = "Add a new user";
            submitValue = "Add";
            passwordClass="'required'";
        } else if (servletName.equals("UserModify") || modifyAllowed) {
            nextPage = "UserModify";
            header = "Editing profile for user: " + user.getLogin();
            submitValue = "Modify Profile";
        } else { // servletName = UserQuery and modifyAllowed is false
            nextPage = "UserModify";
            header = "Profile for user: " + user.getLogin();
            submitValue = "View Profile";
            /* TODO - David says this button should not appear if the user cannot'
             * add or modify the profile
             */
        } 
        if (attrNames == null) {
            this.log.debug("contentSection: attrNames is null");
        }
        if (attrNames.isEmpty()) {
            this.log.debug("contentSection: attrNames is empty");
        }

        out.println("<content>");
        String submitStr = "return submitForm(this, '" + nextPage + "');";
        out.println("<h3>" + header + "</h3>");
        out.println("<p>Required fields are outlined in green.</p>");
        out.println("<form method='post' action='' onsubmit=\"" +
                    submitStr + "\">");
        out.println("<p><input type='submit' value='" + submitValue +
                    "'></input></p>");
        out.println("<table>");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<td>Login Name</td>");
        String strParam = user.getLogin();
        if (strParam == null) { strParam = ""; }
        out.println("<td><input class='required' type='text' ");
        out.println("name='profileName' size='40' value='" +
                     strParam + "'></input></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Password (Enter twice)</td>");
        strParam = user.getPassword();
        out.println("<td>");
        //out.println("<input class="+ passwordClass + "type='password' " +
        out.println("<input class='required' type='password' " +
                        "name='password' size='40'");
        if (strParam != null) { out.println(" value='********'"); }
        out.println("></input></td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Password Confirmation</td>");
        //out.println("<td><input class=" + passwordClass + " type='password' " +
        out.println("<td><input class='required' type='password' " +
                    "name='passwordConfirmation' size='40'");
       if (strParam != null) { out.println(" value='********'"); }
        out.println("></input></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>First Name</td>");
        strParam = user.getFirstName();
        if (strParam == null) { strParam = ""; }
        out.println("<td><input class='required' type='text' " +
                "name='firstName' size='40' value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Last Name</td>");
        out.println("<td>");
        strParam = user.getLastName();
        if (strParam == null) { strParam = ""; }
        out.println("<input class='required' type='text' name='lastName' " + 
                    "size='40' value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");      
        
        out.println("<tr>");
        out.println("<td>X.509 subject name</td>");
        out.println("<td>");
        strParam = user.getCertSubject();
        if (strParam == null) { strParam = ""; }
        out.println("<input class='SOAP' type='text' name='certSubject' " + 
                    "size='40' value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>X.509 issuer name</td>");
        out.println("<td>");
        strParam = user.getCertIssuer();
        if (strParam == null) { strParam = ""; }
        out.println("<input class='SOAP' type='text' name='certIssuer' " + 
                    "size='40' value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        this.outputInstitutionMenu(out, insts, user);

        out.println("<tr>");
        this.outputRoleList(out,attrNames,modifyRights);
        
        out.println("<tr>");
        out.println("<td valign='top'>Personal Description</td>");
        out.println("<td>");
        out.println("<input class='SOAP' type='text' name='description' " +
                    "size='40'");
        strParam = user.getDescription();
        if (strParam == null) { strParam = ""; }
        out.println("value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>E-mail (Primary)</td>");
        out.println("<td><input class='required' type='text' " +
                    "name='emailPrimary' ");
        strParam = user.getEmailPrimary();
        if (strParam == null) { strParam = ""; }
        out.println("size='40' value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>E-mail (Secondary)</td>");
        out.println("<td><input class='SOAP' type='text' " +
                    " name='emailSecondary' size='40' ");
        strParam = user.getEmailSecondary();
        if (strParam == null) { strParam = ""; }
        out.println("value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Phone Number (Primary)</td>");
        out.println("<td>");
        out.println("<input class='required' type='text' name='phonePrimary' ");
        strParam = user.getPhonePrimary();
        if (strParam == null) { strParam = ""; }
        out.println("size='40' value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Phone Number (Secondary)</td>");
        out.println("<td>");
        out.println("<input class='SOAP' type='text' name='phoneSecondary' size='40' ");
        strParam = user.getPhoneSecondary();
        if (strParam == null) { strParam = ""; }
        out.println("value='" + strParam + "'></input>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</tbody></table></form>");
        out.println("</content>");
        this.log.debug("contentSection: finish");
    }

    public void
        outputInstitutionMenu(PrintWriter out, List<Institution> insts,
                              User user) {

        Institution userInstitution = null;
        String institutionName = "";

        out.println("<td>Organization</td>");
        out.println("<td><select class='required' name='institutionName'>");
        userInstitution = user.getInstitution();
        if (userInstitution != null) {
            institutionName = userInstitution.getName();
        } else {
            // use default
            institutionName = "Energy Sciences Network";
        }
        for (Institution i: insts) {
            out.println("<option value='" + i.getName() + "' ");
            if (i.getName().equals(institutionName)) {
                out.println("selected='selected'" );
            }
            out.println(">" + i.getName() + "</option>" );
        }
        out.println("</select>");
        out.println("</td>");
        out.println("</tr>");
    }
    
    public void outputRoleList(PrintWriter out, List<String> attrNames,
                               boolean modify) {

        // this.log.debug("outputRoleList: start");
        if (modify) {
            out.println("<td>Choose role(s)</td>");
            out.println("<td align='left'>");
            if (attrNames.contains("OSCARS-user")) {
                out.println("<input class='SOAP' type='checkbox'  checked='checked' name='roles' value='OSCARS-user' /> User - make reservations");
            } else {
                out.println("<input class='SOAP' type='checkbox'  name='roles' value='OSCARS-user' /> User - make reservations");
            }
            out.println("</td></tr>");
            out.println("<tr><td>*</td><td align='left'>");
            if (attrNames.contains("OSCARS-service")) {
                out.println("<input class='SOAP' type='checkbox'  checked='checked' name='roles' value='OSCARS-service' /> Service - make reservations and view topology");
            } else {
                out.println("<input class='SOAP' type='checkbox'  name='roles' value='OSCARS-service' /> Service - make reservations and view topology");
            }
            out.println("</td></tr>");
            out.println("<tr><td>*</td><td align='left'>");
            if (attrNames.contains("OSCARS-operator")) {
                out.println("<input class='SOAP' type='checkbox'  checked='checked' name='roles' value='OSCARS-operator' /> Operator - view all reservations");
            } else {
                out.println("<input class='SOAP' type='checkbox'  name='roles' value='OSCARS-operator' /> Operator - view all reservations");
            }
            out.println("</td></tr>");
            out.println("<tr><td>*</td><td align='left'>");
            if (attrNames.contains("OSCARS-engineer")) {
                out.println("<input class='SOAP' type='checkbox'  checked='checked' name='roles' value='OSCARS-engineer' /> Engineer - manage all reservations");
            } else {
                out.println("<input class='SOAP' type='checkbox'  name='roles' value='OSCARS-engineer' /> Engineer - manage all reservations");
            }
            out.println("</td></tr>");
            out.println("<tr><td>*</td><td align='left'>");
            if (attrNames.contains("OSCARS-administrator")){
                out.println("<input class='SOAP' type='checkbox' checked='checked' name='roles' value='OSCARS-administrator ' /> Administrator - manage all users");
            } else {
                out.println("<input class='SOAP' type='checkbox' name='roles' value='OSCARS-administrator' /> Administrator - manage all users");
            }
            out.println("</td></tr>");
            out.println("<tr><td>Define new role</td>");
            out.println("<td>");
            out.println("<input class='SOAP' type='text' name='newRole' size='40' />");
            out.println("</td></tr>");
        } else {  // user may not modify attributes
            out.println("<td>Roles</td><td>---</td></tr>");
            if (attrNames.contains("OSCARS-user")) {
                out.println("<tr><td>*</td><td align='left'>");
                out.println("User - can make reservations");  
                out.println("</td></tr>");
            }
            if (attrNames.contains("OSCARS-engineer")) {
                out.println("<tr><td>*</td><td align='left'>");
                out.println("Engineer - can manage all reservations");
                out.println("</td></tr>");
            }
            if (attrNames.contains("OSCARS-administrator")){
                out.println("<tr><td>*</td><td align='left'>");
                out.println("Administrator - can manage all users");
                out.println("</td></tr>");
            }
        }
        // this.log.debug("outputRoleList: finish");
    }
}

