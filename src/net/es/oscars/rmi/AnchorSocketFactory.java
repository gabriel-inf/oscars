package net.es.oscars.rmi;

import java.rmi.server.*;
import java.io.*;
import java.net.*;
public class AnchorSocketFactory extends RMISocketFactory implements Serializable {
    private InetAddress ipInterface = null;
    public AnchorSocketFactory() {}

    public AnchorSocketFactory(InetAddress ipInterface) {
        this.ipInterface = ipInterface;
    }

    public ServerSocket createServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 50, ipInterface);
        } catch (Exception e) {
            System.out.println(e);
        }
        return (serverSocket);
    }
    public Socket createSocket(String dummy, int port) throws IOException {
        return (new Socket(ipInterface, port));
    }
    public boolean equals(Object that) {
        return (that != null && that.getClass() == this.getClass());
    }
}

