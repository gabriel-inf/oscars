package net.es.oscars.pss.common;

public class PSSConfigProvider {
    private static PSSConfigProvider instance;
    private PSSConfigProvider() {
        
    }
    private PSSHandlerConfigBean handlerConfig;
    private PSSConnectorConfigBean connectorConfig;
    
    public static PSSConfigProvider getInstance() {
        if (instance == null) {
            instance = new PSSConfigProvider();
        }
        return instance;
        
    }

    public void setHandlerConfig(PSSHandlerConfigBean handlerConfig) {
        this.handlerConfig = handlerConfig;
    }

    public PSSHandlerConfigBean getHandlerConfig() {
        return handlerConfig;
    }

    public PSSConnectorConfigBean getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(PSSConnectorConfigBean connectorConfig) {
        this.connectorConfig = connectorConfig;
    }


}
