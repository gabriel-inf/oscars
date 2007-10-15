package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the links table, which requires a working
 *     Link.java and Link.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "link" }, dependsOnGroups={ "create" })
public class LinkTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void linkQuery() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO dao = new LinkDAO(this.dbname);
        String topologyIdent = "test suite";
        Link link = (Link)
            dao.queryByParam("topologyIdent", topologyIdent);
        this.sf.getCurrentSession().getTransaction().commit();
        assert link != null;
    }

  @Test
    public void linkList() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO dao = new LinkDAO(this.dbname);
        List<Link> links = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !links.isEmpty();
    }
}
