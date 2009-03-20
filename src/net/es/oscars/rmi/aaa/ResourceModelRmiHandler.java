package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Session;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.model.*;


public class ResourceModelRmiHandler extends ModelRmiHandlerImpl {
    private AAACore core = AAACore.getInstance();
    private Logger log;


    public ResourceModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
    }


    public HashMap<String, Object> list(HashMap<String, Object> parameters)  {
        this.log.debug("listResources.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        ResourceDAO resourceDAO = new ResourceDAO(core.getAaaDbName());
        List<Resource> resources = resourceDAO.list();
        result.put("resources", resources);

        aaa.getTransaction().commit();
        this.log.debug("listResources.end");
        return result;
    }

    public HashMap<String, Object> find(HashMap<String, Object> parameters)  {
        this.log.debug("findResource.start");
        Integer id = (Integer) parameters.get("id");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        HashMap<String, Object> result = new HashMap<String, Object>();

        ResourceDAO resourceDAO = new ResourceDAO(core.getAaaDbName());
        Resource resource = resourceDAO.findById(id, false);
        result.put("resource", resource);
        aaa.getTransaction().commit();
        this.log.debug("findResource.end");
        return result;
    }

}
