package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the pathElemParams table, which requires a working
 *     PathElemParam.java and PathElemParam.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "pathElemParam" }, dependsOnGroups={ "create" })
public class PathElemParamTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathElemParamQuery() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent", CommonParams.getPathIdentifier());
        PathElemDAO dao = new PathElemDAO(this.dbname);
        PathElem pathElem = (PathElem)
            dao.queryByParam("linkId", link.getId());
        PathElemParamDAO peParamDAO = new PathElemParamDAO(this.dbname);
        PathElemParam pathElemParam = (PathElemParam)
            peParamDAO.queryByParam("pathElemId", pathElem.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElemParam != null;
    }

  @Test
    public void pathElemParamList() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemParamDAO dao = new PathElemParamDAO(this.dbname);
        List<PathElemParam> pathElemParams = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !pathElemParams.isEmpty();
    }
}
