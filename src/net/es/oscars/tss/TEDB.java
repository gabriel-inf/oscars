package net.es.oscars.tss;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

/**
 * Interface for modules that access a traffic engineering database (TEDB)
 *  
 * @author Andrew Lake (alake@internet2.edu)
 */
public interface TEDB{
    CtrlPlaneTopologyContent selectNetworkTopology(String type) throws TSSException;
    void insertNetworkTopology(CtrlPlaneTopologyContent topology) throws TSSException;
}