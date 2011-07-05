#!/bin/sh

# Script to stop OSCARS services.
# Call with list of servers to be stopped.
# ALL will start all the servers.Individual server args are:
#  authN authZ api coord topoBridge rm stubPSS lookup wbui
#  stubPCE bwPCE connPCE dijPCE vlanPCE nullAGG stubPSS
# Uses the pid files in $OSCARS_HOME/run to find processes to stop


printUsage() {
   echo "\nusage stopServers <server>"
   echo "<server> is either ALL or one or more of:"
   echo "\t authN authZ api coord topoBridge rm stubPSS  dragonPSS eomplsPSS PSS lookup wbui"
   #echo "\t stubPCE bwPCE connPCE dijPCE vlanPCE nullAGG stubPSS"
   echo "\t bwPCE connPCE dijPCE vlanPCE nullAGG notifyBridge wsnbroker ionui"
   exit 1
}

if [ $# -lt 1 ]; then
    printUsage
fi
if  [ -z $OSCARS_HOME ]; then
    echo "Please set the environment var OSCARS_HOME to the OSCARS deployment directory"
    exit -1
 fi
 DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
 
stopauthN() {
PID_FILE=$DEFAULT_PID_DIR/authN.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing authN
   `kill -9 $PID`
   rm $PID_FILE
else
    echo "AuthN is not running"
fi
}

stopauthZ() {
PID_FILE=$DEFAULT_PID_DIR/authZ.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing authZ
   `kill -9 $PID`
   rm $PID_FILE
else
    echo "AuthZ is not running"
fi
}

stopCoord () {
PID_FILE=$DEFAULT_PID_DIR/coord.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
    echo killing coordinator
    `kill -9 $PID`
    rm $PID_FILE
else
    echo "Coordinator is not running"
fi
}

stopRM () {
PID_FILE=$DEFAULT_PID_DIR/rm.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing resourceManager
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "Resource Manager is not running"
fi
}

stopTopoBridge() {
PID_FILE=$DEFAULT_PID_DIR/topoBridge.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing TopoBridge
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "TopoBridge is not running"
fi
}
stopStubPCE(){
PID_FILE=$DEFAULT_PID_DIR/stubPCE.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing stubPCE
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "StubPCE is not running"
fi
}

stopConnPCE() {
 PID_FILE=$DEFAULT_PID_DIR/connPCE.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing connectivity PCE
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "ConnPCE is not running"
fi
}

stopBWPCE() {
 PID_FILE=$DEFAULT_PID_DIR/bwPCE.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing bandwidth PCE
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "BWPCE is not running"
fi
}

stopDijPCE () {
PID_FILE=$DEFAULT_PID_DIR/dijPCE.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing dijkstarPCE
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "DijkstraPCE is not running"
fi
}

stopVlanPCE () {
PID_FILE=$DEFAULT_PID_DIR/vlanPCE.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing vlanPCE
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "VlanPCE is not running"
fi
}

#stopstubPSS () {
#PID_FILE=$DEFAULT_PID_DIR/stubPSS.pid
#if [ -f $PID_FILE ]
#then
 #  PID=`cat $PID_FILE`
 # echo killing stubPSS
 #  `kill -9 $PID`
 #   rm $PID_FILE
#else
 #   echo "StubPSS is not running"
#fi
#}


##############################################################################
# Subroutine to stop PSS. Checks for arguments and stops the respective PSS
# If none specified, then looks for PIDs from a known list of PSS and stops the
# PSS based on the PID found
# Note: Does not check for unix oid of process, only what OSCARS stores
###############################################################################
stopPSS () {
	#echo "PSS Type:$1"
	PSSOptions=( stub dragon eompls )
	if [ ! -z $1 ] ; then
		PSSType="$1PSS"
	else
		PSSType="*PSS"
	fi
	#echo "PSS Type = $PSSType"
	PID_FILE=$DEFAULT_PID_DIR/$PSSType.pid
	
	NO_PSS=0
	if [ -f $PID_FILE ]; then
		NO_PSS=1
	else
		for opt in  ${PSSOptions[@]}
		do
			PID_FILE="$DEFAULT_PID_DIR/"$opt"PSS.pid"
			#echo "PSS : $PSSFile $opt"
			if [ -f $PID_FILE ]; then
				echo "The PSS currently running is not the one you specified.."
				echo ""$opt"PSS is running. Now killing ..." 
				NO_PSS=1
				break;
			fi
		done
	fi
	#echo "PSS FOUND? $NO_PSS"
	if [ $NO_PSS -eq 0 ]; then
		echo "PSS is not running"
	else
		PID=`cat $PID_FILE`
   		echo killing ""$opt"PSS" #PSS
   		`kill -9 $PID`
    		rm $PID_FILE
	fi
}

stopnullPCE () {
PID_FILE=$DEFAULT_PID_DIR/nullpce.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing nullpce  [$PID]
   `kill -9 $PID`
    rm $PID_FILE
fi
}

stopnullAGG () {
PID_FILE=$DEFAULT_PID_DIR/nullagg.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing nulagg [$PID]
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "nullAGG is not running"
fi
}

stopOSCARSService() {
PID_FILE=$DEFAULT_PID_DIR/api.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing OSCARSService
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "OSCARSService api is not running"
fi
}

stopLookup() {
PID_FILE=$DEFAULT_PID_DIR/lookup.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing LookupService
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "Lookup is not running"
fi
}

stopNotificationBridge() {
PID_FILE=$DEFAULT_PID_DIR/notificationBridge.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing NotificationBridge
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "NotificationBridge is not running"
fi
}

stopWSNBroker() {
PID_FILE=$DEFAULT_PID_DIR/wsnbroker.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing WSNBroker
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "WSNBroker is not running"
fi
}

stopWBUI() {
PID_FILE=$DEFAULT_PID_DIR/wbui.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing WBUI
   `kill -9 $PID`
    rm $PID_FILE
else
    echo "WBUI is not running"
fi
}

stopIONUI() {
	PID_FILE=$DEFAULT_PID_DIR/ionui.pid
	if [ -f $PID_FILE ]
	then
   		PID=`cat $PID_FILE`
   		echo killing IONUI
   		`kill -9 $PID`
    		rm $PID_FILE
	else
    		echo "IONUI is not running"
	fi
}

while [ ! -z $1 ]
  do 
  case $1 in
  ALL)
    stopauthN
    stopauthZ
    stopOSCARSService
    stopCoord
    stopRM
    stopTopoBridge
#    stopStubPCE 
    stopConnPCE
    stopBWPCE
    stopDijPCE
    stopVlanPCE
#    stopstubPSS
    stopPSS #Stops whichever PID is present
#    stopnullPCE
    stopnullAGG
    stopLookup
    stopNotificationBridge
    stopWSNBroker
    stopWBUI
    stopIONUI;;
  authN)    stopauthN;;
  authZ)    stopauthZ;;
  api)      stopOSCARSService;;
  coord)    stopCoord;;
  rm)       stopRM;;
  topoBridge) stopTopoBridge;;
#  stubPCE)  stopStubPCE;;
  connPCE)  stopConnPCE;;
  bwPCE)    stopBWPCE;;
  dijPCE)   stopDijPCE;;
  vlanPCE)  stopVlanPCE;;
  nullPCE)  stopnullPCE;;
  nullAGG)  stopnullAGG;;
#  stubPSS)  stopstubPSS;;
#PSS option stops which ever PSS is running
  PSS)	stopPSS;;
  stubPSS)  stopPSS "stub";;
  dragonPSS) stopPSS "dragon";;
  eomplsPSS) stopPSS "eompls";;
  lookup)   stopLookup;;
  wbui)     stopWBUI;;
  notifyBridge)     stopNotificationBridge;;
  wsnbroker) stopWSNBroker;;
  ionui)    stopIONUI;;
  *)        echo server $1 not recognized;;
  esac
  shift
done
