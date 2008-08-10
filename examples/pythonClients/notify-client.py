import sys
import time
from pprint import pprint
from ZSI import FaultException
from ZSI.ServiceProxy import ServiceProxy
from wssecurity import SignatureHandler

WSDL_URL = 'wsdl/OSCARS-Notify.wsdl'
WS_URL = 'http://anna-lab1.internet2.edu:8080/axis2/services/OSCARSNotify'

signatureHandler = SignatureHandler('cert.cer', 'key.pem')

sp = ServiceProxy(WSDL_URL, url=WS_URL, sig_handler=signatureHandler)

req = {
    'ConsumerReference': {
        'Address': 'http://aeolus.lyranet.it:8080/'
    },
    'Filter': {
        'TopicExpression': [
            [('Dialect', 'http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full'), 'idc:INFO']
        ],
        'ProducerProperties': [
            [('Dialect', 'http://www.w3.org/TR/1999/REC-xpath-19991116'), "/wsa:Address='https://anna-lab1.internet2.edu:8443/axis2/services/OSCARS'"]
        ]
    }
}

subscribeResult = sp.Subscribe(req)
subscriptionId = subscribeResult['SubscriptionReference']['ReferenceParameters']['subscriptionId']

print 'Subscribed!'
print 'Subscription Id:', subscriptionId
print 'Termination time:', subscribeResult['TerminationTime']
print

print 'Sleeping for 10 seconds...'
time.sleep(10)
print

renewResult = sp.Renew({
    'TerminationTime': None,
    'SubscriptionReference': {
        'Address': 'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify',
        'ReferenceParameters': {
            'subscriptionId': subscriptionId
        }
    }
})

print 'Renewed!'
print 'New termination time:', renewResult['TerminationTime']
print

print 'Sleeping for 5 seconds...'
time.sleep(5)
print

pauseResult = sp.PauseSubscription({
    'SubscriptionReference': {
        'Address': 'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify',
        'ReferenceParameters': {
            'subscriptionId': subscriptionId
        }
    }
})
print 'Paused!'
print

print 'Sleeping for 10 seconds...'
time.sleep(10)
print

resumeResult = sp.ResumeSubscription({
    'SubscriptionReference': {
        'Address': 'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify',
        'ReferenceParameters': {
            'subscriptionId': subscriptionId
        }
    }
})
print 'Resumed!'
print

print 'Sleeping for 5 seconds...'
time.sleep(5)
print

sp.Unsubscribe({
    'SubscriptionReference': {
        'Address': 'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify',
        'ReferenceParameters': {
            'subscriptionId': subscriptionId
        }
    }
})
print 'Unsubscribed!'

