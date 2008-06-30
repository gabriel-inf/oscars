#!/usr/bin/env python

import socket
import sys
from optparse import OptionParser
from wssecurity import SignatureHandler
from ZSI.client import Binding
from sampletypes import EchoRequestWrapper, EchoResponseWrapper

WS_URL = 'http://test-idc.internet2.edu:8080/axis2/services/OSCARS'

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-c', '--cert', dest='certFilePath',
                      help='path to the certificate')
    parser.add_option('-k', '--key', dest='privateKeyFilePath',
                      help='path to the private key')
    parser.add_option('-p', '--password', dest='privateKeyPassword',
                      help='private key password')
    (options, args) = parser.parse_args()

    signatureHandler = SignatureHandler(options.certFilePath,
                                        options.privateKeyFilePath,
                                        options.privateKeyPassword)

    binding = Binding(url=WS_URL, sig_handler=signatureHandler)
    echoRequest = EchoRequestWrapper('Hello world!')
    try:
        binding.Send(None, 'echo', echoRequest,
                     encodingStyle='http://www.w3.org/2001/12/soap-encoding')
        response = binding.Receive(EchoResponseWrapper())
        print 'Got back "%s"' % response._message
    except socket.error, (errorNo, errorMessage):
        sys.stderr.write('Socket error: %s\n' % errorMessage)
        sys.exit(errorNo)

