package net.es.oscars.bss;

import org.testng.annotations.*;

import java.util.*;

/**
 * This class tests methods in IntervalAggregator.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class IntervalAggregatorTest {

  @Test
    public void identicalTest() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        // identical intervals
        aggrs.add(new IntervalAggregator(0L, 10L, 0L, 10L, 5L));
        aggrs.add(new IntervalAggregator(0L, 10L, 0L, 10L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void leftTest1() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(0L, 10L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(0L, 10L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15 : "Max: " + maxSum + " does not equal 15";
    }

  @Test
    public void rightTest1() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(10L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(10L, 20L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15 : "Max: " + maxSum + " does not equal 15";
    }

  @Test
    public void containedTest1() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(5L, 10L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void disjointTest1() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(0L, 9L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(11L, 20L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void disjointTest2() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(0L, 4L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(5L, 8L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(11L, 20L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        newAgg.add(aggrs.get(3).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void overlapTest1() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        // intervals are open at right end
        aggrs.add(new IntervalAggregator(0L, 11L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(11L, 20L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 10L : "Max: " + maxSum + " does not equal 10";
    }

  @Test
    public void overlapTest2() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(0L, 12L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(11L, 20L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15L : "Max: " + maxSum + " does not equal 15";
    }

  @Test
    public void overlapTest3() {
        List<IntervalAggregator> aggrs =
            new ArrayList<IntervalAggregator>();
        aggrs.add(new IntervalAggregator(0L, 20L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(0L, 5L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(4L, 7L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(6L, 9L, 0L, 20L, 5L));
        aggrs.add(new IntervalAggregator(11L, 20L, 0L, 20L, 5L));
        IntervalAggregator newAgg = aggrs.get(0);
        newAgg.add(aggrs.get(1).getIntervals());
        newAgg.add(aggrs.get(2).getIntervals());
        newAgg.add(aggrs.get(3).getIntervals());
        newAgg.add(aggrs.get(4).getIntervals());
        Long maxSum = newAgg.getMax();
        assert maxSum == 15L : "Max: " + maxSum + " does not equal 15";
    }
}
