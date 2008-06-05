import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import org.apache.log4j.*;

import net.es.oscars.notify.*;

public class IDCNotifyServer{
    private Logger log;
    private RemoteEventProducer stub;
    private Registry registry;
    private boolean keepAlive;
    
    /* Make remote object static so GarbageCollector doesn't delete it */
    public static  RemoteEventProducer eventProducer;
    
    public IDCNotifyServer() throws RemoteException{
        this.log = Logger.getLogger(this.getClass());
        IDCNotifyServer.eventProducer = new RemoteEventProducerImpl();
        this.stub = (RemoteEventProducer) 
            UnicastRemoteObject.exportObject(IDCNotifyServer.eventProducer, 0);
    }
    
    public void start() throws RemoteException, NotifyException{
        EventConsumer eventConsumer = new EventConsumer();
        eventConsumer.start();
        this.registry = LocateRegistry.createRegistry(8090);
        this.registry.rebind("OSCARSRemoteEventProducer", this.stub);
    }

    public static void main(String[] args){
        try {
            IDCNotifyServer server = new IDCNotifyServer();
            server.start();
            System.out.println("IDC Notify Server started");
        }catch (Exception e) {
            System.out.println("IDC Notify Server failed: " + e);
        }
    }
}