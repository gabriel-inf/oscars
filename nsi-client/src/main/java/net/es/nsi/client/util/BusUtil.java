package net.es.nsi.client.util;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;

public class BusUtil {
    public static void prepareBus(String busFile) {
        SpringBusFactory bf = new SpringBusFactory();
        Bus bus = bf.createBus(busFile);
        SpringBusFactory.setDefaultBus(bus);
    }
}
