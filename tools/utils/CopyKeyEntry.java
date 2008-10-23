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
        Boolean isPKCS12 = false;
        String usageMsg = "usage:  CopyKeyEntry -a alias  [-injks <oldKeystore> | -inpkcs12 <oldKeystore>] -out <newKeystore>";
        java.security.cert.Certificate[] chain = null;
        
        try {
            KeyStore inKS = KeyStore.getInstance("jks");      
            KeyStore outKS = KeyStore.getInstance("jks");
            KeyStore inP12 = KeyStore.getInstance("PKCS12");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            Key keyEntry = null;

            if (args.length < 3) {
                System.out.println(usageMsg);
                System.exit(1);
            }

            int i = 0;
            while (i < args.length) {
                if (args[i].equals("-a")) {
                    aliasName = args[i + 1];
                } else if (args[i].equals("-injks")) {
                    inKeyStore = args[i + 1];
                    isPKCS12 = false;
                } else if (args[i].equals("-inpkcs12")) {
                    inKeyStore = args[i + 1];
                    isPKCS12= true;
                } else if (args[i].equals("-out")) {
                    outKeyStore = args[i + 1];
                }
                i = i + 2;
            }
            if ((aliasName == null) || (inKeyStore == null) || (outKeyStore == null)) {
                System.out.println(usageMsg);
                System.exit(1);
            }

            FileInputStream in = new FileInputStream(inKeyStore);

            
            System.out.print("input password for " + inKeyStore + " : ");
            inPassword = br.readLine().trim();
            inKeyPassword = inPassword;
            if (isPKCS12) {
                inP12.load(in,inPassword.toCharArray());
                Enumeration<String> aliases = inP12.aliases();
                while (aliases.hasMoreElements()) {
                    String name = aliases.nextElement(); 
                    System.out.println("pkcs12 alias is: " + name);
                    System.out.print("input password for KeyEntry " + name + " if different from keystore password: ");
                    inKeyPassword = br.readLine().trim();
                    if ( (inKeyPassword == null) ||
                            (inKeyPassword.length() == 0)){
                        inKeyPassword = inPassword;
                    }
                    Key privKey = inP12.getKey(name, inKeyPassword.toCharArray());
                    if (privKey != null) {
                        keyEntry = privKey;
                        //System.out.println(keyEntry.toString());
                        chain = inP12.getCertificateChain(name);
                        if (chain != null) {
                            //System.out.println(chain[0].toString());
                        }
                    }
                }
            } else {
                inKS.load(in,inPassword.toCharArray());

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
                chain = inKS.getCertificateChain(aliasName);
            }
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
