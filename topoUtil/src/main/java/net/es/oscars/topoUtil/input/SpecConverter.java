package net.es.oscars.topoUtil.input;

import com.google.common.net.InetAddresses;
import net.es.oscars.topoUtil.beans.*;
import net.es.oscars.topoUtil.beans.spec.*;
import net.es.oscars.topoUtil.util.IfceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

public class SpecConverter {
    static final Logger LOG = LoggerFactory.getLogger(SpecConverter.class);
    public static Network fromSpec(NetworkSpec spec) throws VlanFormatException {
        Network net = new Network();
        net.setDomainId(spec.getDomainId());
        net.setIdcId(spec.getIdcId());
        net.setTopologyId(spec.getTopologyId());
        for (DeviceSpec ds : spec.getDevices()){
            LOG.debug("- "+ds.getName());
            Device dev = new Device();
            dev.setName(ds.getName());
            dev.setLoopback(ds.getLoopback());
            dev.setModel(ds.getModel());
            net.getDevices().add(dev);
            for (PortSpec ps : ds.getPorts()) {
                List<String> portNames = IfceIdentifier.explodeIdentifier(ps.getName());
                for (String portName : portNames) {
                    LOG.debug("-- "+portName);
                    Port port = new Port();
                    port.setName(portName);
                    port.setCapacity(ps.getCapacity());
                    port.setReservable(ps.getReservable());
                    dev.getPorts().add(port);

                    for (CustomerLinkSpec cs : ps.getCustomerLinks()) {
                        LOG.debug("--- "+cs.getName());
                        CustomerLink cl = new CustomerLink();
                        cl.setName(cs.getName());
                        VlanInfo vf = new VlanInfo(cs.getVlanRangeExpr());
                        cl.setVlanInfo(vf);
                        port.getCustomerLinks().add(cl);
                    }

                    for (PeeringLinkSpec pls : ps.getPeeringLinks()) {
                        LOG.debug("--- "+pls.getName());
                        PeeringLink pl = new PeeringLink();
                        pl.setName(pls.getName());
                        VlanInfo vf = new VlanInfo(pls.getVlanRangeExpr());
                        pl.setVlanInfo(vf);
                        pl.setRemote(pls.getRemote());
                        port.getPeeringLinks().add(pl);
                    }

                    for (EthInternalLinkSpec es : ps.getEthLinks()) {
                        LOG.debug("--- "+es.getName());
                        EthInternalLink el = new EthInternalLink();
                        el.setName(es.getName());
                        el.setMetric(es.getMetric());
                        el.setRemote(es.getRemote());
                        VlanInfo vf = new VlanInfo(es.getVlanRangeExpr());
                        el.setVlanInfo(vf);
                        port.getEthLinks().add(el);
                    }

                    for (MplsInternalLinkSpec ms : ps.getMplsLinks()) {
                        LOG.debug("--- "+ms.getName());
                        MplsInternalLink ml = new MplsInternalLink();
                        String[] parts = ms.getIpv4Expr().split("/");
                        if (parts.length == 0 || parts.length > 2) {
                            throw new IllegalArgumentException("invalid IP address expression: "+ms.getIpv4Expr());
                        }
                        String addrStr = parts[0];
                        InetAddress addr = InetAddresses.forString(addrStr);
                        if (parts.length == 2) {
                            Integer mask = Integer.valueOf(parts[1]);
                            ml.setMask(mask);
                        }

                        ml.setAddress(addr);
                        ml.setMetric(ms.getMetric());
                        ml.setRemote(ms.getRemote());
                        ml.setName(ms.getName());

                        port.getMplsLinks().add(ml);
                    }
                }
            }
        }




        return net;


    }
}
