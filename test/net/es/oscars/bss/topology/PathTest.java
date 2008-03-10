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
@Test(groups={ "bss", "bss.topology", "path" }, dependsOnGroups={ "create" })
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
        PathDAO dao = new PathDAO(this.dbname);
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        PathElem pathElem = (PathElem) pathElemDAO.queryByParam("description",
                                 "ingress");
        Path path = (Path) dao.queryByParam("pathElemId", pathElem.getId()); 
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
