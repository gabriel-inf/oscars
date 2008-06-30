#!/usr/bin/env python

from ZSI import TC, TCcompound

class EchoRequest(TCcompound.Struct):
    def __init__(self, name, message=None):
        self._message = message
        TC.Struct.__init__(self, EchoRequest,
                           [TC.String(pname='message', aname='_message',
                                      optional = 1)],
                           pname=name, aname='_%s' % name, oname=name)

class EchoRequestWrapper(EchoRequest):
    typecode = EchoRequest(name='echo')

    def __init__(self, message):
        EchoRequest.__init__(self, name='echo', message=message)



class EchoResponse(TCcompound.Struct):
    def __init__(self, name, message=None):
        self._message = message
        TC.Struct.__init__(self, EchoResponse,
                           [TC.String(pname='message', aname='_message',
                                      optional = 1)],
                           pname=name, aname='_%s' % name, oname=name)

class EchoResponseWrapper(EchoResponse):
    typecode = EchoRequest(name='echoResponse')

    def __init__(self):
        EchoResponse.__init__(self, name='echoResponse')

