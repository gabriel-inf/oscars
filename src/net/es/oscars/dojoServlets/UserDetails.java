package net.es.oscars.dojoServlets;

import java.io.PrintWriter;
import java.util.*;
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
     * @param outputMap map with parameter values for userPane
     * @param user the user whose information is being displayed 
     * @param modifyAllowed - true if the  user displaying this information has 
     *                        permission to modify it
     * @param modifyRights true if the  user displaying this information has 
     *                     permission to modify the target user's attributes
     * @param insts list of all institutions (a constant?)
     * @param attrNames all the attributes of the target user
     */
    public void
        contentSection(Map outputMap, User user, boolean modifyAllowed,
                       boolean modifyRights, List<Institution> insts,
                       List<String> attrNames) {

        this.log = Logger.getLogger(this.getClass());
        this.log.debug("contentSection: start");
        if (modifyAllowed) {
            outputMap.put("allowModify", Boolean.TRUE);
            outputMap.put("userHeader",
                          "Editing profile for user: " + user.getLogin());
        } else {
            outputMap.put("allowModify", Boolean.FALSE);
            outputMap.put("userHeader", "Profile for user: " + user.getLogin());
        } 
        if (attrNames == null) {
            this.log.debug("contentSection: attrNames is null");
        }
        if (attrNames.isEmpty()) {
            this.log.debug("contentSection: attrNames is empty");
        }

        String strParam = user.getLogin();
        if (strParam == null) { strParam = ""; }
        outputMap.put("profileName", strParam);
        strParam = user.getPassword();
        if (strParam != null) {
            outputMap.put("password", "********");
        }
        if (strParam != null) {
           outputMap.put("passwordConfirmation", "********");
        }
        strParam = user.getFirstName();
        if (strParam != null) {
           outputMap.put("firstName", strParam);
        }
        strParam = user.getLastName();
        if (strParam != null) {
           outputMap.put("lastName", strParam);
        }
        strParam = user.getCertSubject();
        if (strParam != null) {
           outputMap.put("certSubject", strParam);
        }
        strParam = user.getCertIssuer();
        if (strParam != null) {
           outputMap.put("certIssuer", strParam);
        }
        this.outputInstitutionMenu(outputMap, insts, user);
        this.outputRoleList(outputMap, attrNames, modifyRights);
        
        strParam = user.getDescription();
        if (strParam != null) {
           outputMap.put("description", strParam);
        }
        strParam = user.getEmailPrimary();
        if (strParam != null) {
           outputMap.put("emailPrimary", strParam);
        }
        strParam = user.getEmailSecondary();
        if (strParam != null) {
           outputMap.put("emailSecondary", strParam);
        }
        strParam = user.getPhonePrimary();
        if (strParam != null) {
           outputMap.put("phonePrimary", strParam);
        }
        strParam = user.getPhoneSecondary();
        if (strParam != null) {
           outputMap.put("phoneSecondary", strParam);
        }
        this.log.debug("contentSection: finish");
    }

    public void
        outputInstitutionMenu(Map outputMap, List<Institution> insts,
                              User user) {

        Institution userInstitution = null;
        String institutionName = "";
        StringBuffer sb = new StringBuffer();

        sb.append("<select class='required' name='institutionName'>");
        userInstitution = user.getInstitution();
        if (userInstitution != null) {
            institutionName = userInstitution.getName();
        } else {
            // use default
            institutionName = "Energy Sciences Network";
        }
        for (Institution i: insts) {
            sb.append("<option value='" + i.getName() + "' ");
            if (i.getName().equals(institutionName)) {
                sb.append("selected='selected'" );
            }
            sb.append(">" + i.getName() + "</option>" );
        }
        sb.append("</select>");
        outputMap.put("institutionMenuDiv", sb.toString());
    }
    
    public void outputRoleList(Map outputMap, List<String> attrNames,
                               boolean modify) {

        StringBuilder sb = new StringBuilder();

        this.log.debug("outputRoleList: start");
        sb.append("<tr>");
        sb.append("<td>Choose role(s)</td>");
        sb.append("<td align = 'left'>");
        if (modify) {
            if (attrNames.contains("OSCARS-user")) {
                sb.append("<input type='checkbox'  checked='checked' name='roles' value='OSCARS-user' /> User - make reservations");
            } else {
                sb.append("<input type='checkbox'  name='roles' value='OSCARS-user' /> User - make reservations");
            }
            sb.append("</td></tr>");
            sb.append("<tr><td>*</td><td align='left'>");
            if (attrNames.contains("OSCARS-engineer")) {
                sb.append("<input type='checkbox'  checked='checked' name='roles' value='OSCARS-engineer' /> Engineer - manage all reservations");
            } else {
                sb.append("<input type='checkbox'  name='roles' value='OSCARS-engineer' /> Engineer - manage all reservations");
            }
            sb.append("</td></tr>");
            sb.append("<tr><td>*</td><td align='left'>");
            if (attrNames.contains("OSCARS-administrator")){
                sb.append("<input type='checkbox' checked='checked' name='roles' value='OSCARS-administrator ' /> Administrator - manage all users");
            } else {
                sb.append("<input type='checkbox' name='roles' value='OSCARS-administrator' /> Administrator - manage all users");
            }
            sb.append("</td></tr>");
            sb.append("<tr><td>Define new role</td>");
            sb.append("<td>");
            sb.append("<input type='text' name='newRole' size='40' />");
            sb.append("</td></tr>");
        } else {  // user may not modify attributes
            sb.append("<td>Roles</td><td>---</td></tr>");
            if (attrNames.contains("OSCARS-user")) {
                sb.append("<tr><td>*</td><td align='left'>");
                sb.append("User - can make reservations");  
                sb.append("</td></tr>");
            }
            if (attrNames.contains("OSCARS-engineer")) {
                sb.append("<tr><td>*</td><td align='left'>");
                sb.append("Engineer - can manage all reservations");
                sb.append("</td></tr>");
            }
            if (attrNames.contains("OSCARS-administrator")){
                sb.append("<tr><td>*</td><td align='left'>");
                sb.append("Administrator - can manage all users");
                sb.append("</td></tr>");
            }
        }
        outputMap.put("roleListDiv", sb.toString());
        this.log.debug("outputRoleList: finish");
    }
}

