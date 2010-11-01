package net.es.oscars.bss.topology;

import org.testng.annotations.Test;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.VlanRange;

@Test(groups={ "bss.vlan" })
public class VlanRangeTest {

  @Test
    public void testVlanMEthods() {
      try {
          VlanRange one = new VlanRange("2-2602,2604-3799,3801-4094");
          VlanRange other = new VlanRange("3561-3563");
          System.out.println("ONE: "+one+ " OTHER: "+other);
          VlanRange tmp = VlanRange.and(one, other);
          System.out.println("AND: "+tmp);
          tmp = VlanRange.subtract(one, other);
          System.out.println("SUBTRACT: "+tmp);
      } catch (BSSException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }      
      
    }

}
