package net.es.oscars.servlets;

import java.io.PrintWriter;
import javax.servlet.http.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.AttributeDAO;


public class RoleUtils {

    /* Get the role names from a request and translate them to
     *   a list of AttributeIds
     *   
     *   @param request 
     */
    public ArrayList<Integer> convertRoles (String roles[]) {

        ArrayList <Integer> addRoles = new ArrayList<Integer>();
        Logger log = Logger.getLogger(this.getClass());
        String dbname = Utils.getDbName();
        AttributeDAO attrDAO = new AttributeDAO(dbname);
        if (roles != null && roles.length > 0) {
            String st;
            for (String s : roles) {
                log.debug("role is " + s);
                if (s != null && !s.trim().equals("")) {
                    st=s.trim();
                    try {
                        Integer attrId = attrDAO.getIdByName(st);
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

    /* Converts an attribute name to a more human readable one.
     *
     * @param attributeName String with attribute name from table
     * @return newName String with modified attribute name
     */
    public String convertAttributeName(String attributeName) {
        // assumes attribute name starts with string followed by -
        String[] namePortions = attributeName.split("-");
        if (namePortions.length == 1) {
            return attributeName;
        }
        StringBuilder sb = new StringBuilder();
        String s = namePortions[1];
        if (s.length() > 0) {
            s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
        sb.append(s + " ");
        for (int i=2; i < namePortions.length; i++) {
            sb.append(namePortions[i] + " ");
        }
        return sb.toString().trim();
    }
    
    /* Converts the attribute field to an real attribute name.
    *
    * @param attributeField String with attribute name from a web form
    * @return newName String with the actual attribute name
    */
   public String convertAttributeField(String attributeField) {
       // assumes field name has an abbreviated attribute name followed by " -> description"
       String[] namePortions = attributeField.split(" ->");
       StringBuilder sb = new StringBuilder();
       String s = namePortions[0];
       if (s.length() > 0) {
           s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
       }
       sb.append("OSCARS " + s);
       int fromIndex = 0;
       int i;
       while (fromIndex >= 0) {
           i=sb.indexOf(" ", fromIndex);
           if (i != -1) {
            sb.replace(i,i+1,"-");
           }
           fromIndex=i;
       }
       return sb.toString().trim();
   }
}
