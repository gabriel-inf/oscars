#!/bin/bash

clKeystore="config/client-keystore.jks"
clKeystorePass="client-keystore"
clKeyPass="client-key"
clAlias="clientKey"
clDN="CN=client, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"


tmpFile="config/temp.rfc"


echo "client-bus-ssl.xml config:"
echo "

      <sec:trustManagers>
          <sec:keyStore type=\"JKS\" password=\"$clKeystorePass\"
               file=\"$clKeystore\"/>
      </sec:trustManagers>
      <sec:keyManagers keyPassword=\"$clKeyPass\">
           <sec:keyStore type=\"JKS\" password=\"$clKeystorePass\"
                file=\"$clKeystore\"/>
      </sec:keyManagers>

      "


# create a client key
keytool -keystore "$clKeystore"   -storepass "$clKeystorePass"   -genkey -alias "$clAlias" -dname "$clDN" -keyAlg RSA -keypass "$clKeyPass"
# export it
keytool -keystore "$clKeystore"   -storepass "$clKeystorePass"   -export -alias "$clAlias" -file "$tmpFile" -rfc


rm $tmpFile

