package net.es.oscars.rmi.aaa;

import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.es.oscars.aaa.UserManager;
import net.es.oscars.oscars.OSCARSCore;

public class VerifyLoginRmiHandler {

    private OSCARSCore core = OSCARSCore.getInstance();
    private Logger log = Logger.getLogger(VerifyLoginRmiHandler.class);

    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        this.log.debug("VerifyLogin.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();
        String username = null;
        try {
            username = um.verifyLogin(userName, password, sessionName);
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        }
        this.log.debug("VerifyLogin.end");
        aaa.getTransaction().commit();
        return username;

    }

    public Boolean validSession(String userName, String sessionName) {
        this.log.debug("ValidSession.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();
        Boolean valid = um.validSession(userName, sessionName);
        if (valid == null) {
            this.log.warn("VerifyLoginRmiHandler.validSession: null validSession from UserManager");
            valid = false;
        }

        aaa.getTransaction().commit();
        this.log.debug("ValidSession.end");
        return valid;

    }
}
