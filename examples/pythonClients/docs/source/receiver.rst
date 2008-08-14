.. _receiver:

OSCARSNotify receiver
#####################

The OSCARSNotify receiver is a small server that is able to receive and
interpret ``Notify`` messages coming from an OSCARSNotify server to which the
user previously subscribed.


Observers
*********

The server implements the `observer pattern <http://en.wikipedia.org/wiki/Observer_pattern>`_,
allowing you to register your observers, to which ``Notify`` messages will be
sent in the form of a Python dictionary.

The simplest observer (which is also the one you will find in
``notify-server.py``) is the following::

    class SampleObserver:
        def update(self, notification):
            pprint(notification)

As you can see, the observer must implement an ``update`` method taking just one
argument, the incoming ``Notify`` message.

To start receiving notifications, you just need to ``attach`` the observer to
the ``NotificationHandler``::

    handler = NotificationHandler()
    handler.attach(SampleObserver())

The ``update`` method of your observer will now be called every time a ``Notify``
message is received.


SSL
***

Since the OSCARSNotify receiver is based on the famous
`CherryPy <http://www.cherrypy.org/>`_ HTTP framework, it is easy to enable
SSL encryption (like it is done in ``notify-server.py``), or run the receiver
behind another Web server.

Please refer to the `CherryPy documentation <http://www.cherrypy.org/wiki/TableOfContents>`_
for more information.

