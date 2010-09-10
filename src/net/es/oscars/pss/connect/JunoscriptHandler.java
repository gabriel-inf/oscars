package net.es.oscars.pss.connect;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSConnectorConfigBean;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSEdgeType;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Logger;

public class JunoscriptHandler {
    public static void command(Reservation resv, PSSDirection direction, String command, Logger log) 
            throws PSSException {
        
        Path localPath = null;
        try {
            localPath = resv.getPath(PathType.LOCAL);
            if (localPath == null) {
                throw new PSSException("No local path set");
            }
        } catch (BSSException e) {
            throw new PSSException("Can't find local path "+e.getMessage());
        }
        
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        
        PSSHandlerConfigBean hc = pc.getHandlerConfig();
        
        if (hc.isLogConfig()) {
            log.info("config for router :\n\n"+command);
        }
        
        PSSEdgeType edgeType = PSSEdgeType.A;
        if (direction.equals(PSSDirection.Z_TO_A)) {
            edgeType = PSSEdgeType.Z;
        }
        
        Map<PSSEdgeType, String> edges = PathUtils.getEdgeNodeAddresses(localPath);
        String address = edges.get(edgeType);
        
        PSSConnectorConfigBean cc = pc.getConnectorConfig();

        if (hc.isStubMode()) {
            log.debug("stub mode, no command sent");
            /*
            Random r = new Random();
            if (r.nextFloat() < 0.5) {
                log.debug("simulating failure in stub mode");
                throw new PSSException("simulated failure");
            }
            */
        } else {
            JunoscriptConnector conn = new JunoscriptConnector(cc, address);
            SAXBuilder sb = new SAXBuilder();
            Document doc = null;
            try {
                doc = sb.build(new StringReader(command));
                conn.sendCommand(doc);
            } catch (JDOMException e) {
                throw new PSSException("Can't send command: "+e.getMessage());
            } catch (IOException e) {
                throw new PSSException("Can't send command: "+e.getMessage());
            }
        }
    }
}
