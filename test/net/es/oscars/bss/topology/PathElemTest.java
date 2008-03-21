package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the pathElems table, which requires a working
 *     PathElem.java and PathElem.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "pathElem" }, dependsOnGroups={ "create" })
public class PathElemTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathElemQuery() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent", CommonParams.getPathIdentifier());
        PathElemDAO dao = new PathElemDAO(this.dbname);
        PathElem pathElem = (PathElem)
            dao.queryByParam("linkId", link.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElem != null;
    }

  @Test
    public void pathElemList() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO dao = new PathElemDAO(this.dbname);
        List<PathElem> pathElems = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !pathElems.isEmpty();
    }
}
