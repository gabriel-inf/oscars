package net.es.oscars.pss.sw.junos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.TemplateConfigGen;
import net.es.oscars.pss.eompls.EoMPLSUtils;
import net.es.oscars.pss.impl.SDNNameGenerator;

public class SWJunosConfigGen extends TemplateConfigGen {
    private Logger log;
    private static SWJunosConfigGen instance;


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String generateL2Setup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "sw-junos-setup.txt";
        String config = "";


        return config;
    }

    public String generateL2Teardown(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }

    public String generateL2Status(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }

    
    

    public static SWJunosConfigGen getInstance() {
        if (instance == null) {
            instance = new SWJunosConfigGen();
        }
        return instance;
    }

    private SWJunosConfigGen() {
        this.log = Logger.getLogger(this.getClass());
    }


}
