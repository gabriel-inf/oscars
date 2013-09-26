package net.es.nsi.cli.cmd;

public enum NsiCallback {
    RESERVE_CONFIRMED("RESERVE_CONFIRMED"),
    COMMIT_CONFIRMED("COMMIT_CONFIRMED"),
    PROVISION_CONFIRMED("PROVISION_CONFIRMED");

    private String type;

    private NsiCallback(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }
}
