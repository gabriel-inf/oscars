#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
from optparse import OptionParser
from OSCARS import ClientFactory, WrappedFunctionFactory
from pprint import pprint


def getOperationFromScriptName(name):
    operations = {
        'subscribe.py': 'Subscribe',
        'renew.py': 'Renew',
        'pauseSubscription.py': 'PauseSubscription',
        'resumeSubscription.py': 'ResumeSubscription',
        'unsubscribe.py': 'Unsubscribe'
    }
    for scriptName, operation in operations.iteritems():
        if scriptName in sys.argv[0]:
            return operation
    return None


if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-u', '--url', dest='url', help='Web service URL')
    parser.add_option('-c', '--cert', dest='cert', help='Path to the certificate file')
    parser.add_option('-k', '--key', dest='key', help='Path to the private key file')
    parser.add_option('-p', '--password', dest='password', help='Private key password (if any)')

    operation = getOperationFromScriptName(sys.argv[0])
    if operation is None:
        print >> sys.stderr, "Please don't call this script directly"
        sys.exit(1)

    functionArgs = WrappedFunctionFactory.getArguments(operation)
    for arg, defaultValue in functionArgs:
        parser.add_option('--%s' % arg, dest=arg, default=defaultValue)

    options, args = parser.parse_args()

    requiredArgs = ['url', 'cert', 'key'] +\
                   [ arg[0] for arg in args if arg[1] is None ]
    for arg in requiredArgs:
        if getattr(options, arg) is None:
            parser.print_help(sys.stderr)
            print >> sys.stderr, '\nMissing argument "%s"' % arg
            sys.exit(1)

    client = ClientFactory.instantiateOSCARSNotifyClient(options.url,
                                                         options.cert,
                                                         options.key,
                                                         options.password)

    function = getattr(client, operation)
    callingArgsValues = [ getattr(options, arg[0]) for arg in functionArgs ]
    pprint(function(*callingArgsValues))

