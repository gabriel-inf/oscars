import cherrypy
from pprint import pprint
from ZSI import ParsedSoap, ParseException, EvaluateException
from ZSI.TC import AnyElement, Struct

NotifyTC = Struct(None,
                  [AnyElement(aname='content', minOccurs=0, maxOccurs='unbounded')],
                  pname='Notify',
                  minOccurs=1)

class NotificationHandler:
    @cherrypy.expose
    def index(self):
        body = cherrypy.request.body
        if body is None:
            raise cherrypy.HTTPError(message='Invalid request')
        notification = self._process(cherrypy.request.body)
        pprint(notification)

    def _process(self, request):
        try:
            ps = ParsedSoap(request)
            notification = ps.Parse(NotifyTC)
            return notification['content']
        except ParseException, e:
            raise cherrypy.HTTPError(message=str(e))
        except EvaluateException, e:
            raise cherrypy.HTTPError(message=str(e))

cherrypy.quickstart(NotificationHandler())

