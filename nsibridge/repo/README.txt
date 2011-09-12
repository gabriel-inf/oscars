Put OSCARS.jks and ssl-keystore.jks files here

1. OSCARS.jks : the keystore where the credentials for the OSCARS client are kept.
This means:
--- an OSCARS user key
--- all certificates in the chain needed to trust that user key.
The keystore password, user key alias, and filename for this certificate are set at rampConfig.xml


2. ssl-keystore.jks : the keystore where the certs needed to trust the OSCARS SSL server are stored
--- all the certificates in the chain needed to trust that server cert
The filename is hard-coded, as well as the password (oscars). In the future this will be configurable.

