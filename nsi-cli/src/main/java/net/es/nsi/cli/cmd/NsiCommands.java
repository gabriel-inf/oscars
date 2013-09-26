package net.es.nsi.cli.cmd;


import net.es.nsi.cli.client.BusUtil;
import net.es.nsi.cli.client.ProviderPortHolder;
import net.es.nsi.cli.config.AuthType;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.ResvProfile;
import net.es.oscars.nsibridge.client.ClientUtil;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationRequestCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ScheduleType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.EthernetVlanType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.DirectionalityType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.StpType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.UUID;


@Component
public class NsiCommands implements CommandMarker {
    private static final Logger log = Logger.getLogger(NsiCommands.class);
    private boolean override = false;

    private boolean haveListener() {
        return NsiCliState.getInstance().isListenerStarted();
    }

    private boolean haveProfiles () {
        ResvProfile resvProfile = NsiCliState.getInstance().getResvProfile();
        ProviderProfile provProfile = NsiCliState.getInstance().getProvProfile();
        RequesterProfile requesterProfile = NsiCliState.getInstance().getRequesterProfile();
        if (requesterProfile == null) return false;
        if (resvProfile == null) return false;
        if (provProfile == null) return false;
        // log.debug("profiles ok");
        return true;
    }

    private boolean haveConnectionId() {
        String connectionId = NsiCliState.getInstance().getConnectionId();
        if (connectionId == null || connectionId.isEmpty()) return false;
        // log.debug("have connectionId: "+connectionId);
        return true;
    }

    @CliAvailabilityIndicator({"nsi reserve"})
    public boolean canReserve() {
        if (override) return true;
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        return (NsiCliState.getInstance().isNsiAvailable());
    }

    @CliAvailabilityIndicator({"nsi commit"})
    public boolean canCommit() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        if (!NsiCliState.getInstance().isConfirmed(NsiCliState.getInstance().getConnectionId())) return false;
        return true;
    }

    @CliAvailabilityIndicator({"nsi provision"})
    public boolean canProvision() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        if (!NsiCliState.getInstance().isCommitted(NsiCliState.getInstance().getConnectionId())) return false;
        if (NsiCliState.getInstance().isProvisioned(NsiCliState.getInstance().getConnectionId())) return false;
        return true;
    }

    @CliAvailabilityIndicator({"nsi release"})
    public boolean canRelease() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        if (!NsiCliState.getInstance().isProvisioned(NsiCliState.getInstance().getConnectionId())) return false;
        return true;
    }

    @CliAvailabilityIndicator({"nsi abort"})
    public boolean canAbort() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        if (!NsiCliState.getInstance().isProvisioned(NsiCliState.getInstance().getConnectionId())) return false;
        return true;
    }

    @CliAvailabilityIndicator({"nsi terminate", "nsi query"})
    public boolean canOther() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        return true;
    }


    @CliCommand(value = "nsi help", help = "display help")
    public String nsi_help() {
        String help = "";
        help += "Prerequisites:\n";
        help += "==============\n";
        help += "'nsi' commands require:\n";
        help += " - a current provider profile (see 'prov help'):\n";
        help += " - a current reservation profile (see 'resv help'):\n";
        help += " - a current requester profile (see 'req help'):\n";
        help += " - the callback listener to be started (see 'listener help'):\n";
        help += "\n";
        help += "Normal operation:\n";
        help += "================\n";
        help += "1. use 'nsi reserve' to submit a connection reservation.\n";
        help += "2. when the reservation is confirmed, you can use 'nsi commit' or 'nsi abort'\n";
        help += "3. when the commit is confirmed, you can use 'nsi provision'\n";
        help += "4. when the provision is confirmed, you can use 'nsi release'.\n";
        help += "at any time, use 'nsi terminate' to terminate the reservation.\n";
        help += "\n";
        help += "Override mode (advanced):\n";
        help += "========================\n";
        help += "1. use 'nsi override' to enable override mode,\n";
        help += "2. use 'nsi set --c <connectionId>' to set the connectionId,\n";
        help += "3. now all nsi commands: 'nsi commit | abort | provision | release | terminate ' are allowed.\n";

        return help;
    }

    @CliAvailabilityIndicator({"nsi no override", "nsi set"})
    public boolean overrideEnabled() {
        return override;
    }



    @CliCommand(value = "nsi override", help = "enable override mode")
    public String nsi_override() {
        override = true;
        return "override set";
    }

    @CliCommand(value = "nsi no override", help = "disable override mode")
    public String nsi_no_override() {
        override = false;
        return "override unset";
    }


    @CliCommand(value = "nsi set", help = "set nsi connection id")
    public String nsi_set(
            @CliOption(key = { "c" }, mandatory = true, help = "the connectionId") final String connectionId) {
        NsiCliState.getInstance().setConnectionId(connectionId);
        return "Set connection id to [" + connectionId + "]";
    }

    @CliCommand(value = "nsi reserve", help = "reserve a connection")
    public String reserve(
            @CliOption(key = { "c" }, mandatory = false, help = "a connectionId (optional)") final String inConnectionId) {
        String out = "";
        String connectionId;

        if (inConnectionId != null) {
            connectionId = inConnectionId;
            NsiCliState.getInstance().setConnectionId(inConnectionId);
            out += "current connection id set to "+inConnectionId;
        } else {
            connectionId = NsiCliState.getInstance().getConnectionId();
            out += "using current connection id: "+connectionId;
        }


        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = makeHeader();

        ProviderProfile pp = NsiCliState.getInstance().getProvProfile();
        String serviceType = pp.getServiceType();

        ResvProfile rp = NsiCliState.getInstance().getResvProfile();
        if (rp == null) return "no current reservation profile";

        String gri = rp.getGri();
        String description = rp.getDescription();
        ReservationRequestCriteriaType rct = new ReservationRequestCriteriaType();
        Holder<String> connHolder = new Holder();
        connHolder.value = connectionId;

        rct.setVersion(rp.getVersion());
        rct.setServiceType(serviceType);

        try {
            ScheduleType st = new ScheduleType();
            GregorianCalendar stCal = new GregorianCalendar();
            stCal.setTime(rp.getStartTime());
            st.setStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(stCal));

            GregorianCalendar etCal = new GregorianCalendar();
            etCal.setTime(rp.getEndTime());
            st.setEndTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(etCal));
            rct.setSchedule(st);

        } catch (DatatypeConfigurationException ex) {
            ex.printStackTrace();
            return "error";
        }
        EthernetVlanType evType = new EthernetVlanType();
        ObjectFactory objFactory = new ObjectFactory();
        rct.getAny().add(objFactory.createEvts(evType));
        StpType sourceSTP = new StpType();
        sourceSTP.setNetworkId(rp.getSrcNet());
        sourceSTP.setLocalId(rp.getSrcStp());

        StpType destSTP = new StpType();
        destSTP.setNetworkId(rp.getDstNet());
        destSTP.setLocalId(rp.getDstStp());

        evType.setSourceSTP(sourceSTP);
        evType.setDestSTP(destSTP);

        evType.setSourceVLAN(rp.getSrcVlan());
        evType.setDestVLAN(rp.getDstVlan());

        evType.setCapacity(rp.getBandwidth());
        evType.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        evType.setSymmetricPath(true);


        try {
            port.reserve(connHolder, gri, description, rct, header, outHolder);
            String outConnId = connHolder.value;

            out += "\nSubmitted reserve, new connectionId = "+outConnId;
            NsiCliState.getInstance().setConnectionId(outConnId);


            return out;

        } catch (ServiceException ex) {
            ex.printStackTrace();
            return "failed with: "+ex.getMessage();
        }

    }


    @CliCommand(value = "nsi commit", help = "commit a connection")
    public String commit(
            @CliOption(key = { "c" }, mandatory = false, help = "the connectionId") final String inConnectionId) {
        String connectionId;
        String out = "";
        if (inConnectionId != null) {
            connectionId = inConnectionId;
            NsiCliState.getInstance().setConnectionId(inConnectionId);
            out += "current connection id set to "+inConnectionId;
        } else {
            connectionId = NsiCliState.getInstance().getConnectionId();
        }
        if (connectionId == null) {
            return "null current connection id and none specified";
        }

        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = makeHeader();

        try {
            port.reserveCommit(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted reserveCommit for [" + connectionId + "]";

        return out;
    }

    @CliCommand(value = "nsi abort", help = "abort a connection")
    public String abort(
            @CliOption(key = { "c" }, mandatory = false, help = "the connectionId") final String inConnectionId) {

        String connectionId;
        String out = "";
        if (inConnectionId != null) {
            connectionId = inConnectionId;
            NsiCliState.getInstance().setConnectionId(inConnectionId);
            out += "current connection id set to "+inConnectionId;
        } else {
            connectionId = NsiCliState.getInstance().getConnectionId();
        }
        if (connectionId == null) {
            return "null current connection id and none specified";
        }

        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = makeHeader();

        try {
            port.reserveAbort(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted reserveAbort for [" + connectionId + "]";

        return out;
    }

    @CliCommand(value = "nsi provision", help = "provision a reservation")
    public String provision(
            @CliOption(key = { "c" }, mandatory = false, help = "the connectionId") final String inConnectionId) {

        String connectionId;
        String out = "";
        if (inConnectionId != null) {
            connectionId = inConnectionId;
            NsiCliState.getInstance().setConnectionId(inConnectionId);
            out += "current connection id set to "+inConnectionId;
        } else {
            connectionId = NsiCliState.getInstance().getConnectionId();
        }
        if (connectionId == null) {
            return "null current connection id and none specified";
        }

        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = makeHeader();

        try {
            port.provision(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted provision for [" + connectionId + "]";

        return out;
    }

    @CliCommand(value = "nsi release", help = "release a reservation")
    public String release(
            @CliOption(key = { "c" }, mandatory = false, help = "the connectionId") final String inConnectionId) {

        String connectionId;
        String out = "";
        if (inConnectionId != null) {
            connectionId = inConnectionId;
            NsiCliState.getInstance().setConnectionId(inConnectionId);
            out += "current connection id set to "+inConnectionId;
        } else {
            connectionId = NsiCliState.getInstance().getConnectionId();
        }
        if (connectionId == null) {
            return "null current connection id and none specified";
        }

        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = makeHeader();

        try {
            port.release(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted release for [" + connectionId + "]";

        return out;
    }

    @CliCommand(value = "nsi terminate", help = "terminate a reservation")
    public String terminate(
            @CliOption(key = { "c" }, mandatory = false, help = "the connectionId") final String inConnectionId) {

        String connectionId;
        String out = "";
        if (inConnectionId != null) {
            connectionId = inConnectionId;
            NsiCliState.getInstance().setConnectionId(inConnectionId);
            out += "current connection id set to "+inConnectionId;
        } else {
            connectionId = NsiCliState.getInstance().getConnectionId();
        }
        if (connectionId == null) {
            return "null current connection id and none specified";
        }

        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = makeHeader();

        try {
            port.terminate(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted terminate for [" + connectionId + "]";

        return out;
    }

    @CliCommand(value = "nsi query", help = "query a reservation")
    public String query(
            @CliOption(key = { "c" }, mandatory = false, help = "the connectionId") final String connectionId) {

        return "Unimplemented";
    }


    private CommonHeaderType makeHeader() {
        CommonHeaderType header = new CommonHeaderType();
        if (NsiCliState.getInstance().isListenerStarted()) {
            String replyTo = NsiCliState.getInstance().getRequesterProfile().getUrl();
            header.setReplyTo(replyTo);
            String requesterId = NsiCliState.getInstance().getRequesterProfile().getRequesterId();
            header.setRequesterNSA(requesterId);
        }
        header.setCorrelationId("urn:uuid"+UUID.randomUUID().toString());
        return header;
    }

    private ConnectionProviderPort getPort() {
        ProviderProfile provProf = NsiCliState.getInstance().getProvProfile();
        ProviderPortHolder pph = ProviderPortHolder.getInstance();
        BusUtil.prepareBus(provProf.getProviderServer().getBusConfig());

        try {
            URL url = new URL(provProf.getProviderServer().getUrl());
            ConnectionProviderPort port = pph.getPort(url);
            Client client = ClientProxy.getClient(port);

            if (provProf.getProviderServer().getAuth().getAuthType().equals(AuthType.BASIC)) {
                String username = provProf.getProviderServer().getAuth().getUsername();
                String password = provProf.getProviderServer().getAuth().getPassword();
                HTTPConduit http = (HTTPConduit) client.getConduit();
                http.getAuthorization().setUserName(username);
                http.getAuthorization().setPassword(password);
                http.getAuthorization().setAuthorizationType("Basic");
                // System.out.println("using HTTP-Basic user: "+username+ " pass: "+password);

            }
            return port;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }

    }

}
