package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.BSSException;

/**
 * This class tests methods in PathDAO.java, which requires a working
 *     Path.java and Path.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
/* @Test(groups={ "bss/topology", "path" }, dependsOnGroups={ "create" }) */
@Test(groups={ "broken" })
public class PathTest {
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
    public void pathCreate() throws BSSException {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO pathDAO = new PathDAO(this.dbname);
        Path path = new Path();
        path.setExplicit(false);

        PathElem pathElem0 = new PathElem();
        pathElem0.setDescription("ingress");
        Ipaddr ipaddr0 = new Ipaddr();
        ipaddr0.setValid(true);
        String hop0 = this.props.getProperty("hop0");
        ipaddr0.setIP(hop0);
        Link link = new Link();
        link.setValid(true);
        link.setSnmpIndex(0);
        link.setCapacity(10000000L);
        link.setMaximumReservableCapacity(5000000L);
        link.setTopologyIdent("test suite");
        ipaddr0.setLink(link);
        pathElem0.setLink(link);

        PathElem pathElem6 = new PathElem();
        pathElem6.setDescription("egress");
        Ipaddr ipaddr6 = new Ipaddr();
        ipaddr6.setValid(true);
        String hop6 = this.props.getProperty("hop6");
        ipaddr6.setIP(hop6);
        Link link6 = new Link();
        link6.setValid(true);
        link6.setSnmpIndex(1);
        link6.setCapacity(10000000L);
        link6.setMaximumReservableCapacity(5000000L);
        link6.setTopologyIdent("test suite6");
        ipaddr6.setLink(link6);
        pathElem6.setLink(link6);

        path.setPathElem(pathElem0);
        pathDAO.create(path);
        this.sf.getCurrentSession().getTransaction().commit();
        assert path != null;
    }

  @Test(dependsOnMethods={ "pathCreate" })
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

  @Test(dependsOnMethods={ "pathCreate" })
    public void pathList() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        List<Path> paths = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !paths.isEmpty();
    }

  @Test(dependsOnMethods={ "pathCreate", "pathQuery", "pathList" })
    public void pathRemove() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        PathElem pathElem = (PathElem) pathElemDAO.queryByParam("description",
                                 "ingress");
        Path path = (Path) dao.queryByParam("pathElemId", pathElem.getId()); 
        dao.remove(path);
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
