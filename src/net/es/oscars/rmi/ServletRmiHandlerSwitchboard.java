package net.es.oscars.rmi;

import java.io.IOException;
import java.util.HashMap;

import net.es.oscars.oscars.*;

import org.apache.log4j.Logger;
import java.rmi.Remote;

public class ServletRmiHandlerSwitchboard implements Remote {
    private OSCARSCore core;
    private Logger log;
    private ServletCreateRmiHandler createHandler;


    public ServletRmiHandlerSwitchboard() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.createHandler = new ServletCreateRmiHandler();
    }

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) throws IOException {
        return this.createHandler.createReservation(inputMap, userName);
    }

}
