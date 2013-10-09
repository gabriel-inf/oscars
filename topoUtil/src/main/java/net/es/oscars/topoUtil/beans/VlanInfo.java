package net.es.oscars.topoUtil.beans;

import com.google.common.collect.Range;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VlanInfo {
    static final Logger LOG = LoggerFactory.getLogger(VlanInfo.class);
    public static final Integer MAX_TAGGED_VLAN = 4094;
    public static final Integer MIN_TAGGED_VLAN = 2;
    public static final Integer UNTAGGED_VLAN = 0;
    public static final String ANY = "any";
    public static final String UNTAGGED = "untagged";
    public static final String FORMAT = "\\d(\\.\\.\\-\\d)?";

    protected ArrayList<Range<Integer>> ranges = new ArrayList<Range<Integer>>();
    protected boolean tagged;


    private VlanInfo() {
    }

    public String toString() {
        ArrayList<String> tmp = new ArrayList<String>();

        for (Range<Integer> range : ranges) {
            tmp.add(range.lowerEndpoint()+".."+range.upperEndpoint());
        }
        return StringUtils.join(tmp,",");
    }

    public String toOscarsString() {
        ArrayList<String> tmp = new ArrayList<String>();

        for (Range<Integer> range : ranges) {
            tmp.add(range.lowerEndpoint()+"-"+range.upperEndpoint());
        }
        return StringUtils.join(tmp,",");
    }


    public VlanInfo(String expression) throws VlanFormatException {

        if (expression.equals(ANY)) {
            Range<Integer> range = Range.closed(MIN_TAGGED_VLAN, MAX_TAGGED_VLAN);
            ranges.add(range);
            return;
        } else if (expression.equals(UNTAGGED)) {
            Range<Integer> range = Range.closed(UNTAGGED_VLAN, UNTAGGED_VLAN);
            ranges.add(range);
            return;
        }

        String[] parts = expression.split("\\,");
        if (parts.length == 0) {
            throw new VlanFormatException("Invalid expression: "+expression+", allowed: "+FORMAT);
        }
        for (String part : parts) {
            part = part.trim().toLowerCase();
            if (part.contains("..")) {
                String[] moreParts = part.split("\\.\\.");
                if (moreParts.length != 2) {
                    throw new VlanFormatException("Invalid expression: "+expression+", allowed: "+FORMAT);
                }
                Integer a = Integer.valueOf(moreParts[0]);
                Integer z = Integer.valueOf(moreParts[1]);
                if (a > z) {
                    Integer temp = a;
                    a = z;
                    z = temp;
                }
                if (a < MIN_TAGGED_VLAN) {
                    throw new VlanFormatException("Invalid expression: "+expression+", "+a+ "is < "+MIN_TAGGED_VLAN);

                } else if (z > MAX_TAGGED_VLAN) {
                    throw new VlanFormatException("Invalid expression: "+expression+", "+z+ "is < "+MAX_TAGGED_VLAN);
                }
                LOG.info("a range: "+a +".." +z);

                Range<Integer> range = Range.closed(a, z);
                ranges.add(range);

            } else {
                Integer a = Integer.valueOf(part);
                Range<Integer> range = Range.closed(a, a);
                LOG.info("a range: "+a);
                ranges.add(range);
            }
        }
        Comparator<Range<Integer>> comp = new Comparator<Range<Integer>>() {
            @Override
            public int compare(Range<Integer> o1, Range<Integer> o2) {
                return o2.lowerEndpoint() - o1.lowerEndpoint();
            }
        };
        Collections.sort(ranges, comp);


    }



    public boolean isTagged() {
        return tagged;
    }

    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    public ArrayList<Range<Integer>> getRanges() {
        return ranges;
    }

    public void setRanges(ArrayList<Range<Integer>> ranges) {
        this.ranges = ranges;
    }
}
