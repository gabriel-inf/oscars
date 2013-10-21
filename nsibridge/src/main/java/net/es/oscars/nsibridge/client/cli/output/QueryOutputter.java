package net.es.oscars.nsibridge.client.cli.output;

import java.util.List;

import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QueryRecursiveResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;

/**
 * Interface for outputting query results
 *
 */
public interface QueryOutputter {
    
    public void outputSummary(List<QuerySummaryResultType> results);
    
    public void outputRecursive(List<QueryRecursiveResultType> results);
    
    public void outputFailed(ServiceExceptionType serviceException);

}
