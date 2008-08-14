Introduction
############

Thank you for your interest in PyOSCARS.

What is PyOSCARS?
*****************

PyOSCARS is a Python client for the `OSCARS <http://www.es.net/oscars/>`_ [#]_
Web service.
It implements all messages, including the IDC event notification interface [#]_.

Development started during `Google Summer of Code <http://code.google.com/soc/>`_
2008.


.. rubric:: Footnotes

.. [#] On-demand Secure Circuits and Advance Reservation System
.. [#] Also known as OSCARSNotify


Why another client?
*******************

OSCARS and OSCARSNotify already have an extensive set of Java clients which can
be used to interact with IDCs both from the command line and your own programs.

However, it is important for a project that aims to be deployed in different
contexts to make sure that developers are not forced to use the programming
language in which the server is written.
This is also a guarantee of interoperability, and helps avoid the infamous
lock-in problem.



