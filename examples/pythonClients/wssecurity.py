# -*- coding: utf-8 -*-

import base64
import re
from datetime import datetime, timedelta
from M2Crypto import BIO, DSA, X509
from xml.dom.ext.reader.PyExpat import Reader
from xml import xpath
from xml.xpath.Context import Context
from ZSI.wstools.c14n import Canonicalize
from ZSI.wstools.Namespaces import DSIG, OASIS

try:
    from hashlib import sha1
except ImportError:
    from sha import sha as sha1


processorNamespaces = {
    'ds': DSIG.BASE,
    'wsu': OASIS.UTILITY,
    'wsse': OASIS.WSSE
}


class SignatureGenerationException(Exception):
    def __init__(self, signatureValue):
        self.signatureValue = signatureValue

    def __str__(self):
        return repr(self.signatureValue)


class SignatureHandler(object):

    def __init__(self, certFilePath, privateKeyFilePath,
                 privateKeyPassword = None):
        self._certFilePath = certFilePath
        self._privateKeyFilePath = privateKeyFilePath
        self._privateKeyPassword = privateKeyPassword


    def _loadCertificate(self):
        '''Loads the X509 certificate as a string'''

        x509Cert = X509.load_cert(self._certFilePath)
        x509CertPart = re.compile(r'-----BEGIN CERTIFICATE-----\n?(.*?)\n?-----END CERTIFICATE-----', re.S)
        return x509CertPart.findall(x509Cert.as_pem())[0].replace('\n', '')


    def _appendSecurityElement(self, soapWriter):
        '''Creates and appends the <Security> element'''

        # Set namespace attributes in the SOAP header
        soapWriter._header.setNamespaceAttribute('wsse', OASIS.WSSE)
        soapWriter._header.setNamespaceAttribute('wsu', OASIS.UTILITY)
        soapWriter._header.setNamespaceAttribute('ds', DSIG.BASE)

        wsseSecurityElement = soapWriter._header.createAppendElement(OASIS.WSSE, 'Security')
        wsseSecurityElement.setNamespaceAttribute('wsse', OASIS.WSSE)
        #wsseSecurityElement.node.setAttribute('SOAP-ENV:mustUnderstand', 'true')

        # Add references to the message body
        soapWriter.body.setNamespaceAttribute('wsu', OASIS.UTILITY)
        soapWriter.body.node.setAttribute('wsu:Id', 'body')

        self._appendBinarySecurityTokenElement(wsseSecurityElement)
        self._appendAndComputeSignatureElement(soapWriter, wsseSecurityElement)
        self._appendTimestampElement(wsseSecurityElement)


    def _appendBinarySecurityTokenElement(self, wsseSecurityElement):
        '''Creates and appends the <BinarySecurityToken> element, loading the
           certificate from file'''

        x509Cert = self._loadCertificate()
        binarySecurityTokenElement = wsseSecurityElement.createAppendElement(OASIS.WSSE, 'BinarySecurityToken')
        binarySecurityTokenElement.setNamespaceAttribute('wsu', OASIS.UTILITY)
        binarySecurityTokenElement.node.setAttribute('EncodingType', OASIS.X509TOKEN.Base64Binary)
        binarySecurityTokenElement.node.setAttribute('ValueType', OASIS.X509TOKEN.X509 + 'v3')
        binarySecurityTokenElement.node.setAttribute('wsu:Id', 'binaryToken')
        binarySecurityTokenElement.createAppendTextNode(x509Cert)


    def _appendTimestampElement(self, wsseSecurityElement, validity=5):
        '''Creates and appends the <Timestamp> element with the given validity
           (in minutes)'''

        timestampElement = wsseSecurityElement.createAppendElement(OASIS.UTILITY, 'Timestamp')
        timestampElement.setNamespaceAttribute('wsu', OASIS.UTILITY)
        timestampElement.node.setAttribute('wsu:Id', 'timestamp')
        createdElement = timestampElement.createAppendElement(OASIS.UTILITY, 'Created')
        createdElement.createAppendTextNode(datetime.utcnow().isoformat() + 'Z')
        expiresElement = timestampElement.createAppendElement(OASIS.UTILITY, 'Expires')
        expiresElement.createAppendTextNode((datetime.utcnow() + timedelta(seconds=validity * 60)).isoformat() + 'Z')


    def _appendAndComputeSignatureElement(self, soapWriter, wsseSecurityElement):
        '''Creates and appends the <Signature> element and its children'''

        # Signature
        signatureElement = wsseSecurityElement.createAppendElement(DSIG.BASE, 'Signature')
        signatureElement.setNamespaceAttribute('ds', DSIG.BASE)
        signatureElement.node.setAttribute('Id', 'Signature')

        # Signature -> SignedInfo
        signedInfoElement = signatureElement.createAppendElement(DSIG.BASE, 'SignedInfo')

        # Signature -> SignedInfo -> CanonicalizationMethod
        canonicalizationMethodElement = signedInfoElement.createAppendElement(DSIG.BASE, 'CanonicalizationMethod')
        canonicalizationMethodElement.node.setAttribute('Algorithm', DSIG.C14N_EXCL)

        # Signature -> SignedInfo -> SignatureMethod
        signatureMethodElement = signedInfoElement.createAppendElement(DSIG.BASE, 'SignatureMethod')
        signatureMethodElement.node.setAttribute('Algorithm', DSIG.SIG_DSA_SHA1)

        # Get referenced nodes and add a <Reference> for each
        referencedNodes = self._getReferencedNodes(soapWriter)

        for node in referencedNodes:
            uri = u'#' + node.attributes[(OASIS.UTILITY, 'Id')].value

            # Only sign the body
            if uri == u'#body':
                canonicalizedReference = Canonicalize(node, unsuppressedPrefixes=['SOAP-ENV', 'wsu'])
                digestValue = base64.b64encode(sha1(canonicalizedReference).digest())
                self._appendReferenceElement(signedInfoElement, uri, digestValue)

        # Signature -> SignatureValue
        signatureValueElement = signatureElement.createAppendElement(DSIG.BASE, 'SignatureValue')
        self._computeAndStoreSignature(soapWriter, signatureValueElement)

        # Signature -> KeyInfo
        keyInfoElement = signatureElement.createAppendElement(DSIG.BASE, 'KeyInfo')
        keyInfoElement.node.setAttribute('Id', 'KeyId')

        # Signature -> KeyInfo -> SecurityTokenReference
        securityTokenReferenceElement = keyInfoElement.createAppendElement(OASIS.WSSE, 'SecurityTokenReference')
        securityTokenReferenceElement.setNamespaceAttribute('wsu', OASIS.UTILITY)
        securityTokenReferenceElement.node.setAttribute('wsu:Id', 'STRId')

        # Signature -> KeyInfo -> SecurityTokenReference -> Reference
        referenceElement = securityTokenReferenceElement.createAppendElement(OASIS.WSSE, 'Reference')
        referenceElement.node.setAttribute('URI', '#binaryToken')
        referenceElement.node.setAttribute('ValueType', OASIS.X509TOKEN.X509 + 'v3')

    def _getReferencedNodes(self, soapWriter):
        '''Looks for nodes to be signed'''

        # Evaluate the whole document and find nodes with references
        document = Reader().fromString(str(soapWriter))
        context = Context(document, processorNss=processorNamespaces)
        return xpath.Evaluate('//*[@wsu:Id]', contextNode=document, context=context)


    def _appendReferenceElement(self, signedInfoElement, uri, digestValue):
        '''Creates and appends a <Reference> element for the given URI,
           with the given digest value'''

        referenceElement = signedInfoElement.createAppendElement(DSIG.BASE, 'Reference')
        referenceElement.node.setAttribute('URI', uri)
        transformsElement = referenceElement.createAppendElement(DSIG.BASE, 'Transforms')
        transformElement = transformsElement.createAppendElement(DSIG.BASE, 'Transform')
        transformElement.node.setAttribute('Algorithm', DSIG.C14N_EXCL)
        digestMethodElement = referenceElement.createAppendElement(DSIG.BASE, 'DigestMethod')
        digestMethodElement.node.setAttribute('Algorithm', DSIG.DIGEST_SHA1)
        digestValueElement = referenceElement.createAppendElement(DSIG.BASE, 'DigestValue')
        digestValueElement.createAppendTextNode(digestValue)


    def _computeAndStoreSignature(self, soapWriter, signatureValueElement):
        '''Computes and stores the message signature in the <SignatureValue> node'''

        # Evaluate the whole document and find the <SignedInfo> element
        document = Reader().fromString(str(soapWriter))
        context = Context(document, processorNss=processorNamespaces)
        signedInfoNode = xpath.Evaluate('//ds:SignedInfo', contextNode=document, context=context)[0]

        # Apply exclusive canonicalization and compute its digest
        canonicalizedSignedInfo = Canonicalize(signedInfoNode, unsuppressedPrefixes=[])
        signedInfoDigestValue = sha1(canonicalizedSignedInfo).digest().strip()

        # Sign the digest and store the signature value in the <SignatureValue> element
        encodedSignatureValue = self._getDigestValueSignature(signedInfoDigestValue)
        signatureValueElement.createAppendTextNode(encodedSignatureValue)


    def _getDigestValueSignature(self, digestValue):
        '''Computes the signature value for the given digest'''

        # Load the private key from file, providing its password (if needed)
        privateKeyFile = BIO.File(open(self._privateKeyFilePath))
        privateKey = DSA.load_key_bio(privateKeyFile,
                                      callback=lambda *args, **kwargs: self._privateKeyPassword)

        # Compute the signature
        signatureValueR, signatureValueS = privateKey.sign(digestValue)

        # Take into account the format OpenSSL/M2Crypto uses
        signatureValueR, signatureValueS = self._i2osp(signatureValueR), self._i2osp(signatureValueS)

        return base64.encodestring(signatureValueR + signatureValueS).strip()


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


    def sign(self, soapWriter):
        '''Signs outgoing requests'''

        self._appendSecurityElement(soapWriter)


    def verify(self, parsedSoap):
        '''Verifies incoming responses'''

        # No verification has to be performed
        pass

