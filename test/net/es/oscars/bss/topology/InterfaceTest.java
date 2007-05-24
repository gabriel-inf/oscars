package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the interfaces table, which requires a working
 *     Interface.java and Interface.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "interface" }, dependsOnGroups={ "router" })
public class InterfaceTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss.topology", true);
        this.dbname = "bss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
    public void interfaceCreate() {
        this.sf.getCurrentSession().beginTransaction();
        InterfaceDAO dao = new InterfaceDAO(this.dbname);
        String routerName = this.props.getProperty("routerName");
        RouterDAO routerDAO = new RouterDAO(this.dbname);
        Router router = routerDAO.queryByParam("name", routerName);

        Interface xface = new Interface();
        xface.setValid(true);
        xface.setSnmpIndex(0);
        xface.setSpeed(10000000L);
        xface.setDescription(this.props.getProperty("xfaceDescription"));
        xface.setRouter(router);
        dao.create(xface);
        this.sf.getCurrentSession().getTransaction().commit();
        assert xface != null;
    }

  @Test(dependsOnMethods={ "interfaceCreate" })
    public void interfaceQuery() {
        this.sf.getCurrentSession().beginTransaction();
        InterfaceDAO dao = new InterfaceDAO(this.dbname);
        String description = this.props.getProperty("xfaceDescription");
        Interface xface = (Interface)
            dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert xface != null;
    }

  @Test(dependsOnMethods={ "interfaceCreate" })
    public void interfaceList() {
        this.sf.getCurrentSession().beginTransaction();
        InterfaceDAO dao = new InterfaceDAO(this.dbname);
        List<Interface> interfaces = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !interfaces.isEmpty();
    }
}
