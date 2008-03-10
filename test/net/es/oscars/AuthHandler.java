package net.es.oscars;

import java.util.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.topology.CommonParams;

/**
 * This class sets the fields for a layer 2 reservation that are
 * available before scheduling
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AuthHandler {
    private Properties props;

    public AuthHandler () {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

    public boolean checkAuthorization() {
        String hostsProp = this.props.getProperty("allowedHosts");
        Map<String,String> hostsMap = new HashMap<String,String>();
        String[] allowedHosts = hostsProp.split(", ");
        for (int i=0; i < allowedHosts.length; i++) {
            hostsMap.put(allowedHosts[i], null);
        }
        String thisHost = System.getenv("HOST");
        assert hostsMap.containsKey(thisHost);
        String usersProp = this.props.getProperty("allowedUsers");
        Map<String,String> usersMap = new HashMap<String,String>();
        String[] allowedUsers = usersProp.split(", ");
        for (int i=0; i < allowedUsers.length; i++) {
            usersMap.put(allowedUsers[i], null);
        }
        String thisUser = System.getenv("USER");
        return usersMap.containsKey(thisUser);
    }
}
