.. _cli:

Command line interface
######################

PyOSCARS comes with a series of scripts you can use from the command line.
You can use them to issue any OSCARS and OSCARSNotify command.


Setup
*****

The command line interface is made up by:

* ``client.py``, which handles OSCARS messages;
* ``notify-client.py`` which is used for OSCARSNotify.

Since both scripts rely on the file name they were called as to determine the
operation they need to perform, it is necessary to set up a number of symbolic
links and use these instead.

To do that, you can use the provided ``symlinks_setup.py`` utility::

    oscars@pyoscars-dev:~/PyOSCARS$ ./symlinks_setup.py --help

    Usage: symlinks_setup.py [options]

    Options:
      -h, --help            show this help message and exit
      -c, --create          
      -r, --remove          
      -x, --executable      
      --client-path=CLIENTPATH
                            directory containing the OSCARS client files
      --symlinks-path=SYMLINKSPATH
                            directory where the symlinks will be created

The usage is simple: you need to specify if you want the script to create the
symbolic links (``--create``), or remove them (``--remove``).
If you are creating them, you can also specify that they should be set
executable (``--executable``).
Furthermore, you can specify the path where the two client files reside, and
where the symbolic links should be created. Both these options default to the
current working directory.


Usage
*****

Now that symbolic links are in place, we can start using the command line
interface to interact with the IDC.
All commands can be called with the ``--help`` argument to get a list of the
arguments they need.

OSCARS
======

For example, if we want to create a reservation, but do not remember the
arguments needed, we may issue the following command::

    oscars@pyoscars-dev:~/PyOSCARS$ ./createReservation.py  --help

    Usage: createReservation.py [options]

    Options:
      -h, --help            show this help message and exit
      -u URL, --url=URL     Web service URL
      -c CERT, --cert=CERT  Path to the certificate file
      -k KEY, --key=KEY     Path to the private key file
      -p PASSWORD, --password=PASSWORD
                        Private key password (if any)
      --durationSecs=DURATIONSECS
      --bandwidth=BANDWIDTH
      --desc=DESC           
      --srcEndpoint=SRCENDPOINT
      --destEndpoint=DESTENDPOINT

Once we have gathered the required information, we can ask the client to create
the reservation, getting back the server response::

    oscars@pyoscars-dev:~/PyOSCARS$ ./createReservation.py -u https://anna-lab1.internet2.edu:8443/axis2/services/OSCARS -c cert.cer -k key.pem \
    --durationSecs=60 --bandwidth=100 --desc="Test" --srcEndpoint="urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1" \
    --destEndpoint="urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=1-1-1:link=1"

    {'globalReservationId': u'anna.internet2.edu-92',
     'pathInfo': {'layer2Info': {'destEndpoint': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=1-1-1:link=1',
                                 'srcEndpoint': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'},
                  'pathSetupMode': u'user-xml'},
     'status': u'ACCEPTED',
     'token': u'none'}

After a while, we might for example want to query the reservation we have just
created::

    oscars@pyoscars-dev:~/PyOSCARS$ ./queryReservation.py -u https://anna-lab1.internet2.edu:8443/axis2/services/OSCARS -c cert.cer -k key.pem \
    --globalReservationId=anna.internet2.edu-92

    {'bandwidth': 100,
     'createTime': 1218627710,
     'description': u'Test',
     'endTime': 1218627768,
     'globalReservationId': u'anna.internet2.edu-92',
     'login': u'pythontest',
     'pathInfo': {'layer2Info': {'destEndpoint': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr2:port=1-1-1:link=1',
                                 'srcEndpoint': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'},
                  'path': {'_attrs': {'id': u'unimplemented'},
                           'hop': [{'_attrs': {'id': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'},
                                    'linkIdRef': u'urn:ogf:network:domain=anna.internet2.edu:node=anna-vlsr1:port=1-1-1:link=1'}]},
                  'pathSetupMode': u'timer-automatic'},
     'startTime': 1218627708,
     'status': u'FAILED'}

OSCARSNotify
============

Another common task is to subscribe to the OSCARSNotify service, in order to get
callbacks from the IDC.
Once again, we can easily do that using the command line interface.

For example, if we want to get called every time the IDC produces a message at
the ``INFO`` level [#]_, we can call the client like this::

    oscars@pyoscars-dev:~/PyOSCARS$ ./subscribe.py -u https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify -c cert.cer -k key.pem \
    --consumer=https://consumer.example.com:8443/ --topics="idc:INFO" --producers=https://anna-lab1.internet2.edu:8443/axis2/services/OSCARS

    {'CurrentTime': datetime.datetime(2008, 8, 13, 11, 55, 30),
     'SubscriptionReference': {'Address': u'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify',
                               'ReferenceParameters': {'subscriptionId': u'urn:uuid:3bf5bedb-9535-4b7f-bd4d-fb97d082e43b'},
                               'any': []},
     'TerminationTime': datetime.datetime(2008, 8, 13, 12, 55, 30),
     'any': []}

After a while, we may also decide to unsubscribe::

    oscars@pyoscars-dev:~/PyOSCARS$ ./unsubscribe.py -u https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify -c cert.cer -k key.pem \
    --producer=https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify --subscriptionId=urn:uuid:3bf5bedb-9535-4b7f-bd4d-fb97d082e43b

    {'SubscriptionReference': {'Address': u'https://anna-lab1.internet2.edu:8443/axis2/services/OSCARSNotify',
                               'ReferenceParameters': {'subscriptionId': u'urn:uuid:3bf5bedb-9535-4b7f-bd4d-fb97d082e43b'},
                               'any': []},
     'any': []}


.. rubric:: Footnotes

.. [#] We could also specify more topics, by separating them with the ``|``
       symbol.


Final remarks
*************

I focused on the main commands you may want to send to an IDC, but had to leave
out many others.

The commands share a predictable interface, though, so you should be able to use
them without problems after the first few tries.

