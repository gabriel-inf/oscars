import sys
import time
from ZSI.ServiceProxy import ServiceProxy
from wssecurity import SignatureHandler

#WSDL_URL = 'https://wiki.internet2.edu/confluence/download/attachments/16272/OSCARS.wsdl'
WSDL_URL = 'OSCARS.wsdl'
WS_URL = 'https://test-idc.internet2.edu:8443/axis2/services/OSCARS'

currentTimeMillis = lambda: int(time.time() * 1000)
req = {
    "startTime": currentTimeMillis(),
    "endTime": currentTimeMillis() + 1000*60**2,
    "bandwidth": 100,
    "description": "Gianluca's test",
    "pathInfo": {
        "pathSetupMode": "user-xml",
        "layer2Info": {
            "srcEndpoint": "test-newy.dcn.internet2.edu",
            "destEndpoint": "test-chic.dcn.internet2.edu"
        }
    }
}

signatureHandler = SignatureHandler('cert.cer', 'key.pem')

sp = ServiceProxy(WSDL_URL, url=WS_URL, tracefile=sys.stdout, sig_handler=signatureHandler)

sp.createReservation(req)
