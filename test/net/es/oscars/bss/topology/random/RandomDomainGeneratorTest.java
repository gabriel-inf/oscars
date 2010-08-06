package net.es.oscars.bss.topology.random;


import net.es.oscars.bss.topology.random.RandomDomainGenerator.DomainResult;

import org.testng.annotations.Test;

@Test(groups={ "topology.rand" })

public class RandomDomainGeneratorTest {


    @Test
    
    public void testDomainCreation() throws InterruptedException {
        Long seed = 100L;
        RandomDomainGenerator rdg = RandomDomainGenerator.getInstance();
        DomainVizFrame frame = new DomainVizFrame();
        frame.setVisible(true);
        
        for (Long i = 0L; i < 5L; i++) {
            RandomDomainGeneratorParams params = new RandomDomainGeneratorParams(i);
            params.minBBLinks = 1;
            params.maxBBLinks = 6;
            DomainResult res = rdg.makeDomain(params);
            frame.showGraph(res.graph);
            Thread.sleep(1000);
            
        }
    }
    

    
}
