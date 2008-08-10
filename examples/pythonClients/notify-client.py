import sys
import time
from pprint import pprint
from ZSI import FaultException
from ZSI.ServiceProxy import ServiceProxy
from wssecurity import SignatureHandler

WSDL_URL = 'wsdl/OSCARS-Notify.wsdl'
WS_URL = 'http://anna-lab1.internet2.edu:8080/axis2/services/OSCARSNotify'

signatureHandler = SignatureHandler('cert.cer', 'key.pem')

sp = ServiceProxy(WSDL_URL, url=WS_URL, sig_handler=signatureHandler, tracefile=open('test', 'w'))

req = {
    'ConsumerReference': {
        'Address': 'http://aeolus.lyranet.it:8080/'
    },
    'Filter': {
        'TopicExpression': ['idc:INFO'],
        'ProducerProperties': ["/wsa:Address='https://anna-lab1.internet2.edu:8443/axis2/services/OSCARS'"]
    }
}

"""
<ns5:ConsumerReference>
<Address xmlns="http://www.w3.org/2005/08/addressing">http://aeolus.lyranet.it:8080/</Address>
</ns5:ConsumerReference>
<ns5:Filter>
<ns5:TopicExpression Dialect="http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full">idc:INFO</ns5:TopicExpression>
<ns5:ProducerProperties Dialect="http://www.w3.org/TR/1999/REC-xpath-19991116">/wsa:Address='https://anna-lab1.internet2.edu:8443/axis2/services/OSCARS'</ns5:ProducerProperties>
</ns5:Filter>
"""

sp.Subscribe(req)

