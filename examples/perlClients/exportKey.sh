#!/bin/sh
if [ $# != 2 ] ; then
   echo "Usage is: $1  alias keystore"
   exit 1
fi
ALIAS=$1
echo ${ALIAS}
PKEY_8=privatekey.pkcs8
PKEY_64=privatekey.b64
CERT_64=certificate.b64
CERT_12=certificate.p12
keytool -alias ${ALIAS} -export -rfc >${CERT_64}
java DumpPrivateKey $@ >${PKEY_8}
(echo "-----BEGIN PRIVATE KEY-----" ;
 openssl enc -in ${PKEY_8} -a;
 echo "-----END PRIVATE KEY-----") >${PKEY_64}
#openssl pkcs12 -inkey ${PKEY_64} -in ${CERT_64} -out ${CERT_12} -export 
rm ${PKEY_8}  
#echo ${CERT_12}
