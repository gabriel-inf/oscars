import sys
import time
from pprint import pprint
from ZSI import FaultException
from ZSI.ServiceProxy import ServiceProxy
from wssecurity import SignatureHandler

#WSDL_URL = 'https://wiki.internet2.edu/confluence/download/attachments/16272/OSCARS.wsdl'
WSDL_URL = 'OSCARS.wsdl'
WS_URL = 'https://test-idc.internet2.edu:8443/axis2/services/OSCARS'


currentTimeSecs = lambda: int(time.time())

signatureHandler = SignatureHandler('cert.cer', 'key.pem')

sp = ServiceProxy(WSDL_URL, url=WS_URL, sig_handler=signatureHandler)


print 'Sending faulty request'
faultyReq = {
    "startTime": currentTimeSecs(),
    "endTime": currentTimeSecs() + 60**2,
    "bandwidth": 100,
    "description": "Gianluca's test",
    "pathInfo": {
        "pathSetupMode": "user-xml"
    }
}
print 'IDC reply:'
try:
    sp.createReservation(faultyReq)
except FaultException, reason:
    print reason
print

print 'Sleeping for 5 seconds...'
time.sleep(5)
print

print 'Sending correct request'
req = {
    "startTime": currentTimeSecs(),
    "endTime": currentTimeSecs() + 60**2,
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
response = sp.createReservation(req)
print 'IDC reply:'

pprint(response)
print

hops = ( hop['linkIdRef'] for hop in response['pathInfo']['path']['hop'] )

print 'Global reservation ID:', response['globalReservationId']
print 'Status:', response['status']
print

print 'Hops:'
for hop in hops:
    print '    ', hop
print

print 'Layer 2 Info:'
print '    Source endpoint:', response['pathInfo']['layer2Info']['srcEndpoint']
print '    Source VTAG:', response['pathInfo']['layer2Info']['srcVtag']
print '    Destination endpoint:', response['pathInfo']['layer2Info']['destEndpoint']
print '    Destination VTAG:', response['pathInfo']['layer2Info']['destVtag']
print

print 'Going to sleep for 30 seconds...'
time.sleep(30)
print

print 'Reservation status:'
pprint(sp.queryReservation({'gri': response['globalReservationId']}))
print

print 'Sleeping another 5 seconds...'
time.sleep(5)
print

print 'Canceling reservation:'
pprint(sp.cancelReservation({'gri': response['globalReservationId']}))
print

