Requirements
############

Apart from a recent version of the Python interpreter, PyOSCARS needs the
following libraries to do its work:

* `M2Crypto <http://chandlerproject.org/Projects/MeTooCrypto>`_
* `PyXML <http://pyxml.sourceforge.net/>`_

On Debian GNU/Linux and derivatives, you can easily install them as follows::

    root@pyidc-dev:~# apt-get install python-m2crypto python-xml

Despite relying on `ZSI <http://pywebsvcs.sourceforge.net/>`_, you do **not**
need to install ZSI yourself, as a patched version is provided.

If you also want to use the :ref:`OSCARSNotify receiver <receiver>`, you need
two additional packages:

* `CherryPy <http://www.cherrypy.org/>`_ 3
* `pyOpenSSL <http://pyopenssl.sourceforge.net/>`_
  (only needed if you want SSL support in CherryPy)

On Debian GNU/Linux and derivatives, you can easily install them as follows::

    root@pyidc-dev:~# apt-get install python-cherrypy3 python-pyopenssl

