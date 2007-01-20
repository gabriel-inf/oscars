

import java.io.BufferedReader;
import java.io.IOException;

public class Args {

    static public String getArg(BufferedReader br, String prompt)
          throws IOException {

        String inarg = null;
        while (inarg == null || inarg.equals ("")){
            System.out.print(prompt + ": ");
            System.out.flush();
            inarg = br.readLine();
        }
        return inarg;
    }

    static public String getArg(BufferedReader br, String prompt, String def)
            throws IOException {

        String inarg = null;
        System.out.print(prompt + ": [" + def +"] ");
        System.out.flush();
        inarg = br.readLine();
        if (inarg == null || inarg.equals("")) { inarg = def; }
        return inarg;
    }
}
