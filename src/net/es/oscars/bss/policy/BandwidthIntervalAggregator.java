package net.es.oscars.bss.policy;

import java.util.*;

/**
  * This class handles interval aggregation to check for oversubscription.
  */
public class BandwidthIntervalAggregator {
    private List<TimePoint> intervals; 

    public BandwidthIntervalAggregator(Long start, Long end, 
                              Long newStart, Long newEnd, Long item) {

        TimePoint pt = null;

        this.intervals = new ArrayList<TimePoint>();
        // new reservation starts after beginning of this one
        if (newStart >= start) {
            pt = new TimePoint(newStart, item);
            this.intervals.add(pt);
        } else {
            // fill interval from start of new reservation to this with 0
            pt = new TimePoint(newStart, 0L);
            this.intervals.add(pt);
            pt = new TimePoint(start, item);
            this.intervals.add(pt);
        }
        // new reservation ends before this one
        if (end >= newEnd) {
            pt = new TimePoint(newEnd, 0L);
            this.intervals.add(pt);
        } else {
            // fill interval from end of this reservation to new with 0
            pt = new TimePoint(end, 0L);
            this.intervals.add(pt);
            pt = new TimePoint(newEnd, 0L);
            this.intervals.add(pt);
        }
    }

    /**
     * Private inner class to handle a point in time, and its item
     * in the segment until the next time point.
     */
    private class TimePoint {
        private Long pt;
        private Long item;

        private TimePoint(Long pt, Long item) {
            this.pt = pt;
            this.item = item;
        }

        private Long getPt() {
            return this.pt;
        }

        private Long getItem() {
            return this.item;
        }

        private void addToItem(Long item) {
            this.item += item;
        }
    }

    public List<TimePoint> getIntervals() {
        return this.intervals;
    }

    /**
     * Splits the list of intervals, given a new set of intervals,
     * and adjusts capacities.
     *
     * @param compareIntervals list of TimePoints to split by
     */
    public void add(List<TimePoint> compareIntervals) {
        int currentIncr = 0;
        TimePoint currentPt = this.intervals.get(currentIncr);
        List<TimePoint> newIntervals = new ArrayList<TimePoint>();
        TimePoint newPt = null;
        // ratchet through the list
        for (int i=0; i < compareIntervals.size(); i++) {
            TimePoint comparePt = compareIntervals.get(i);
            // compare point greater than current point; may be more than one
            // current point in between
            while (comparePt.getPt() > currentPt.getPt()) {
                newPt = new TimePoint(currentPt.getPt(),
                     currentPt.getItem() + compareIntervals.get(i-1).getItem());
                newIntervals.add(newPt);
                currentIncr++;
                currentPt = this.intervals.get(currentIncr);
            }
            if (comparePt.getPt() == currentPt.getPt()) {
                newPt = new TimePoint(currentPt.getPt(),
                                     currentPt.getItem() + comparePt.getItem());
                newIntervals.add(newPt);
                currentIncr++;
                if (currentIncr == this.intervals.size()) { break; }
                currentPt = this.intervals.get(currentIncr);
            } else if (comparePt.getPt() < currentPt.getPt()) {
                newPt = new TimePoint(comparePt.getPt(),
                     this.intervals.get(currentIncr-1).getItem() + comparePt.getItem());
                newIntervals.add(newPt);
            }
        }
        this.intervals = newIntervals;
    }

    public Long getMax() {
        Long maxSum = 0L;
        for (int i = 0; i < this.intervals.size(); i++) {
            if (this.intervals.get(i).getItem() > maxSum) {
                maxSum = this.intervals.get(i).getItem();
            }
        }
        return maxSum;
    }
}
