import logging
import ConfigParser
import math
import re
import simplejson

from nox.lib.core import *
from nox.webapps.webserver import webserver
from nox.webapps.webservice import webservice
from nox.lib.netinet import netinet

logger = logging.getLogger('nox.webapps.OSCARS.OSCARS')

default_config_file = 'OSCARS.conf'

config = ConfigParser.ConfigParser()

def getFactory():
    class Factory:
        def instance(self, ctxt):
            return OSCARS(ctxt)

    return Factory()

class OSCARS(Component):
    def __init__(self, ctxt):
        Component.__init__(self, ctxt)

    def install(self):
        inst = self

	# The config file contains, among other things, a mapping from IP
	# address to datapath id. Presumably, this could be figured out
	# automagically, but for now I'm putting it in a configuration file.
        config.read(default_config_file)

        ws = self.resolve(str(webservice.webservice))
        v1 = ws.get_version("1")
        v1.register_request(lambda request, data: self.handle_request(request, data), "POST", [ webservice.WSPathStaticString("OSCARS") ], "OSCARS JSON Target")

    def getInterface(self):
        return str(OSCARS)

    def handle_request(self, request, data):
        try:
            json_content = webservice.json_parse_message_body(request)
            if json_content == None:
                raise ValueError('Invalid JSON request')
        except Exception as e:
            lg.error("Invalid request: %s" % e.__str__())
            return webservice.badRequest(self.raw_request, e.__str__())

        return OSCARSRequestHandler(self, request, json_content).handle()

class OSCARSRequestHandler:
    def __init__(self, openflow_class, raw_request, request):
        self.openflow_class = openflow_class
        self.raw_request = raw_request
        self.request = request
        self.changes_applied = {}

    def handle(self):
        try:
            oscars_request = OSCARSRequest(self.request)

            for element in oscars_request.path:
                try:
                    dpid = long(config.get(element["switch"], "datapath_id"), 0)
                except Exception as e:
                    lg.error("Problem finding 'datapath_id' for '%s': %s" % (element["switch"], e.__str__()))
                    return webservice.badRequest(self.raw_request, "Unknown switch '%s'" % element["switch"])

            error_msg = ""
            successful = None
            try:
                if oscars_request.path:
                    logger.debug("Request has a path")
                    for path_element in oscars_request.path:
                        dpid = long(config.get(path_element["switch"], "datapath_id"), 0)

                        logger.debug("Configuring switch %s: %s" % (path_element["switch"], dpid))
    
                        for key in "del-flows", "add-flows":
    
                            logger.debug("Handling %s for switch %s/%s" % (key, path_element["switch"], dpid))
        
                            if key in path_element:
                                if len(path_element[key]) != 2:
                                    raise ValueError("The flow needs to have two elements")
    
                                if key == "del-flows":
                                    action = "delete"
                                else:
                                    action = "add"
                        
                                logger.debug("Handling %s for switch %s" % (action, path_element["switch"]))
     
                                self.modify_flow(action, dpid, path_element[key][0], path_element[key][1]);
        
                                self.modify_flow(action, dpid, path_element[key][1], path_element[key][0]);

                    successful = True
            except Exception as e:
                error_msg = "Problem configuring switch: %s" % e
                logger.error(error_msg)
                successful = False

            if successful == False:
                try:
                    self.undo_changes()
                except Exception as e:
                    logger.error("Problem undoing changes: %s" % e)
                    error_msg += ": Problem backing out changes: %s" % e

            status = ""
            if successful:
                if oscars_request.action == "setup":
                    status = "ACTIVE"
                elif oscars_request.action == "teardown":
                    status = "FINISHED"
            else:
                status = "FAILED"

            response = {
                "type": "oscars-reply",
                "version": "1.0",
                "gri": oscars_request.gri,
                "action": oscars_request.action,
                "status": status,
                "err_msg": error_msg
            }

            self.raw_request.setResponseCode(200, "Successful")
            self.raw_request.setHeader("Content-Type", "application/json")
            self.raw_request.write(simplejson.dumps(response))
            self.raw_request.finish()

            return webservice.NOT_DONE_YET

        except Exception as e:
            lg.error("Invalid request: %s" % e.__str__())
            return webservice.badRequest(self.raw_request, e.__str__())

    def modify_flow(self, action, dpid, source_port_info, destination_port_info):
        flow = {}
        flow[core.IN_PORT] = int(source_port_info["port"])
        flow[core.DL_VLAN] = int(source_port_info["vlan_range"])

        result = False
        if action == "add":
            actions = []
            actions.append([openflow.OFPAT_SET_VLAN_VID, int(destination_port_info["vlan_range"])])
            actions.append([openflow.OFPAT_OUTPUT, [0, int(destination_port_info["port"])]])

            result = self.openflow_class.install_datapath_flow(dp_id=dpid, attrs=flow, actions=actions, idle_timeout=openflow.OFP_FLOW_PERMANENT, hard_timeout=openflow.OFP_FLOW_PERMANENT)
        else:
            result = self.openflow_class.delete_datapath_flow(dp_id=dpid, attrs=flow)

        if result:
            if not dpid in self.changes_applied:
                self.changes_applied[dpid] = []
            self.changes_applied[dpid].append([action, "flow", source_port_info, destination_port_info])

    def undo_changes(self):
        for switch in self.changes_applied:

            self.changes_applied[switch].reverse()

            for change in self.changes_applied[switch]:
                undo_action = "delete" if (change[0] == "add") else "add"
                element_type = change[1]

                if (element_type == "queue"):
                    port = change[2]
                    vlan_range = change[3]
                    bandwidth = change[4]

		    # Have an 'internal' try here so that we can undo as much
		    # as possible even if we can't undo a specific element
                    try:
                        self.modify_queue(undo_action, switch, port, vlan_range, bandwidth)
                    except Exception as e:
                        logger.error("Couldn't undo queue change: %s" % e)

                elif (element_type == "flow"):
                    source_port_info = change[2]
                    destination_port_info = change[3]

                    try:
                        self.modify_flow(undo_action, switch, source_port_info, destination_port_info)
                    except Exception as e:
                        logger.error("Couldn't undo flow change: %s" % e)

                else:
                    raise ValueError("Unknown element type %s" % element_type)

class OSCARSRequest:
    def __init__(self, request):
        logger.debug("in __init__")
        if not isinstance(request, dict):
            raise ValueError('JSON request is not a hash')

        logger.debug("is a dict")
        if not request["version"]:
            raise ValueError('No version found in request')

        logger.debug("is a version")
        if request["version"] != "1.0":
            raise ValueError('Only version 1.0 requests are supported')

        logger.debug("version 1.0")

        for k, v in request.iteritems():
            if (k == "action"):
                if not isinstance(v, str):
                    raise ValueError('action must be a string')

                if v != "setup" and v != "teardown" and v != "modify" and v != "verify":
                    raise ValueError('Invalid action found in request')

                self.action = v
            elif (k == "gri"):
                if not isinstance(v, str):
                    raise ValueError('gri must be a string')

                self.gri = v
            elif (k == "path"):
                self.path = self.parse_path(request["path"])
            elif (k == "version"):
                self.version = v
            elif (k == "bandwidth"):
                self.bandwidth = convert_bandwidth_to_bps(v)
                logger.debug("Bandwidth: %s" % self.bandwidth)
            elif (k == "type"):
                continue
            else:
                raise ValueError('Unknown element %s in request' % k)

        logger.debug("finished iterating")

        if not self.action:
            raise ValueError('No action found in request')

        if not self.gri:
            raise ValueError('No gri found in request')

        if not self.path:
            raise ValueError('No path found in request')

        logger.debug("request is correct")

    def parse_path (self, path):
        logger.debug("parse_path")
        if not isinstance(path, list):
            raise ValueError('Invalid path in request')

        new_path = []
        for element in path:
            new_path.append(self.parse_path_element(element))

        return new_path

    def parse_path_element (self, element):
        logger.debug("parse_path_element")
        if not isinstance(element, dict):
            raise ValueError('Invalid path element')

        new_element = {}
        for k, v in element.iteritems():
            if (k == "switch"):
                if not isinstance(v, str):
                    raise ValueError('invalid switch element in path element')

                # xxx: validate the switch as an ip or whatever

                new_element["switch"] = v
            elif (k == "del-flows" or k == "add-flows"):
                if not isinstance(v, list):
                    raise ValueError('invalid %s element in path element' % k)
                new_element[k] = self.parse_flows(v)
            else:
                raise ValueError('Unknown element %s in path element' % k)

        if (not new_element["switch"] or
             (not "del-flows" in new_element and not "add-flows" in new_element)):
            raise ValueError('path element is missing required elements')

        return new_element

    def parse_flows (self, flows):
        logger.debug("parse_flows")
        if not isinstance(flows, list):
            raise ValueError('Invalid flows in request')

        new_flows = []
        for element in flows:
            new_flows.append(self.parse_flow_element(element))

        return new_flows

    def parse_flow_element (self, element):
        logger.debug("parse_flow_element")
        if not isinstance(element, dict):
            raise ValueError('Invalid flow description')

        new_flow = {}
        for k, v in element.iteritems():
            if (k == "port"):
                if not isinstance(v, str):
                    raise ValueError('invalid port element in flow description')

                # xxx: validate the port

                new_flow[k] = v
            elif (k == "vlan_range"):
                if not isinstance(v, str):
                    raise ValueError('invalid vlan_range element in flow description')

                # xxx: validate the VLAN range

                new_flow[k] = v
            else:
                raise ValueError('Unknown element %s in flow description' % k)

        if not new_flow["port"] or not new_flow["vlan_range"]:
            raise ValueError('flow description is missing required elements')

        return new_flow

def convert_bandwidth_to_bps(bandwidth):
    m = re.search('^([0-9][0-9]*)([kmgKMG])([Bb])ps$', bandwidth)
    if (m):
        new_bandwidth = int(m.group(1))
        if (m.group(2) == "k" or m.group(2) == "K"):
            new_bandwidth *= 1000
        if (m.group(2) == "m" or m.group(2) == "M"):
            new_bandwidth *= 1000*1000
        if (m.group(2) == "g" or m.group(2) == "G"):
            new_bandwidth *= 1000*1000*1000
        if (m.group(3) == "B"):
            new_bandwidth *= 8
        return new_bandwidth

    m = re.search('^[0-9]+$', bandwidth)
    if (m):
        return bandwidth

    raise ValueError('Unknown bandwidth value %s' % bandwidth)
