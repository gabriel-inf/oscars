package net.es.oscars.nsibridge.prov;

import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.types.ServiceExceptionType;

public class ServiceExceptionUtil {
    public static ServiceException makeException(String text, String connectionId, String errorId) {

        String nsaId = NSI_OSCARS_Translation.findNsaId();

        ServiceException ex = new ServiceException();
        ServiceExceptionType set = new ServiceExceptionType();
        set.setConnectionId(connectionId);
        set.setErrorId(errorId);
        set.setNsaId(nsaId);
        set.setText(text);


        return ex;
    }
}
