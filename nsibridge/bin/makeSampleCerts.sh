#!/bin/bash

clTruststore="config/client-truststore.jks"
clTruststorePass="client-trust"
clKeystore="config/client-keystore.jks"
clKeystorePass="client-keystore"
clKeyPass="client-key"
clAlias="clientKey"
clDN="CN=client, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"

svTruststore="config/server-truststore.jks"
svTruststorePass="server-trust"
svKeystore="config/server-keystore.jks"
svKeystorePass="server-keystore"
svKeyPass="server-key"
svAlias="serverKey"
svDN="CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"

tmpFile="config/temp.rfc"


echo "client-bus-ssl.xml config:"
echo "

      <sec:trustManagers>
          <sec:keyStore type=\"JKS\" password=\"$clTruststorePass\"
               file=\"$clTruststore\"/>
      </sec:trustManagers>
      <sec:keyManagers keyPassword=\"$clKeyPass\">
           <sec:keyStore type=\"JKS\" password=\"$clKeystorePass\"
                file=\"$clKeystore\"/>
      </sec:keyManagers>

      "

echo "server-bus-ssl.xml config:"
echo "

      <sec:trustManagers>
          <sec:keyStore type=\"JKS\" password=\"$svTruststorePass\"
               file=\"$svTruststore\"/>
      </sec:trustManagers>
      <sec:keyManagers keyPassword=\"$svKeyPass\">
           <sec:keyStore type=\"JKS\" password=\"$svKeystorePass\"
                file=\"$svKeystore\"/>
      </sec:keyManagers>
      "

# create a client key
keytool -keystore "$clKeystore"   -storepass "$clKeystorePass"   -genkey -alias "$clAlias" -dname "$clDN" -keyAlg RSA -keypass "$clKeyPass"
# export it
keytool -keystore "$clKeystore"   -storepass "$clKeystorePass"   -export -alias "$clAlias" -file "$tmpFile" -rfc
# import it into the server truststore
keytool -keystore "$svTruststore" -storepass "$svTruststorePass" -import -alias "$clAlias" -file "$tmpFile" -noprompt

# create a client key
keytool -keystore "$svKeystore"   -storepass "$svKeystorePass"   -genkey -alias "$svAlias" -dname "$svDN" -keyAlg RSA -keypass "$svKeyPass"
# export it
keytool -keystore "$svKeystore"   -storepass "$svKeystorePass"   -export -alias "$svAlias" -file "$tmpFile" -rfc
# import it into the client truststore
keytool -keystore "$clTruststore" -storepass "$clTruststorePass" -import -alias "$svAlias" -file "$tmpFile" -noprompt

rm $tmpFile

