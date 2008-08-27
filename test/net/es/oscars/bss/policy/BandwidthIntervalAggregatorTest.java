package net.es.oscars.bss.policy;

import org.testng.annotations.*;

import java.util.*;

/**
 * This class tests methods in BandwidthIntervalAggregator.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class BandwidthIntervalAggregatorTest {

  @Test
    public void identicalTest() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        // identical intervals
        aggrs.add(new BandwidthIntervalAggregator(0L, 10L, 0L, 10L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 10L, 0L, 10L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void leftTest1() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 10L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 10L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15 : "Max: " + maxSum + " does not equal 15";
    }

  @Test
    public void rightTest1() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(10L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(10L, 20L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15 : "Max: " + maxSum + " does not equal 15";
    }

  @Test
    public void containedTest1() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(5L, 10L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void disjointTest1() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 9L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(11L, 20L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void disjointTest2() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 4L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(5L, 8L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(11L, 20L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        newAgg.add(aggrs.get(3).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void overlapTest1() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        // intervals are open at right end
        aggrs.add(new BandwidthIntervalAggregator(0L, 11L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(11L, 20L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void overlapTest2() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 12L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(11L, 20L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15L : "Max: " + maxSum + " does not equal 15";
    }

  @Test
    public void overlapTest3() {
        List<BandwidthIntervalAggregator> aggrs =
            new ArrayList<BandwidthIntervalAggregator>();
        aggrs.add(new BandwidthIntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(0L, 5L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(4L, 7L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(6L, 9L, 0L, 20L, 5L));
        aggrs.add(new BandwidthIntervalAggregator(11L, 20L, 0L, 20L, 5L));
        BandwidthIntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        newAgg.add(aggrs.get(3).getIntervals());
        newAgg.add(aggrs.get(4).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15L : "Max: " + maxSum + " does not equal 15";
    }
}
