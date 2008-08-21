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
}
