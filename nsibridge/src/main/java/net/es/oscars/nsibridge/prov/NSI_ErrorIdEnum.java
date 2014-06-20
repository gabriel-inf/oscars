package net.es.oscars.nsibridge.prov;

public enum NSI_ErrorIdEnum {
    NRM_ERROR ("00501");

    private String code;
    private NSI_ErrorIdEnum(String code) {
        this.code = code;
    }
    public String toString() {
        return code;
    }

}
