package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the pathElems table, which requires a working
 *     PathElem.java and PathElem.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
/* @Test(groups={ "bss/topology", "pathElem" }, dependsOnGroups={ "create" }) */
 @Test(groups={ "broken" })
public class PathElemTest {
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
    public void pathElemQuery() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO dao = new PathElemDAO(this.dbname);
        String description = "test suite";
        PathElem pathElem = (PathElem)
            dao.queryByParam("description", description);
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
