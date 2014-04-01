package net.es.nsi.cli.cmd;


import net.es.nsi.cli.client.BusUtil;
import net.es.nsi.cli.client.ProviderPortHolder;
import net.es.nsi.cli.config.AuthType;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.client.ClientUtil;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QueryType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ReservationRequestCriteriaType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ScheduleType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.services.point2point.P2PServiceBaseType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.services.point2point.ObjectFactory;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.services.types.DirectionalityType;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
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

    @CliAvailabilityIndicator({"nsi wait"})
    public boolean canWait() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;

        return (NsiCliState.getInstance().isNsiAvailable());
    }


    @CliAvailabilityIndicator({"nsi clear"})
    public boolean canClear() {
        if (!haveProfiles()) return false;

        if (!haveConnectionId()) return false;
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

    @CliAvailabilityIndicator({"nsi terminate"})
    public boolean canTerminate() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (!haveConnectionId()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        return true;
    }

    @CliAvailabilityIndicator({"nsi query"})
    public boolean canQuery() {
        if (!haveProfiles()) return false;
        if (!haveListener()) return false;
        if (override) return true;
        if (!NsiCliState.getInstance().isNsiAvailable()) return false;
        return true;
    }

    @CliCommand(value = "nsi wait", help = "wait until a callback message has arrived")
    public String nsi_wait(
            @CliOption(key = { "type" }, mandatory = true, help = "the callback message type") final NsiCallbackMessageEnum callback,
            @CliOption(key = { "maxwait" }, mandatory = false, help = "max sec to wait (default: 120 sec)") final Integer maxWait
            ) {
        String out = "";
        boolean timeout = false;
        Long nowMillis = (new Date()).getTime();

        Long until = nowMillis + 120*1000;
        if (maxWait != null) {
            until = nowMillis + maxWait*1000;
        }

        Long sleepTime = 100L;
        String connId = NsiCliState.getInstance().getConnectionId();

        while (!gotCallback(connId, callback) && !timeout) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return e.getMessage();
            }
            nowMillis = (new Date()).getTime();
            if (nowMillis > until) {
                timeout = true;
            }
        }
        if (timeout || !gotCallback(connId, callback)) {
            out += "never got received callback "+callback+" for connectionId: "+connId;

        } else {
            out += "received callback "+callback+" for connectionId: "+connId;
        }


        return out;
    }
    private boolean gotCallback(String connectionId, NsiCallbackMessageEnum callback) {
        switch (callback) {

            case COMMIT_CONFIRMED:
                if (NsiCliState.getInstance().isCommitted(connectionId)) {
                    return true;
                }
                break;
            case RESERVE_CONFIRMED:
                if (NsiCliState.getInstance().isConfirmed(connectionId)) {
                    return true;
                }
                break;
            case PROVISION_CONFIRMED:
                if (NsiCliState.getInstance().isProvisioned(connectionId)) {
                    return true;
                }
                break;
        }
        return false;

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

        if (connectionId != null && !connectionId.isEmpty()) {
            NsiCliState.getInstance().setConfirmed(connectionId, false);
            NsiCliState.getInstance().setCommitted(connectionId, false);
        }


        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = null;
        try {
            header = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

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
        P2PServiceBaseType p2pType = new P2PServiceBaseType();
        ObjectFactory objFactory = new ObjectFactory();
        rct.getAny().add(objFactory.createP2Ps(p2pType));



        p2pType.setSourceSTP(rp.getSrcStp());
        p2pType.setDestSTP(rp.getDstStp());

        p2pType.setCapacity(rp.getBandwidth());
        p2pType.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        p2pType.setSymmetricPath(true);

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
        CommonHeaderType header = null;
        try {
            header = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

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
        CommonHeaderType header = null;
        try {
            header = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

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
        CommonHeaderType header = null;
        try {
            header = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

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
        CommonHeaderType header = null;
        try {
            header = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

        try {
            port.release(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted release for [" + connectionId + "]";

        return out;
    }


    @CliCommand(value = "nsi clear", help = "clear the connection id, set version to 0")
    public String clear() {

        NsiCliState.getInstance().getResvProfile().setVersion(0);
        NsiCliState.getInstance().setConnectionId(null);
        return "";
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
        CommonHeaderType header = null;
        try {
            header = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

        try {
            port.terminate(connectionId, header, outHolder);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        out += "Submitted terminate for [" + connectionId + "]";

        return out;
    }

    @CliCommand(value = "nsi query", help = "query ")
    public String query(
            @CliOption(key = { "c" }, mandatory = false, help = "connectionId") final String inConnId,
            @CliOption(key = { "g" }, mandatory = false, help = "gri") final String inGri,
            @CliOption(key = { "mode" }, mandatory = true, help = "the query mode") final NsiQueryModeEnum queryMode) {
        String out = "";
        String connectionId = inConnId;
        if (connectionId == null || connectionId.isEmpty()) {
            connectionId = NsiCliState.getInstance().getConnectionId();
        }
        if (connectionId == null || connectionId.isEmpty()) {
            if (inGri == null || inGri.isEmpty()) {
                return "must specify connection id or gri";
            }
        }
        QueryType queryReq = new QueryType();
        out += "query params: ";
        if (connectionId != null && !connectionId.isEmpty()) {
            out += "connectionId: ["+connectionId+"] ";
            queryReq.getConnectionId().add(connectionId);
        }
        if (inGri != null && !inGri.isEmpty()) {
            out += "gri: ["+inGri+"] ";
            queryReq.getGlobalReservationId().add(inGri);
        }
        out += "\n";


        ConnectionProviderPort port = getPort();
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        try {
            outHolder.value = makeHeader();
        } catch (CliInternalException e) {
            e.printStackTrace();
        }

        try {
            switch (queryMode) {
                case RECURSIVE:
                    port.queryRecursive(queryReq, outHolder);
                    break;
                case SUMMARY:
                    port.querySummary(queryReq, outHolder);
                    break;
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        return out;
    }


    private CommonHeaderType makeHeader() throws CliInternalException {
        CommonHeaderType header = new CommonHeaderType();
        if (NsiCliState.getInstance().isListenerStarted()) {
            ProviderProfile pp = NsiCliState.getInstance().getProvProfile();
            RequesterProfile rp = NsiCliState.getInstance().getRequesterProfile();

            String providerNsa = pp.getProviderNSA();
            String protocolV = pp.getProtocolVersion();
            String requesterId = rp.getRequesterId();
            String externalIp;
            try {
                externalIp  = getIpAddress();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new CliInternalException(ex.getMessage());
            }
            String replyTo = "";

            String url = rp.getUrl();
            if (url.startsWith("http://")) {
                replyTo = "http://";
                url = url.replace("http://", "");
            } else if (url.startsWith("https://")) {
                url = url.replace("https://", "https://");

                replyTo = "https://";
            } else {
                throw new CliInternalException("requester url does not start with http(s)://");
            }

            String host;
            String portAndPath;
            if (url.contains(":")) {
                String[] pieces = url.split(":");
                host = pieces[0];
                portAndPath = ":"+pieces[1];
            } else {
                String[] pieces = url.split("/");
                host = pieces[0];

                portAndPath = "";
                for (int i = 1; i <= pieces.length; i++) {
                    portAndPath += "/"+pieces[i];
                }
            }
            replyTo += externalIp+portAndPath;





            header.setReplyTo(replyTo);
            header.setRequesterNSA(requesterId);
            header.setProtocolVersion(protocolV);
            header.setProviderNSA(providerNsa);
        }
        header.setCorrelationId("urn:uuid:"+UUID.randomUUID().toString());
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

    public String getIpAddress() throws IOException {
        URL myIP = new URL("http://api.externalip.net/ip/");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(myIP.openStream())
        );
        return in.readLine();
    }


}
