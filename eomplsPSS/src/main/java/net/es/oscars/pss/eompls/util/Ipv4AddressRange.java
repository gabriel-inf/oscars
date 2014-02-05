package net.es.oscars.pss.eompls.util;



public class Ipv4AddressRange {
    private Long base;
    private Integer range;

    public Long getBase() {
        return base;
    }

    public void setBase(Long base) {
        this.base = base;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }



    public String toString() {
       String res = "base: "+base+" range: "+range;
       return res;

    }

    public Ipv4AddressRange(String cidr) {
        String[] parts = cidr.split("/");
        base = ipToLong(parts[0]);
        int mask = Integer.parseInt(parts[1]);
        int exp = 32-mask;

        Double d = Math.pow(2, exp);
        range = d.intValue();



    }


    public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] atoms = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
        }

        return result & 0xFFFFFFFF;
    }

    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                sb.insert(0, '.');
            }

            ip >>= 8;
        }

        return sb.toString();
    }


    public String getAddressInRange(Integer offset) throws IllegalArgumentException {
        if (offset >= range) throw new IllegalArgumentException();
        if (offset == 0) throw new IllegalArgumentException();
        Long addr = base + offset;
        return longToIp(addr);
    }

}
