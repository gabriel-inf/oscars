from ZSI.ServiceContainer import ServiceContainer, SOAPRequestHandler
from OSCARSNotify_services_server import OSCARSNotify
import os

class CustomSOAPRequestHandler(SOAPRequestHandler):
    def do_GET(self):
        wsdl = open('wsdl/OSCARS-Notify.wsdl').read()
        self.send_xml(wsdl)

class SOAPServer:
    def __init__(self, host, port=8080, services=(), reqHandlerClass=SOAPRequestHandler):
        self._hostname = host
        self._port = port
        self._services = services
        self._reqHandlerClass = reqHandlerClass

    def start(self):
        address = (self._hostname, self._port)
        sc = ServiceContainer(address, (), self._reqHandlerClass)
        for service in self._services:
            path = service.getPost()
            sc.setNode(service, path)
        sc.serve_forever()

if __name__ == '__main__':
    cs = [OSCARSNotify()]
    s = SOAPServer('localhost', 8080, cs, reqHandlerClass=CustomSOAPRequestHandler)
    s.start()
