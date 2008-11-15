package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Session;

import net.es.oscars.aaa.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.model.*;


public class ConstraintModelRmiHandler extends ModelRmiHandlerImpl {
    private OSCARSCore core;
    private Logger log;


    public ConstraintModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }


    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("listConstraints.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        ConstraintDAO constraintDAO = new ConstraintDAO(core.getAaaDbName());
        List<Constraint> constraints = constraintDAO.list();
        result.put("constraints", constraints);

        aaa.getTransaction().commit();
        this.log.debug("listConstraints.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("findConstraintById.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        ConstraintDAO constraintDAO = new ConstraintDAO(core.getAaaDbName());
        Constraint constraint = constraintDAO.findById(id, false);
        result.put("constraint", constraint);
        aaa.getTransaction().commit();
        this.log.debug("findConstraintById.end");
        return result;
    }

}
