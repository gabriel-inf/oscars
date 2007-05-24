

import java.io.BufferedReader;
import java.io.IOException;

public class Args {

    static public String getArg(BufferedReader br, String prompt)
          throws IOException {

        String inarg = null;
        while (inarg == null || inarg.equals ("")){
            System.out.print(prompt + ": ");
            System.out.flush();
            inarg = br.readLine().trim();
        }
        return inarg;
    }

    static public String getArg(BufferedReader br, String prompt, String def)
            throws IOException {

        String inarg = null;
        System.out.print(prompt + ": [" + def.trim() +"] ");
        System.out.flush();
        inarg = br.readLine().trim();
        if (inarg == null || inarg.equals("")) { inarg = def.trim(); }
        return inarg;
    }
}
