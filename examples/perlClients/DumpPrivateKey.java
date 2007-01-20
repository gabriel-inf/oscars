
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Key;
 
public class DumpPrivateKey {
        static public void main(String[] args) {
                try {
                        KeyStore ks = KeyStore.getInstance("jks");
                        ks.load(new FileInputStream(args[1]),
                                 "password".toCharArray());
                        Key key = ks.getKey(args[0],
                                 "password".toCharArray());
                        System.out.write(key.getEncoded());
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}


