package net.es.oscars.nsibridge.config;


public class OscarsConfig {
    protected String userDN = "";
    protected String issuerDN = "";
    protected boolean stub = false;

    public OscarsConfig(){

    };

    public String toString() {
        String out;
        out = "userDN: "+userDN+"\n";
        out += "issuerDN: "+issuerDN+"\n";
        out += "stub: "+stub+"\n";
        return out;
    }

    public boolean isStub() {
        return stub;
    }

    public void setStub(boolean stub) {
        this.stub = stub;
    }


    public String getUserDN() {
        return userDN;
    }

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    public String getIssuerDN() {
        return issuerDN;
    }

    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }
}
