# -*- coding: utf-8 -*-

from time import time
from wssecurity import SignatureHandler
from ZSI.ServiceProxy import ServiceProxy


OSCARS_WSDL_PATH = 'wsdl/OSCARS.wsdl'
OSCARSNOTIFY_WSDL_PATH = 'wsdl/OSCARS-Notify.wsdl'


def currentTimeSecs():
    return int(time())


class InvalidRequestException(Exception):
    def __init__(self, message):
        self.message = message

    def __str__(self):
        return repr(self.message)


class MessageBuilder(object):

    @staticmethod
    def buildCreateReservationMessage(durationSecs, bandwidth, desc,
                                      srcEndpoint, destEndpoint):
        currentTime = currentTimeSecs()
        message = {
            'startTime': currentTime,
            'endTime': currentTime + int(durationSecs),
            'bandwidth': int(bandwidth),
            'description': desc,
            'pathInfo': {
                'pathSetupMode': 'user-xml',
                'layer2Info': {
                    'srcEndpoint': srcEndpoint,
                    'destEndpoint': destEndpoint
                }
            }
        }
        return message

    @staticmethod
    def buildQueryReservationMessage(globalReservationId):
        return {'gri': globalReservationId}

    @staticmethod
    def buildCancelReservationMessage(globalReservationId):
        return {'gri': globalReservationId}

    @staticmethod
    def buildSubscribeMessage(consumer, topics=[], producers=[]):
        if len(topics) == 0:
            raise InvalidRequestException('Must specify at least one topic')

        if len(producers) == 0:
            raise InvalidRequestException('Must specify at least one producer')

        topicsAttribs = [('Dialect',
                          'http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full')]
        topicsExpr = [ topicsAttribs + [topic] for topic in topics ]

        producerAttribs = [('Dialect',
                            'http://www.w3.org/TR/1999/REC-xpath-19991116')]
        producerProps = [ topicsAttribs +
                          ["/wsa:Address='%s'" % producer] for producer in producers ]

        message = {
            'ConsumerReference': {
                'Address': consumer
            },
            'Filter': {
                'TopicExpression': topicsExpr,
                'ProducerProperties': producerProps
            }
        }
        return message

    @staticmethod
    def _buildSubscriptionReferenceSubpart(producer, subscriptionId):
        subscriptionReference = {
            'Address': producer,
            'ReferenceParameters': {
                'subscriptionId': subscriptionId
            }
        }
        return subscriptionReference

    @staticmethod
    def buildRenewMessage(producer, subscriptionId):
        subscriptionReference = MessageBuilder._buildSubscriptionReferenceSubpart(producer, subscriptionId)
        message = {
            'TerminationTime': None,
            'SubscriptionReference': subscriptionReference
        }
        return message

    @staticmethod
    def buildPauseSubscriptionMessage(producer, subscriptionId):
        subscriptionReference = MessageBuilder._buildSubscriptionReferenceSubpart(producer, subscriptionId)
        return { 'SubscriptionReference': subscriptionReference }

    @staticmethod
    def buildResumeSubscriptionMessage(producer, subscriptionId):
        subscriptionReference = MessageBuilder._buildSubscriptionReferenceSubpart(producer, subscriptionId)
        return { 'SubscriptionReference': subscriptionReference }

    @staticmethod
    def buildUnsubscribeMessage(producer, subscriptionId):
        subscriptionReference = MessageBuilder._buildSubscriptionReferenceSubpart(producer, subscriptionId)
        return { 'SubscriptionReference': subscriptionReference }


class WrappedFunctionFactory(object):

    def __init__(self, sp):
        self._sp = sp

    def __getattr__(self, name):
        spFunction = self._getServiceProxyFunction(name)
        builderFunction = WrappedFunctionFactory._getMessageBuilderFunction(name)

        def wrappedFunction(*args, **kwargs):
            message = builderFunction(*args, **kwargs)
            return spFunction(message)
        wrappedFunction.__name__ = name

        return wrappedFunction

    @staticmethod
    def getArguments(name):
        builderFunction = WrappedFunctionFactory._getMessageBuilderFunction(name)
        code = builderFunction.func_code

        def getDefaultValue(i):
            if builderFunction.func_defaults is None:
                return None
            try:
                return builderFunction.func_defaults[code.co_argcount - i - 1]
            except IndexError:
                return None

        args = [ (code.co_varnames[i], getDefaultValue(i)) for i in range(code.co_argcount) ]
        return args

    def _getServiceProxyFunction(self, name):
        spFunction = getattr(self._sp, name, None)
        if not callable(spFunction):
            raise AttributeError
        return spFunction

    @staticmethod
    def _getMessageBuilderFunction(name):
        if name is None:
            raise AttributeError
        builderFunctionName = 'build%sMessage' % (name[0].upper() + name[1:])
        builderFunction = getattr(MessageBuilder, builderFunctionName, None)
        if not callable(builderFunction):
            raise AttributeError
        return builderFunction


class ClientFactory(object):

    @staticmethod
    def _instantiateClient(wsdlPath, webServiceURL,
                           certFilePath, privateKeyFilePath,
                                         privateKeyPassword = None):
        signatureHandler = SignatureHandler(certFilePath, privateKeyFilePath,
                                                          privateKeyPassword)
        sp = ServiceProxy(wsdlPath, url=webServiceURL,
                          sig_handler=signatureHandler)
        return WrappedFunctionFactory(sp)

    @staticmethod
    def instantiateOSCARSClient(*args, **kwargs):
        return ClientFactory._instantiateClient(OSCARS_WSDL_PATH,
                                                *args, **kwargs)

    @staticmethod
    def instantiateOSCARSNotifyClient(*args, **kwargs):
        return ClientFactory._instantiateClient(OSCARSNOTIFY_WSDL_PATH,
                                                *args, **kwargs)

