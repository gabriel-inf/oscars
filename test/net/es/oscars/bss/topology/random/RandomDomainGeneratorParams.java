package net.es.oscars.bss.topology.random;

import java.util.HashMap;
public class RandomDomainGeneratorParams {

    public enum LinkRole {
        BACKBONE,
        EDGE,
    }
    
    public enum TopologyType {
        STAR,
        MESH,
        RANDOM,
    }
    public enum MinMax {
        MIN, 
        MAX
    }
    
    public static class Bandwidths {
        public static final Long GBPS      = 1000000000L;
        public static final Long TENGBPS   = 10000000000L;
    }
    
    public Long seed;
    public TopologyType type = TopologyType.RANDOM;
    public Integer minNodes     = 1;
    public Integer maxNodes     = 10;
    
    
    // the bare minimum number of backbone links coming out of a node
    public Integer minBBLinks   = 1;
    // the absolute maximum number of backbone links coming out of a node 
    // it should be > minBBLinks. 
    public Integer maxBBLinks   = 8;

    // the average percentage of maxBBLinks that go out of a node
    // value may not be achievable depending on the topology
    public Double density = 0.6;
    
    // how "meshy" the topology is
    // after making things connected, what is the chance that
    // a random new backbone link will go between two previously 
    // unconnected nodes (as opposed to already connected)
    public Double meshiness = 0.2;
    
    
   
    public HashMap<Double, HashMap<MinMax, Integer>> edgeDistribution;
    
    public HashMap<LinkRole, HashMap<Float, Long>> bwDistribution;
    
    public RandomDomainGeneratorParams(Long seed) {
        this.seed = seed;
        
        bwDistribution = new HashMap<LinkRole, HashMap<Float, Long>>();
        // core links are mostly 10Gbps
        HashMap<Float, Long> bbDist = new HashMap<Float, Long>();
        bbDist.put(0.3F, Bandwidths.GBPS);
        bbDist.put(0.7F, Bandwidths.TENGBPS);
        bwDistribution.put(LinkRole.BACKBONE, bbDist);
        
        // edge links are mostly 1Gbps
        HashMap<Float, Long> edgeDist = new HashMap<Float, Long>();
        edgeDist.put(0.3F, Bandwidths.TENGBPS);
        edgeDist.put(0.7F, Bandwidths.GBPS);
        bwDistribution.put(LinkRole.EDGE, edgeDist);

        
        edgeDistribution = new HashMap<Double, HashMap<MinMax, Integer>>();
        // 10% of our nodes will be edge "hubs" with more edges
        HashMap<MinMax, Integer> hubDist = new HashMap<MinMax, Integer>();
        edgeDistribution.put(0.1, hubDist);
        hubDist.put(MinMax.MIN, 2);
        hubDist.put(MinMax.MAX, 5);

        // the rest won't
        HashMap<MinMax, Integer> restDist = new HashMap<MinMax, Integer>();
        edgeDistribution.put(0.9, restDist);
        restDist.put(MinMax.MIN, 0);
        restDist.put(MinMax.MAX, 2);

        
    
    }
}
