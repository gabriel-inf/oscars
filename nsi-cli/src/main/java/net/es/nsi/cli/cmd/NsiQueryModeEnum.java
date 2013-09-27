package net.es.nsi.cli.cmd;

public enum NsiQueryModeEnum {
    SUMMARY("SUMMARY"),
    RECURSIVE("RECURSIVE");

    private String type;

    private NsiQueryModeEnum(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }
}
