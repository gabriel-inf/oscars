package net.es.nsi.cli.cmd;

public enum NsiCallbackMessageEnum {
    RESERVE_CONFIRMED("RESERVE_CONFIRMED"),
    COMMIT_CONFIRMED("COMMIT_CONFIRMED"),
    PROVISION_CONFIRMED("PROVISION_CONFIRMED");

    private String type;

    private NsiCallbackMessageEnum(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }
}
