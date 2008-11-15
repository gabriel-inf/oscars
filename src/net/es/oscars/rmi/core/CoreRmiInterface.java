package net.es.oscars.rmi.core;

import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.bss.*;

public interface CoreRmiInterface extends BssRmiInterface, AaaRmiInterface  {
    static int rmiPort = 1099;
    static String localhost = "127.0.0.1";
    static String registryName = "IDCRMIServer";

}
