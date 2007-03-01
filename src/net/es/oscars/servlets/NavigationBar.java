package net.es.oscars.servlets;

import java.io.*;
import java.util.Map;

public class NavigationBar {

    public void init(PrintWriter out, Map<String,String> tabParams) {
        String methodName;

        out.println("<navigation><ul id='tabnav'>");
        if (tabParams.get("ListReservations") != null)  {
            methodName = "ListReservations";
            out.println("<li><a id='ListReservations' " +
                "title='View/edit reservations' " +
                "class='active' " +
                "onclick=\"return newSection('" + methodName + "');\" " +
                "href='#'>Reservations</a></li>");
        }
        if (tabParams.get("CreateReservationForm") != null) {
            methodName = "CreateReservationForm";
            out.println("<li> <a id='CreateReservationForm' " +
                "title='Create an OSCARS reservation' " +
                "onclick=\"return newSection('" + methodName + "');\" " +
                "href='#'>Create Reservation</a></li>");
        }
        if (tabParams.get("UserList") != null) {
            methodName = "UserList";
            out.println("<li> <a id='UserList' " +
                "title='Manage user accounts' " +
                "onclick=\"return newSection('" + methodName + "');\" " +
                "href='#'>Users</a></li>");
        }
        if (tabParams.get("UserQuery") != null) {
            methodName = "UserQuery";
            out.println("<li> <a id='UserQuery' " +
                "title='View/edit my profile' " +
                "onclick=\"return newSection('" + methodName + "');\" " +
                "href='#'>User Profile</a></li>");
        }
        /* TODO:  these tabs later
        if (tabParams.get("ResourceList") != null)  {
            methodName = "ResourceList";
            out.println("<li><a id='ResourceList' " +
               "title='Manage resources' " +
                "onclick=\"return newSection('" + methodName + "');\" " +
               "href='#'>Resources</a></li>");
        }
        if (tabParams.get("AuthorizationList") != null ) {
            methodName = "AuthorizationList";
            out.println("<li> <a id='AuthorizationList' " +
               "title='Manage authorizations' " +
                "onclick=\"return newSection('" + methodName + "');\" " +
               "href='#'>Authorizations</a></li>");
        } */
        out.println(" <li><a id='UserLogout' " +
               "title='Log out on click' " +
               "href='servlet/UserLogout'>Log out</a></li></ul> " +
               "</navigation>");
    }
}
