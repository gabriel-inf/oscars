package net.es.oscars.client.improved.list;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;

import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ConfigHelper;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.Layer3Info;
import net.es.oscars.wsdlTypes.ListReply;
import net.es.oscars.wsdlTypes.ListRequest;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.wsdlTypes.ResDetails;
import net.es.oscars.wsdlTypes.VlanTag;

public class ListClient extends ImprovedClient {

    public static final String DEFAULT_CONFIG_FILE = "list.yaml";

    private int numResults;
    private ArrayList<String> statuses;
    private String description = "";
    private ArrayList<String> linkIds;
    private ArrayList<String> vlanIds;
    private String src;
    private String dst;
    private String betweenA;
    private String betweenZ;
    private String level;

    @SuppressWarnings("unchecked")
    public void configure() {
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
        }

        ConfigHelper cfg = ConfigHelper.getInstance();
        config = cfg.getConfiguration(this.configFile);


        Map filters = (Map) config.get("filters");
        linkIds = (ArrayList<String>) filters.get("by-link");
        vlanIds = (ArrayList<String>) filters.get("by-vlan");
        statuses = (ArrayList<String>) filters.get("by-status");
        numResults = (Integer) filters.get("numResults");
        description = (String) filters.get("by-descr");
        src = (String) filters.get("by-src");
        dst = (String) filters.get("by-dst");
        level = (String) filters.get("by-level");

        Map between = (Map) filters.get("between");
        if (between != null) {
            betweenA = (String) between.get("betweenA");
            betweenZ = (String) between.get("betweenZ");
        }
    }

    @SuppressWarnings("unchecked")
    public void setUserChoices(Map<String, String> userChoices) {
        if (userChoices.containsKey("vlans")) {
            String[] vlans = userChoices.get("vlans").trim().split(",");
            for (int i=0; i < vlans.length; i++) {
                String v = vlans[i].trim();
                if (!v.equals("")) {
                    vlanIds.add(v);
                }
            }
        }
        if (userChoices.containsKey("statuses")) {
            String[] statusArr = userChoices.get("statuses").trim().split(",");
            for (int i=0; i < statusArr.length; i++) {
                String s = statusArr[i].trim();
                if (!s.equals("")) {
                    statuses.add(s);
                }
            }
        }
        if (userChoices.containsKey("linkIds")) {
            String[] links = userChoices.get("linkIds").trim().split(",");
            for (int i=0; i < links.length; i++) {
                String l = links[i].trim();
                if (!l.equals("")) {
                    linkIds.add(l);
                }
            }
        }
        if (userChoices.containsKey("numResults")) {
            try {
                numResults = Integer.parseInt(userChoices.get("numResults"));
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number format");
                System.exit(1);
            }
        }
        if (userChoices.containsKey("description")) {
            description = userChoices.get("description");
        }
    }


    public ListRequest formRequest() {
        ListRequest listReq = new ListRequest();

        if (statuses != null && !statuses.isEmpty()) {
            for (String status : statuses) {
                status = status.trim();
                if (!status.equals("")) {
                    listReq.addResStatus(status);
                }
            }
        }

        if (description != null) {
            String desc = description.trim();
            if (!desc.equals("")) {
                listReq.setDescription(desc);
            }
        }

        if (linkIds != null && !linkIds.isEmpty()) {
            for (String linkId : linkIds) {
                linkId = linkId.trim();
                if (!linkId.equals("")) {
                    listReq.addLinkId(linkId);
                }
            }
        }

        if (vlanIds != null && !vlanIds.isEmpty()) {
            for (String vlanId : vlanIds) {
                vlanId = vlanId.trim();
                if (!vlanId.equals("")) {
                    VlanTag vlanTag = new VlanTag();
                    vlanTag.setString(vlanId);
                    vlanTag.setTagged(true);
                    listReq.addVlanTag(vlanTag);
                }
            }
        }

        listReq.setResRequested(this.numResults);


        return listReq;
    }

    public ListReply performRequest(ListRequest listReq) {
        ListReply response = null;
        Client oscarsClient = new Client();

        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            response = oscarsClient.listReservations(listReq);
        } catch (AxisFault e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AAAFaultMessage e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return response;

    }

    public ResDetails[] filterResvs(ResDetails[] resvs) {
        ResDetails[] temp = new ResDetails[resvs.length];

        for (int i = 0; i < resvs.length; i++) {
            temp[i] = null;
        }

        int numFilteredResults = 0;
        for (ResDetails resv : resvs) {

            PathInfo pathInfo = resv.getPathInfo();
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            Layer3Info layer3Info = pathInfo.getLayer3Info();

            String resvSrc = "";
            if (layer2Info != null) {
                resvSrc = layer2Info.getSrcEndpoint().trim();
            } else if (layer3Info != null) {
                resvSrc = layer3Info.getSrcHost().trim();
            }
            String resvDest = "";
            if (layer2Info != null) {
                resvDest = layer2Info.getDestEndpoint().trim();
            } else if (layer3Info != null) {
                resvDest = layer3Info.getDestHost().trim();
            }

            boolean isResult = true;

            if (this.betweenA != null) {
                if (resvSrc.equals(this.betweenA) && resvDest.equals(this.betweenZ)) {
                    isResult = true;
                } else if (resvSrc.equals(this.betweenZ) && resvDest.equals(this.betweenA)) {
                    isResult = true;
                } else {
                    isResult = false;
                    // System.out.println("filterResvs: not a result because of between");
                }
            } else {
                if (this.src != null && !this.src.equals("")) {
                    if (resvSrc.equals(this.src)) {
                        isResult = true;
                    } else {
                        isResult = false;
                        // System.out.println("filterResvs: not a result because of src");
                    }
                }
                if (this.dst != null && !this.dst.equals("")) {
                    if (resvDest.equals(this.dst)) {
                        isResult = true;
                    } else {
                        isResult = false;
                        // System.out.println("filterResvs: not a result because of dst");
                    }
                }
            }
            if (isResult) {
                // System.out.println("filterResvs: a result: "+resv.getGlobalReservationId());
                temp[numFilteredResults] = resv;
                numFilteredResults++;
            }
        }


        ResDetails[] filteredResults = new ResDetails[numFilteredResults];
        for (int i = 0; i < numFilteredResults; i++) {
            filteredResults[i] = temp[i];
        }
        return filteredResults;
    }




    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }


    public String getLevel() {
        return level;
    }


    public void setLevel(String level) {
        this.level = level;
    }


    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<String> statuses) {
        this.statuses = statuses;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLinkIds() {
        return linkIds;
    }

    public void setLinkIds(ArrayList<String> linkIds) {
        this.linkIds = linkIds;
    }

    public List<String> getVlanIds() {
        return vlanIds;
    }

    public void setVlanIds(ArrayList<String> vlanIds) {
        this.vlanIds = vlanIds;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getBetweenA() {
        return betweenA;
    }

    public void setBetweenA(String betweenA) {
        this.betweenA = betweenA;
    }

    public String getBetweenZ() {
        return betweenZ;
    }

    public void setBetweenZ(String betweenZ) {
        this.betweenZ = betweenZ;
    }



}
