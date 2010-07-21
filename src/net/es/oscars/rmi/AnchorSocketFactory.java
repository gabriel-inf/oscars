package net.es.oscars.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * RMISocketFactory implementation that causes the RMI registry and server
 * to listen only on the specified interface. When interface is set to 127.0.0.1
 * allows connections only from the local host
 *
 * @author Mary Thompson ESNet
 * @author Evangelos Chaniotakis ESNet
 *
 */
public class AnchorSocketFactory extends RMISocketFactory implements Serializable {
    private static final long serialVersionUID = 1L;
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

