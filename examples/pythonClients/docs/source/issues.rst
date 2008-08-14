Development issues
##################

PyOSCARS is built on top of different Python libraries, of which the most
prominent is probably `ZSI <http://pywebsvcs.sourceforge.net/>`_, the SOAP
library.

Since IDCs are providing their services through
`Apache Axis2 <http://ws.apache.org/axis2/>`_, it was crucial to make sure that
the two software could exchange information.

What follows is a brief account of the major problems encountered during
development, as well as their solutions.


SOAP
****

ZSI implements SOAP 1.1, while the IDC interface requires SOAP 1.2.

This is a serious problem, because the two are unable to speak to each other,
and it also prevents the client from automatically generating code from the WSDL
files.

Namespaces
==========

Solving this problem required changing all the SOAP 1.1 namespace references in
the ZSI code to their respective SOAP 1.2 counterparts.

Faults
======

A SOAP fault is a special kind of message (defined in the SOAP WSDL) that is
used to notify problems.

Since the models for SOAP 1.1 fault messages are hardwired in ZSI, which causes
it to fail whenever Axis2 sends a SOAP 1.2 fault messages its way, a rewrite of
the fault handling code was required.

As a result, SOAP faults are now thrown as Python exceptions, passing over the
message that the server sent as motivation.


.. _ws-sec:

WS-Security and XML-DSig
************************

`OASIS Web Services Security <http://www.oasis-open.org/committees/wss/>`_
(WS-Security) is used to guarantee the integrity of messages as they travel from
the client to the server, and to identify the client by associating the digital
certificate in the message to the corresponding IDC user.
All outgoing messages are signed by the client according to the
`XML Signature Syntax and Processing W3C Recommendation <http://www.w3.org/TR/xmldsig-core/>`_
(XML-DSig).

ZSI provides no support for WS-Security, but has the ability to use a so-called
``SignatureHandler``, which is called to sign outgoing requests and verify
incoming responses.
WS-Security has been implemented in PyOSCARS as a ``SignatureHandler`` which
only handles signing, as no verification is currently needed.

XML Canonicalization
====================

WS-Security relies on `Exclusive XML Canonicalization <http://www.w3.org/TR/xml-exc-c14n/>`_
to make sure that similar XML documents (or parts thereof) are represented in
the same way, thus allowing signature verification.

The process involves two canonicalizations, namely:

* Canonicalization of the elements with a ``wsu:Id`` attribute, which are then
  referred to in the ``<SignedInfo>`` element, along with their hash values;

* Canonicalization of the ``<SignedInfo>`` element, which is then hashed and
  signed to obtain the final ``<SignatureValue>`` element value.

Regarding the first canonicalization, the IDC seems to accept only messages in
which the ``<SignedInfo>`` element refers only to the SOAP body, despite other
elements (for example the ``<Timestamp>``) having the ``wsu:Id`` attribute.
Moreover, this first canonicalization has to be carried out by suppressing all
namespace prefixes, but ``SOAP-ENV`` and ``wsu``.

The second canonicalization, instead, is carried out on the ``<SignedInfo>``
element only, by suppressing all namespaces prefixes.

Signature generation
====================

Signatures are generated using `M2Crypto <http://chandlerproject.org/Projects/MeTooCrypto>`_,
which is based on the famous `OpenSSL <http://www.openssl.org/>`_ library.

Both OpenSSL and M2Crypto prepend to the actual signature value a byte count,
which needs to be eliminated according to the XML-DSig standard.
This is done by the ``_i2osp`` function in ``wssecurity.py``::

    def _i2osp(self, signatureValue):
        '''Converts a OpenSSL/M2Crypto DSA signature to WS-Security format'''

        # Remove leading zeros
        signatureValue = signatureValue.lstrip('\x00')

        # Find out how many bytes will follow
        takeHowManyBytes = ord(signatureValue[0])

        # Take that many bytes, less any leading zeros
        signatureValue = signatureValue[-takeHowManyBytes:].lstrip('\x00')

        # This should give a signature length of 20 bytes, as mandated by the
        # XML-Signature specification
        if len(signatureValue) != 20:
            raise SignatureGenerationException(signatureValue)

        return signatureValue


ZSI
***

ZSI is a good SOAP library, but it sometimes lacks features already present in
other parts of the code.

I will briefly present two cases that exemplify this situation.

SignatureHandler
================

As we know from :ref:`above <ws-sec>`, ZSI has the ability to use a
``SignatureHandler`` class as a filter for incoming and outgoing messages.

This is true for the ``Binding``, which is a commonly used class for sending
messages where the model is already encoded as a Python object.

Using a ``ServiceProxy`` [#]_, however, there is no way to specify which object
should act as the ``SignatureHandler``, even though, behind the scenes, the
``ServiceProxy`` is doing nothing more than generating the models and creating a
``Binding``.


.. rubric:: Footnotes

.. [#] A class that turns WSDL files into code at runtime, caching the result
       to increase performance.

XML attributes
==============

XML attributes are used in one specific message, the ``Subscribe`` method of
OSCARSNotify, to indicate the ``Dialect`` of the two elements
``<TopicExpression>`` and ``<ProducerProperties>``.

Apparently, ZSI provides no easy way to encode these attributes using a Python
structure (the usual dictionary), but requires instantiation of classes to hold
these values.

To get around this problem, I extended the syntax of ZSI simple types to also
accept 2-tuples holding the attribute name and its value.

For example, let us have a look at a sample ``Subscribe`` message, generated
using the ``MessageBuilder``::

    >>> MessageBuilder.buildSubscribeMessage('consumer', 'topic', 'producer')

    {'ConsumerReference': {'Address': 'consumer'},
     'Filter': {'ProducerProperties': [[('Dialect',
                                         'http://www.w3.org/TR/1999/REC-xpath-19991116'),
                                        "/wsa:Address='producer'"]],
                'TopicExpression': [[('Dialect',
                                      'http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full'),
                                     'topic']]}}

As you can see, ``TopicExpression`` maps to a list of lists, each having the
required ``Dialect`` attribute and its value, and the actual value (the topic).
This also allows for creating two topics with a different ``Dialect``.

