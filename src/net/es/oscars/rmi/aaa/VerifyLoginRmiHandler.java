package net.es.oscars.rmi.aaa;

import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.AAACore;

public class VerifyLoginRmiHandler {

    private AAACore core = AAACore.getInstance();
    private Logger log = Logger.getLogger(VerifyLoginRmiHandler.class);


    public String verifyDN(String dn) throws RemoteException {
        this.log.debug("VerifyDN.start " + dn);
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();
        String username = null;
        try {
            username = um.loginFromDN(dn);

            if (username == null) {
                String[] dnElems = null;
                // if that fails try the reverse of the elements in the DN
                dnElems = dn.split(",");
                String reverseDN = " " + dnElems[0];
                for (int i = 1; i < dnElems.length; i++) {
                    reverseDN = dnElems[i] + "," + reverseDN;
                }
                reverseDN = reverseDN.substring(1);
                this.log.debug("checkUser reverse DN: " + reverseDN);

                username = um.loginFromDN(reverseDN);
                if (username == null) {
                    AAAException AAAEx = new AAAException("verifyDN: invalid user" + dn);
                    throw AAAEx;
                }
            }
        } catch (Exception ex) {
            this.log.warn(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage(),ex);
        }
        this.log.debug("VerifyDN.end " + dn);
        aaa.getTransaction().commit();
        return username;
    }

    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        this.log.debug("VerifyLogin.start " + userName);
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager um = core.getUserManager();
        String username = null;
        try {
            username = um.verifyLogin(userName, password, sessionName);
        } catch (Exception ex) {
            this.log.warn(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage(),ex);
        }
        this.log.debug("VerifyLogin.end " + userName);
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
            this.log.warn(" null validSession from UserManager for user: " + userName);
            valid = false;
        }

        aaa.getTransaction().commit();
        this.log.debug("ValidSession.end");
        return valid;

    }
}
