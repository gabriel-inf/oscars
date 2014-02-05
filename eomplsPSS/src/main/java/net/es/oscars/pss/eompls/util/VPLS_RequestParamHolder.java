package net.es.oscars.pss.eompls.util;

import java.util.HashMap;

public class VPLS_RequestParamHolder {
    private static VPLS_RequestParamHolder instance;
    private VPLS_RequestParamHolder() {

    }
    public static VPLS_RequestParamHolder getInstance() {
        if (instance == null) instance = new VPLS_RequestParamHolder();
        return instance;
    }
    HashMap<String, VPLS_RequestParams> requestParams = new HashMap<String, VPLS_RequestParams>();

    public HashMap<String, VPLS_RequestParams> getRequestParams() {
        return requestParams;
    }
}
