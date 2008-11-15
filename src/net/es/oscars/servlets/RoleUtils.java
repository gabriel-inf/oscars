package net.es.oscars.servlets;

import java.io.PrintWriter;
import javax.servlet.http.*;
import java.util.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.Attribute;


public class RoleUtils {

    /* Get the role names from a request and translate them to
     *   a list of AttributeIds
     *
     *   @param request
     */
    public ArrayList<Integer> convertRoles (String roles[], List<Attribute> attributes) {

        ArrayList <Integer> addRoles = new ArrayList<Integer>();
        Logger log = Logger.getLogger(this.getClass());
        if (roles != null && roles.length > 0) {
            String st;
            for (String s : roles) {
                log.debug("role is " + s);
                if (s != null && !s.trim().equals("")) {
                    st = s.trim();
                    Integer attrId = null;
                    for (Attribute attr : attributes) {
                        if (attr.getName().equals(st)) {
                            attrId = attr.getId();
                        }
                    }
                    if (attrId != null) {
                        if (!addRoles.contains(attrId)) {
                            log.info("adding "+ attrId);
                            addRoles.add(attrId);
                        }
                    }
                }
            }
        }
        return addRoles;
    }
}
