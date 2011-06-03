package net.es.oscars.eompls.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;

public class LSP {
    private Logger log = Logger.getLogger(LSP.class);
    private ArrayList<String> pathAddresses;
    private String to;
    private String from;

    public LSP(String deviceId, PathInfo pi, EoMPLSDeviceAddressResolver dar, EoMPLSIfceAddressResolver iar, boolean reverse) throws PSSException {
        pathAddresses = new ArrayList<String>();
        
        ArrayList<String> lspLinkIds = new  ArrayList<String>();
        List<CtrlPlaneHopContent> hops = pi.getPath().getHop();
        String aLinkId = hops.get(0).getLink().getId();
        String yLinkId = hops.get(hops.size()-2).getLink().getId();
        if (reverse) {
            aLinkId = hops.get(hops.size()-2).getLink().getId();
            yLinkId = hops.get(1).getLink().getId();
        }
        log.debug("alinkId: "+aLinkId);
        log.debug("ylinkId: "+yLinkId);
        if (reverse) {
            log.debug("reverse lsp hops:");
            for (int i = hops.size() - 3; i > 0 ; i -= 2) {
                String linkId = hops.get(i).getLink().getId();
                lspLinkIds.add(linkId);
                log.debug("lsp hop: "+linkId);
            }
        } else {
            log.debug("forward lsp hops:");
            for (int i = 2; i < hops.size(); i += 2) {
                String linkId = hops.get(i).getLink().getId();
                lspLinkIds.add(linkId);
                log.debug("lsp hop: "+linkId);
            }
        }
        
        String[] pathHops = new String[lspLinkIds.size()];
        for (int i = 0 ; i < lspLinkIds.size(); i++) {
            pathAddresses.add(i, iar.getIfceAddress(lspLinkIds.get(i)));
            log.debug(" "+i+": "+pathHops[i]);
        }
        
        setFrom(dar.getDeviceAddress(deviceId));
        setTo(iar.getIfceAddress(yLinkId));

    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public ArrayList<String> getPathAddresses() {
        return pathAddresses;
    }

    public void setPathAddresses(ArrayList<String> pathAddresses) {
        this.pathAddresses = pathAddresses;
    }
}
