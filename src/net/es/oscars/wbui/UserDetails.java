package net.es.oscars.wbui;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.*;

import net.es.oscars.aaa.User;
import net.es.oscars.aaa.Institution;


public class UserDetails {

    public void
        contentSection(PrintWriter out, User user, User requester,
                       List<Institution> insts, String servletName) {

        String nextPage = null;
        String header = null;
        String submitValue = null;

        if (servletName.equals("UserAddForm")) {
            nextPage = "UserAdd";
            header = "Add a new user";
            submitValue = "Add";
        } else {
            nextPage = "UserModify";
            header = "Editing profile for user: " + user.getLogin();
            submitValue = "Modify Profile";
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
        out.println("<td><input class='required' type='text' ");
        out.println("name='profileName' size='40' value='" +
                     strParam + "'></input></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Password (Enter twice)</td>");
        strParam = user.getPassword();
        out.println("<td>");
        out.println("<input class='required' type='password' " +
                        "name='password' size='40'");
        if (strParam != null) { out.println(" value='********'"); }
        out.println("></input></td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Password Confirmation</td>");
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
        this.outputInstitutionMenu(out, insts, user);

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
}
