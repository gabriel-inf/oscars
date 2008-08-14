API
###

PyOSCARS exposes an API [#]_ that you can use to build your own applications.
The :ref:`command line interface <cli>` itself uses the API, so this page might
also help you get a better understanding of how those scripts work.

The API can be used at two levels of abstraction, depending on what your needs
are.
Usually, you will want to use the :ref:`with models <with_models>` approach,
since it hides most details behind its interface.

There are cases, however, where you need to program :ref:`without models <without_models>`,
for example because you want more flexibility.
Also remember that, in this case, the interface you will be using is the same
models are built on top of, so it should be easy to adapt them to your needs and
regain a similar high-level interface.

.. rubric:: Footnotes

.. [#] Application Programming Interface


.. _with_models:

With models
***********

The main entry point in the code is the ``ClientFactory``.
This class instantiates client objects both for OSCARS and OSCARSNotify.

The client objects that the two static functions ``instantiateOSCARSClient`` and
``instantiateOSCARSNotifyClient`` return are proxies: the methods they expose
are in fact calls to specific methods of the ``MessageBuilder``, which turns the
arguments into a Python structure (usually a dictionary), which is then encoded
as a SOAP message and sent to the server.

The ``MessageBuilder`` provides a convenient interface for calling methods
without having to provide the whole Python structure, but it also limits you,
because it has to make some assumptions on what your request is about.
If you are interested in building your application without models, have a look
at the :ref:`Without models <without_models>` section.

To start things off, let us get a client object for OSCARS::

    client = ClientFactory.instantiateOSCARSClient(webServiceURL,
                                                   certFilePath,
                                                   privateKeyFilePath,
                                                   privateKeyPassword=None)

We can now easily call IDC methods, for example to query the status of a
reservation::

    client.queryReservation(globalReservationId)

The result is a Python dictionary containing the parsed SOAP response::

    >>> client.queryReservation('anna.internet2.edu-32')

    {'bandwidth': 100,
     'createTime': 1215111940,
     'description': u'test',
     'endTime': 1215112140,
     'globalReservationId': u'anna.internet2.edu-32',
     'login': u'andy',
     'pathInfo': {'layer2Info': {'destEndpoint': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1',
                                 'destVtag': u'3000',
                                 'srcEndpoint': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=E1-1-1:link=1',
                                 'srcVtag': u'3000'},
                  'path': {'_attrs': {'id': u'unimplemented'},
                           'hop': [{'_attrs': {'id': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=E1-1-1:link=1'},
                                    'linkIdRef': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=E1-1-1:link=1'},
                                   {'_attrs': {'id': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=1-1-12:link=10.10.3.2'},
                                    'linkIdRef': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=1-1-12:link=10.10.3.2'},
                                   {'_attrs': {'id': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-12:link=10.10.3.1'},
                                    'linkIdRef': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-12:link=10.10.3.1'},
                                   {'_attrs': {'id': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'},
                                    'linkIdRef': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'}]},
                  'pathSetupMode': u'timer-automatic'},
     'startTime': 1215111900,
     'status': u'FAILED'}

The client object is also aware of the methods it provides and the arguments
(and the corresponding default values) they need.

For example, we may want to find out what methods the OSCARS client provides::

    >>> client.getMethods()

    ['cancelReservation', 'createReservation', 'queryReservation']

Suppose now that we want to use ``createReservation``, but do not know its
arguments. In this case, we can just ask::

    >>> client.getArguments('createReservation')

    [('durationSecs', None),
     ('bandwidth', None),
     ('desc', None),
     ('srcEndpoint', None),
     ('destEndpoint', None)]

As you can see, ``getArguments`` returns a list of 2-tuples, where the first
element of the tuple is the name of the argument, and the second is its default
value, or ``None`` if the argument has no default value.


.. _without_models:

Without models
**************

Programming without models is slightly more difficult than what we saw so far,
because it involves writing manually the Python structures that are then turned
into SOAP messages.

I will present a brief code example and comment it shortly::

    from wssecurity import SignatureHandler
    from ZSI.ServiceProxy import ServiceProxy

    signatureHandler = SignatureHandler(certFilePath, privateKeyFilePath,
                                                      privateKeyPassword)
    sp = ServiceProxy(wsdlPath, url=webServiceURL,
                      sig_handler=signatureHandler)

    req = {
        'startTime': 1218698957,
        'endTime': 1218699017,
        'bandwidth': 100,
        'description': 'Test',
        'pathInfo': {
            'pathSetupMode': 'user-xml',
            'layer2Info': {
                'srcEndpoint': 'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=E1-1-1:link=1',
                'destEndpoint': 'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'
            }
        }
    }

    print sp.createReservation(req)

As you can see, instantiating the client code is still fairly easy.
What is more cumbersome is the definition of the request, which must follow the
XML Schema for the message [#]_.

My suggestion is to start from the simpler messages the ``MessageBuilder`` can
provide you with, and extend them::

    >>> MessageBuilder.buildCreateReservationMessage(60, 100, 'Test', 'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=E1-1-1:link=1',
    ... 'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1')

    {'bandwidth': 100,
     'description': 'Test',
     'endTime': 1218699017,
     'pathInfo': {'layer2Info': {'destEndpoint': 'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1',
                                 'srcEndpoint': 'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=E1-1-1:link=1'},
                  'pathSetupMode': 'user-xml'},
     'startTime': 1218698957}


.. rubric:: Footnotes

.. [#] The client knows this, and will prevent you from sending malformed
       messages.

