package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the links table, which requires a working
 *     Link.java and Link.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss", "bss.topology", "link" }, dependsOnGroups={ "create" })
public class LinkTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void linkQuery() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO dao = new LinkDAO(this.dbname);
        Link link = (Link)
            dao.queryByParam("topologyIdent", CommonParams.getIdentifier());
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
