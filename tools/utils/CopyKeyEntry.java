import java.util.*;
import java.io.*;

import java.security.KeyStore;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.KeyStore.*;

public class CopyKeyEntry {
/**
 * CopyKeyEntry is a command-line client for copying a KeyEntry from one
 * keystore to another.
 * 
 * @author Mary R. Thompson (mrthompson@lbl.gov)
 */


    public static void main(String[] args) {

        String aliasName = null;
        String inKeyStore = null;
        String inPassword = null;
        String inKeyPassword = null;
        String outPassword = null;
        String outKeyPassword = null;
        String outKeyStore = null;
        
        try {
            KeyStore inKS = KeyStore.getInstance("jks");      
            KeyStore outKS = KeyStore.getInstance("jks");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            Key keyEntry = null;

            if (args.length < 3) {
                System.out
                .println("usage:  CopyKeyEntry -a alias  -in <oldKeystore> -out <newKeystore>");
                System.exit(1);
            }

            int i = 0;
            while (i < args.length) {
                if (args[i].equals("-a")) {
                    aliasName = args[i + 1];
                } else if (args[i].equals("-in")) {
                    inKeyStore = args[i + 1];
                } else if (args[i].equals("-out")) {
                    outKeyStore = args[i + 1];
                }
                i = i + 2;
            }
            if ((aliasName == null) || (inKeyStore == null) || (outKeyStore == null)) {
                System.out
                .println("usage:  CopyKeyEntry -a alias  -in <oldKeystore> -out <newKeystore>");
                System.exit(1);
            }

            FileInputStream in = new FileInputStream(inKeyStore);

            
            System.out.print("input password for " + inKeyStore + " : ");
            inPassword = br.readLine().trim();
            inKS.load(in,inPassword.toCharArray());
            in.close();

            System.out.print("input password for KeyEntry " + aliasName + " if different from keystore password: ");
            inKeyPassword = br.readLine().trim();
            if ( (inKeyPassword == null) ||
                 (inKeyPassword.length() == 0)){
                inKeyPassword = inPassword;
            }
            
            if (inKS.isKeyEntry(aliasName)) {
                keyEntry = inKS.getKey(aliasName,inKeyPassword.toCharArray());
            } else {
                System.out.println(aliasName + "is not a KeyEntry");
                System.exit(0);
            }
            java.security.cert.Certificate[] chain = inKS.getCertificateChain(aliasName);
            
            in = new FileInputStream(outKeyStore);
            System.out.print("input password for " + outKeyStore + " : ");
            outPassword = br.readLine().trim();
            outKS.load(in,outPassword.toCharArray());
            in.close();
            
            outKS.setKeyEntry(aliasName, keyEntry, inKeyPassword.toCharArray(), chain);
            
            FileOutputStream out = new FileOutputStream(outKeyStore);
            outKS.store(out,outPassword.toCharArray());
            out.close();
        } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
        }
    }
}
