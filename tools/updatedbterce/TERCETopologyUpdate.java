import java.rmi.RemoteException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.*;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;
import edu.internet2.hopi.dragon.terce.ws.service.*;
import edu.internet2.hopi.dragon.terce.ws.types.tedb.*;
import org.hibernate.*;
import java.util.ArrayList;

public class TERCETopologyUpdate{
    /**
     * Gets topology from TERCE
     * @param url the TERCE URL
     * @return the topology returned by the TERCE 
     */
    public CtrlPlaneTopologyContent getTERCETopology(String url) throws RemoteException, TEDBFaultMessage{
        TERCEStub terce = new TERCEStub(url);
        SelectNetworkTopology selectTopology = new SelectNetworkTopology();
        SelectNetworkTopologyContent request = new SelectNetworkTopologyContent();
        SelectNetworkTopologyResponse response = null;
        SelectNetworkTopologyResponseContent responseContent = null;
        CtrlPlaneTopologyContent topology = null;

        /* Format Request */
        request.setFrom(SelectTypes.all);
        request.setDatabase("intradomain");

        /* Send request and get response*/
        selectTopology.setSelectNetworkTopology(request);
        response = terce.selectNetworkTopology(selectTopology);
        responseContent = response.getSelectNetworkTopologyResponse();
        topology = responseContent.getTopology();

        return topology;
    }

    public static void main(String[] args){
        TERCETopologyUpdate topoUpdate = new TERCETopologyUpdate();

        if(args.length < 1){
            System.err.println("Must specify TERCE url as parameter");
            System.exit(1);
        }

        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try{
            CtrlPlaneTopologyContent topology = topoUpdate.getTERCETopology(args[0]);
            TopologyAxis2Importer importer = new TopologyAxis2Importer("bss");
            importer.updateDatabase(topology);
            bss.getTransaction().commit();
            System.out.println("Complete.");
        }catch(TEDBFaultMessage e){
            System.out.println(e.getFaultMessage().getMsg());
            bss.getTransaction().rollback();
        }catch(Exception e){
            e.printStackTrace();
            bss.getTransaction().rollback();
        }

    }
}