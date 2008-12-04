package net.es.oscars.bss;

import java.util.*;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;

/**
 * This class sets the fields for a layer 2 reservation that are
 * available before scheduling
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class CommonReservation {
    private Properties props;
    private final Long BANDWIDTH = 25000000L;  // 25 Mbps
    private final int DURATION = 240;
    // for layer 3
    private final int BURST_LIMIT = 10000000; // 10 Mbps
    private final String PROTOCOL = "UDP";
    private final String LSP_CLASS = "4";

    public CommonReservation () {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

    public void setParameters(Reservation resv, String description) {

        Long seconds = System.currentTimeMillis()/1000;
        resv.setStartTime(seconds);
        resv.setCreatedTime(seconds);
        seconds += DURATION;
        resv.setEndTime(seconds);

        resv.setBandwidth(BANDWIDTH);
        resv.setDescription(description);
        resv.setStatus("TEST");
        resv.setLogin(this.props.getProperty("login"));
    }

    public void setLayer3Parameters(Reservation resv, PathInfo pathInfo,
                                    String description) {

        this.setParameters(resv, description);

        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        layer3Info.setProtocol(PROTOCOL);
        pathInfo.setLayer3Info(layer3Info);

        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(BURST_LIMIT);
        mplsInfo.setLspClass(LSP_CLASS);
        pathInfo.setMplsInfo(mplsInfo);

    }

    public void setLayer2PathInfo(PathInfo pathInfo, String vlanTag) {
	
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(this.props.getProperty("layer2Src"));
        layer2Info.setDestEndpoint(this.props.getProperty("layer2Dest"));
        String srcVlanTag = null;
        String destVlanTag = null;
        boolean srcTagged = true;
        boolean destTagged = true;
        
        if(vlanTag == null){
            srcVlanTag = this.props.getProperty("srcVlan");
            destVlanTag = this.props.getProperty("destVlan");
            srcTagged = "1".equals(this.props.getProperty("srcVlanTagged"));
            destTagged = "1".equals(this.props.getProperty("destVlanTagged"));
        }else{
            srcVlanTag = vlanTag;
            destVlanTag = vlanTag;
        }
        
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(srcVlanTag);
        srcVtag.setTagged(srcTagged);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString(destVlanTag);
        destVtag.setTagged(destTagged);
        layer2Info.setDestVtag(destVtag);
        pathInfo.setLayer2Info(layer2Info);
    }

    public void setLayer2Parameters(Reservation resv, PathInfo pathInfo,
                                    String vlanTag, String description) {
        this.setParameters(resv, description);
        this.setLayer2PathInfo(pathInfo, vlanTag);
    }
    
    public static String getScheduledLayer2Description() {
        return "automated layer 2 test reservation for scheduling";
    }

    public static String getScheduledLayer3Description() {
        return "automated layer 3 test reservation for scheduling";
    }
}
