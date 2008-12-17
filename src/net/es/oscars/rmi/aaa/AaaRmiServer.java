package net.es.oscars.rmi.aaa;

import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;

import net.es.oscars.PropHandler;
import net.es.oscars.PropertyLoader;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.*;
import net.es.oscars.rmi.model.*;

import org.apache.log4j.*;

public class AaaRmiServer  implements AaaRmiInterface  {
    private Logger log = Logger.getLogger(AaaRmiServer.class);
    private Registry registry;

    /** Static remote object so that GarbageCollector doesn't delete it */
    public static AaaRmiServer staticObject;

    private AaaRmiInterface stub;
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


    /**
     * AaaRmiServer constructor
     * @throws RemoteException
     */
    public AaaRmiServer() throws RemoteException {
        AaaRmiServer.staticObject = this;
    }


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
        this.log.debug("AaaRmiServer.init().start");

        Properties props = PropertyLoader.loadProperties("rmi.properties","aaa",true);


        // default rmi registry port
        int rmiPort = AaaRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = AaaRmiInterface.registryAddress;
        // default rmi registry name
        String rmiRegName = AaaRmiInterface.registryName;

        if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").equals("")) {
            try {
                rmiPort = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) {
                this.log.warn(e);
            }
        }

        if (props.getProperty("registryAddress") != null && !props.getProperty("registryAddress").equals("")) {
            rmiIpaddr = props.getProperty("registryAddress");
        }

        if (props.getProperty("registryName") != null && !props.getProperty("registryName").equals("")) {
            rmiRegName = props.getProperty("registryName");
        }


        this.log.info("AAA server RMI info: "+rmiIpaddr+":"+rmiPort+":"+rmiRegName);

        InetAddress ipAddr = null;
        AnchorSocketFactory sf = null;
        // Causes the endPoint of the remote sever object to match the interface that is listened on
        System.setProperty("java.rmi.server.hostname",rmiIpaddr);
        try {
            ipAddr = InetAddress.getByName(rmiIpaddr);
            // creates a custom socket that only listens on ipAddr
            sf = new AnchorSocketFactory(ipAddr);
            this.registry = LocateRegistry.createRegistry(rmiPort, null, sf);
        } catch (UnknownHostException ex) {
            this.log.error(ex);
        }

        this.stub = (AaaRmiInterface) UnicastRemoteObject.exportObject(AaaRmiServer.staticObject, rmiPort, null, sf);
        this.registry.rebind(rmiRegName, this.stub);
        this.initHandlers();
        this.log.debug("AaaRmiServer.init().end");
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
            throw new RemoteException(ex.getMessage());
        }
    }



    /**
     * shutdown
     */
    public void shutdown() {
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(AaaRmiServer.staticObject, true);
            java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);
            this.registry.unbind(registryName);
        } catch (RemoteException ex) {
            this.log.error("Remote exception shutting down AAA RMI server", ex);

        } catch (NotBoundException ex) {
            this.log.error("AAA RMI Server already unbound", ex);
        }
    }

}
