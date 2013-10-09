package net.es.oscars.topoUtil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfceIdentifier {

    public static List<String> explodeIdentifier(String id) {
        ArrayList<String> idents = new ArrayList<String>();
        String rangeExpr = "(.*?)\\[(\\d+)\\.\\.(\\d+)\\]";
        Pattern p = Pattern.compile(rangeExpr);
        Matcher m = p.matcher(id);


        ArrayList<HashMap<String, Integer>> azs = new ArrayList<HashMap<String, Integer>>();
        ArrayList<String> pres = new ArrayList<String>();

        int count = 0;
        int postOffset = 0;

        while (m.find()) {
            String pre = m.group(1);
            String a = m.group(2);
            String b = m.group(3);
            pres.add(pre);

            HashMap<String, Integer> az = new HashMap<String, Integer>();
            az.put("a", Integer.parseInt(a));
            az.put("b", Integer.parseInt(b));
            azs.add(az) ;
            count++;
            postOffset = m.end();
        }

        if (count == 0) {
            idents.add(id);
            return idents;
        }

        String post = id.substring(postOffset);

        for (int i = 0; i < pres.size(); i++) {
            String pre = pres.get(i);
            Integer a = azs.get(i).get("a");
            Integer b = azs.get(i).get("b");

            if (idents.isEmpty()) {
                idents = getLevel(pre, a, b);
            } else {
                ArrayList<String> tmp = new ArrayList<String>();
                for (String prevPre : idents) {
                    tmp.addAll(getLevel(prevPre+pre, a, b));
                }
                idents = tmp;
            }
        }
        ArrayList<String> result = new ArrayList<String>();

        for (String ident : idents) {
            result.add(ident+post);
        }

        return result;

    }
    private static ArrayList<String> getLevel(String pre, int a, int b) {
        ArrayList<String> res = new ArrayList<String>();

        if (a > b) {
            System.exit(1);
        }
        for (int i = a; i <= b; i++) {
            String identifier = pre + i;
            res.add(identifier);
        }
        return res;


    }
}
