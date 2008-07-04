package net.es.oscars.rmi;

import java.io.IOException;
import java.util.HashMap;

import net.es.oscars.oscars.*;

import org.apache.log4j.Logger;
import java.rmi.Remote;

public class RmiHandlerSwitchboard implements Remote {
    private OSCARSCore core;
    private Logger log;
    private CreateResRmiHandler createHandler;
    private QueryResRmiHandler queryHandler;
    private ListResRmiHandler listHandler;
    private CancelResRmiHandler cancelHandler;


    public RmiHandlerSwitchboard() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.createHandler = new CreateResRmiHandler();
        this.queryHandler = new QueryResRmiHandler();
        this.listHandler = new ListResRmiHandler();
        this.cancelHandler = new CancelResRmiHandler();
    }

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException {
        return this.createHandler.createReservation(inputMap, userName);
    }
    
    public HashMap<String, Object> queryReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException {
        return this.queryHandler.queryReservation(inputMap, userName);
    }
    
    public HashMap<String, Object> listReservations(HashMap<String, String[]> inputMap, String userName) 
        throws IOException {
         return this.listHandler.listReservations(inputMap, userName);
    }
    
    public HashMap<String, Object> cancelReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException {
        return this.cancelHandler.cancelReservation(inputMap, userName);
    }

}
