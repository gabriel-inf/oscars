import sys
import time
from pprint import pprint
from ZSI import FaultException
from ZSI.ServiceProxy import ServiceProxy
from wssecurity import SignatureHandler

WSDL_URL = 'wsdl/OSCARS-Notify.wsdl'
WS_URL = 'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify'


signatureHandler = SignatureHandler('cert.cer', 'key.pem')

sp = ServiceProxy(WSDL_URL, url=WS_URL, sig_handler=signatureHandler, tracefile=open('test', 'w'))

req = {
    'ConsumerReference': {
        'Address': 'http://test.campanel.la/'
    }
}

sp.Subscribe(req)

