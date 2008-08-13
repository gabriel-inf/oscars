#!/usr/bin/env python
# -*- coding: utf-8 -*-

import errno
import os
import stat
import sys
from optparse import OptionParser

OSCARS_CLIENT_FILE = 'client.py'
OSCARSNOTIFY_CLIENT_FILE = 'notify-client.py'


SYMLINKS = {
    'createReservation.py': OSCARS_CLIENT_FILE,
    'queryReservation.py': OSCARS_CLIENT_FILE,
    'cancelReservation.py': OSCARS_CLIENT_FILE,
    'subscribe.py': OSCARSNOTIFY_CLIENT_FILE,
    'renew.py': OSCARSNOTIFY_CLIENT_FILE,
    'pauseSubscription.py': OSCARSNOTIFY_CLIENT_FILE,
    'resumeSubscription.py': OSCARSNOTIFY_CLIENT_FILE,
    'unsubscribe.py': OSCARSNOTIFY_CLIENT_FILE
}


if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-c', '--create', dest='create', action='store_true', default=False)
    parser.add_option('-r', '--remove', dest='remove', action='store_true', default=False)
    parser.add_option('-x', '--executable', dest='executable', action='store_true', default=False)
    parser.add_option('--client-path', dest='clientPath', default=os.getcwd(),
                      help='directory containing the OSCARS client files')
    parser.add_option('--symlinks-path', dest='symlinksPath', default=os.getcwd(),
                      help='directory where the symlinks will be created')
    options, args = parser.parse_args()

    def printErrorAndQuit(errorMessage):
        parser.print_help(sys.stderr)
        print >> sys.stderr, '\n%s' % errorMessage
        sys.exit(1)

    if not options.create ^ options.remove:
        printErrorAndQuit('No operation specified')

    if not os.path.isdir(options.clientPath):
        printErrorAndQuit('"%s" is not a valid path' % options.clientPath)

    oscarsClient = os.path.join(options.clientPath, OSCARS_CLIENT_FILE)
    if not os.path.isfile(oscarsClient):
        printErrorAndQuit('"%s" not found in "%s"' % (OSCARS_CLIENT_FILE, options.clientPath))

    oscarsNotifyClient = os.path.join(options.clientPath, OSCARSNOTIFY_CLIENT_FILE)
    if not os.path.isfile(oscarsNotifyClient):
        printErrorAndQuit('"%s" not found in "%s"' % (OSCARSNOTIFY_CLIENT_FILE, options.clientPath))

    if not os.path.isdir(options.symlinksPath):
        printErrorAndQuit('"%s" is not a valid path' % options.symlinksPath)

    for name, client in SYMLINKS.iteritems():
        symlinkAbsPath = os.path.join(options.symlinksPath, name)
        clientAbsPath = None
        if client == OSCARS_CLIENT_FILE:
            clientAbsPath = oscarsClient
        elif client == OSCARSNOTIFY_CLIENT_FILE:
            clientAbsPath = oscarsNotifyClient

        if options.create:
            try:
                os.symlink(clientAbsPath, symlinkAbsPath)
            except OSError, e:
                if e.errno == errno.EEXIST:
                    print >> sys.stderr, 'Skipping existent symlink "%s"' % symlinkAbsPath
            if options.executable:
                os.chmod(clientAbsPath,
                         stat.S_IRWXU | stat.S_IRGRP | stat.S_IXGRP | stat.S_IROTH | stat.S_IXOTH)
        else:
            try:
                os.remove(symlinkAbsPath)
            except OSError, e:
                if e.errno == errno.ENOENT:
                    print >> sys.stderr, 'Skipping inexistent symlink "%s"' % symlinkAbsPath

