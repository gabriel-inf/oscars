#!/bin/bash

OSCARS_HOME=/usr/local/oscars
OSCARS_DIST=/usr/local/oscars-0.6

dirs=( AuthNPolicyService AuthNService AuthZPolicyService AuthZService BandwidthPCE ConnectivityPCE CoordService DijkstraPCE LookupService NotificationBridgeService OSCARSInternalService OSCARSService PCEService PSSService ResourceManagerService TopoBridgeService Utils VlanPCE WBUIService WSNBrokerService )

for dir in ${dirs[@]}
do
	cd $OSCARS_HOME/$dir/conf
	rm -rf *
done

$OSCARS_DIST/bin/exportconfig

