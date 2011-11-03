package net.es.oscars.pss.eompls.alu;

public class ALUNameGenerator {
    
    private static ALUNameGenerator instance;
    private ALUNameGenerator() {
    }
    public static ALUNameGenerator getInstance() {
        if (instance == null) {
            instance = new ALUNameGenerator();
        }
        return instance;
    }

    public String getLSPName(String gri) {
        return numbers(gri+"_lsp");
    }

    public String getPathName(String gri) {
        return numbers(gri+"_path");
    }

    private String numbers(String gri) {

        String circuitStr = gri;
        circuitStr = circuitStr.replaceAll("[^0-9]+", "");
        
        return circuitStr;
    }
    

    public String getEpipeId(String gri) {
        return numbers(gri);
    }

    public String getQosId(String gri) {
        return numbers(gri);
    }

    public String getSdpId(String gri) {
        return numbers(gri);
    }

    
}
