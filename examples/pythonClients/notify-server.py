import cherrypy
from pprint import pprint
from ZSI import ParsedSoap, ParseException, EvaluateException
from ZSI.TC import AnyElement, Struct

NotifyTC = Struct(None,
                  [AnyElement(aname='content', minOccurs=0, maxOccurs='unbounded')],
                  pname='Notify',
                  minOccurs=1)

class SampleObserver:
    def update(self, notification):
        pprint(notification)

class NotificationHandler:
    def __init__(self):
        self._observers = []

    def attach(self, observer):
        if observer not in self._observers and self._isValidObserver(observer):
            self._observers.append(observer)

    def detach(self, observer):
        try:
            self._observers.remove(observer)
        except ValueError:
            pass

    @cherrypy.expose
    def index(self):
        body = cherrypy.request.body
        if body is None:
            raise cherrypy.HTTPError(message='Invalid request')
        notification = self._processRequest(body)
        self._notifyObservers(notification)

    def _processRequest(self, request):
        try:
            ps = ParsedSoap(request)
            notification = ps.Parse(NotifyTC)
            return notification['content']
        except ParseException, e:
            raise cherrypy.HTTPError(message=str(e))
        except EvaluateException, e:
            raise cherrypy.HTTPError(message=str(e))

    def _notifyObservers(self, notification):
        for observer in self._observers:
            observer.update(notification)

    def _isValidObserver(self, observer):
        updateMethod = getattr(observer, 'update', None) 
        return callable(updateMethod)

handler = NotificationHandler()
handler.attach(SampleObserver())

cherrypy.config.update({'server.socket_port': 8443,
                        'environment': 'production',
                        'log.screen': True,
                        'server.ssl_certificate': 'server.crt',
                        'server.ssl_private_key': 'server.key'})

cherrypy.quickstart(handler)

