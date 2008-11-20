package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in PathDAO.java, which requires a working
 *     Path.java and Path.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "path" }, dependsOnGroups={ "create" })
public class PathTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathQuery() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent", CommonParams.getPathIdentifier());
        String sql = "select * from paths p " +
                     "inner join pathElems pe on p.id = pe.pathId " +
                     "inner join links l on pe.linkId = l.id " +
                     "where l.id = ?";

        Path path = (Path) this.sf.getCurrentSession().createSQLQuery(sql)
                                                    .addEntity(Path.class)
                                                    .setInteger(0, link.getId())
                                                    .setMaxResults(1)
                                                    .uniqueResult();
        this.sf.getCurrentSession().getTransaction().commit();
        assert path != null;
    }

  @Test
    public void pathList() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        List<Path> paths = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !paths.isEmpty();
    }
}
