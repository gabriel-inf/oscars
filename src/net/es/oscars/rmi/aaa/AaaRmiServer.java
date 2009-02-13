package net.es.oscars.rmi.aaa;

import java.util.*;

import java.rmi.*;

import net.es.oscars.PropHandler;
import net.es.oscars.PropertyLoader;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.*;
import net.es.oscars.rmi.model.*;

import org.apache.log4j.*;

public class AaaRmiServer extends BaseRmiServer implements AaaRmiInterface  {
    protected Logger log;

    private VerifyLoginRmiHandler verifyLoginHandler;
    private CheckAccessRmiHandler checkAccessHandler;
    private ModelRmiHandler authorizationModelHandler;
    private ModelRmiHandler userModelHandler;
    private ModelRmiHandler attributeModelHandler;
    private ModelRmiHandler constraintModelHandler;
    private ModelRmiHandler permissionModelHandler;
    private ModelRmiHandler resourceModelHandler;
    private ModelRmiHandler rpcModelHandler;
    private ModelRmiHandler institutionModelHandler;

    protected static AaaRmiServer staticObject = null;



    /**
     * init
     *   By default initializes the RMI server registry to listen on port 1099 (the default)
     *   the RMI server to listen on a random port, and both to listen only on the loopback
     *   interface. These values can be overidden by oscars.properties.
     *   Setting the serverIpaddr to localhost will allow access from remote hosts and
     *   invalidate our security assumptions.
     *
     * @throws remoteException
     */
    public void init() throws RemoteException {
        this.log = Logger.getLogger(this.getClass());

        AaaRmiServer.staticObject = this;

        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi.aaa", true);;
        this.setProperties(props);
        // name of aaa service in registry, will be reset from aaa.registryName in rmi properties
        this.setRmiServiceName("AAARMIServer");
        // used for logging in BaseRmiServer.init
        this.setServiceName("AAA RMI Server");

        super.init(staticObject);
        this.initHandlers();
    }

    public void shutdown() {
        super.shutdown(staticObject);
    }

    public void initHandlers() {
        this.log.debug("Initializing AAA RMI handlers");
        this.verifyLoginHandler 	= new VerifyLoginRmiHandler();
        this.checkAccessHandler 	= new CheckAccessRmiHandler();
        this.authorizationModelHandler = new AuthorizationModelRmiHandler();
        this.userModelHandler 		= new UserModelRmiHandler();
        this.attributeModelHandler 	= new AttributeModelRmiHandler();
        this.constraintModelHandler = new ConstraintModelRmiHandler();
        this.resourceModelHandler 	= new ResourceModelRmiHandler();
        this.permissionModelHandler = new PermissionModelRmiHandler();
        this.rpcModelHandler 		= new RpcModelRmiHandler();
        this.institutionModelHandler 	= new InstitutionModelRmiHandler();
        this.log.debug("Done initializing AAA RMI handlers");
    }

    public Boolean validSession(String userName, String sessionName) throws RemoteException {
        return this.verifyLoginHandler.validSession(userName, sessionName);
    }

    public String getInstitution(String userName) throws RemoteException {
        return this.checkAccessHandler.getInstitution(userName);
    }


    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        return this.verifyLoginHandler.verifyLogin(userName, password, sessionName);
    }

    public String verifyDN(String dn) throws RemoteException {
        return this.verifyLoginHandler.verifyDN(dn);
    }

    public AuthValue checkAccess(String userName, String resourceName, String permissionName) throws RemoteException {
        return this.checkAccessHandler.checkAccess(userName, resourceName, permissionName);
    }

    public AuthMultiValue checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions) throws RemoteException {
        return this.checkAccessHandler.checkMultiAccess(userName, resourcePermissions);
    }

    public AuthValue
        checkModResAccess(String userName, String resourceName, String permissionName,
            int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI) throws RemoteException {
        return this.checkAccessHandler.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
    }
    
    public Boolean
        checkDomainAccess(String userName,String institutionName,String srcTopologyId,String destTopologyId)
            throws RemoteException{
        return this.checkAccessHandler.checkDomainAccess(userName,institutionName,srcTopologyId,destTopologyId);
    }


    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException {
        ModelObject objectType = (ModelObject) parameters.get("objectType");
        try {
            if (objectType == null) {
                throw new RemoteException("null objectType");

            } else if (objectType == ModelObject.ATTRIBUTE) {
                return this.attributeModelHandler.manage(parameters);
            } else if (objectType == ModelObject.INSTITUTION) {
                return this.institutionModelHandler.manage(parameters);
            } else if (objectType == ModelObject.AUTHORIZATION) {
                return this.authorizationModelHandler.manage(parameters);
            } else if (objectType == ModelObject.USER) {
                return this.userModelHandler.manage(parameters);
            } else if (objectType == ModelObject.RESOURCE) {
                return this.resourceModelHandler.manage(parameters);
            } else if (objectType == ModelObject.PERMISSION) {
                return this.permissionModelHandler.manage(parameters);
            } else if (objectType == ModelObject.CONSTRAINT) {
                return this.constraintModelHandler.manage(parameters);
            } else if (objectType == ModelObject.RPC) {
                return this.rpcModelHandler.manage(parameters);
            } else {
                throw new RemoteException("unknown objectType");
            }
        } catch (Exception ex) {
            log.error(ex);
            throw new RemoteException(ex.getMessage(),ex);
        }
    }

}
