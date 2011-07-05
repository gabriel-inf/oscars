#!/bin/sh  
# Script to start OSCARS services.
# Call with a context and a list of servers to start.
# ALL will start all the servers.Individual server args are:
#  authN authZ api coord topoBridge rm stubPSS lookup wbui
# stubPCE bwPCE connPCE dijPCE vlanPCE nullAGG stubPSS
# saves server pids in $OSCARS_HOME/run for stopServers to use
# server output is directed to files in the current directory

DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d $DEFAULT_PID_DIR ]
then
   mkdir $DEFAULT_PID_DIR
fi

if  [ -z $OSCARS_DIST ]; then
    echo "Please set the environment var OSCARS_DIST to the OSCARS source directory"
    exit -1
 fi
 
printUsage() {
   echo
   echo "usage startServers [-v] <context> <server >"
   echo " -v  prints out debugging messages. Must be first arg"
   echo "<context> is one of: PRODUCTION|pro UNITTEST|test DEVELOPMENT|dev SDK|sdk"
   echo "<server> is either ALL or one or more of:"
   echo "     authN authZ api coord topoBridge rm stubPSS eomplsPSS dragonPSS PSS "
   echo "     lookup wbui bwPCE connPCE dijPCE vlanPCE nullAGG notifyBridge wsnbroker"
   exit 1
}
startService() {
   Config=$(sh $OSCARS_DIST/bin/parseManifest.sh $Service $CONTEXT $Directory | sed "s/'//g")
   if [ $debug ]; then
       echo "Starting $Service"
   fi
   Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
   Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
   if [ "$Conf" == "conf" ]; then
       if [ $debug ]; then
           echo "using configuration file $OSCARS_HOME/$Service/$Conf/$Yaml"
       fi
       port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print}' $OSCARS_HOME/$Service/$Conf/$Yaml)
   elif [ "$Conf" == "config" ]; then
       if [ $debug ]; then
           echo "using configuration file  $OSCARS_DIST/$Service/$Conf/$Yaml"
       fi
       port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print}' $OSCARS_DIST/$Service/$Conf/$Yaml)
   fi
    if [ $debug ]; then
       echo "port definition line is  $port"
   fi
   port=$(echo $port | sed "s/[^0-9]//g")
   if [ $debug ]; then
       echo "port is $port"
   fi
   if [ $debug ]; then
       line=`netstat -na | grep tcp | grep LISTEN | grep $port`
       echo "checking $line"
   fi
   porttest=`netstat -na | grep tcp | grep LISTEN | grep "[:|\.]$port "`
   if [ ! -z "$porttest" ]; then
       echo $Service is  already running
   else
       echo starting $ShortName on port $port
       if [ $ShortName != "nullagg" ]; then
           (cd $OSCARS_DIST/$Directory; bin/startServer.sh $CONTEXT > $currDir/$ShortName.out 2>&1  & )
       else
           (cd $OSCARS_DIST/$Directory; bin/startNullAgg.sh $CONTEXT > $currDir/$ShortName.out 2>&1 &)
       fi
   fi
}
startauthN() {
   Service="AuthNService"
   ShortName="authN"
   Directory="authN"
   startService
}

startauthZ(){
   Service="AuthZService"
   ShortName="authZ"
   Directory="authZ"
   startService
}

startOSCARSService() {
    Service="OSCARSService"
    ShortName="api"
    Directory="api"
    startService
}

startCoord() {
    Service="CoordService"
    ShortName="coord"
    Directory="coordinator"
    startService
}

startRM(){
    Service="ResourceManagerService"
    ShortName="rm"
    Directory="resourceManager"
    startService
}

startTopoBridge() {
    Service="TopoBridgeService"
    ShortName="topoBridge"
    Directory="topoBridge"
    startService
}

startNotificationBridge() {
    Service="NotificationBridgeService"
    ShortName="notificationBridge"
    Directory="notificationBridge"
    startService
}

startWSNBroker() {
    Service="WSNBrokerService"
    ShortName="wsnbroker"
    Directory="wsnbroker"
    startService
}

startStubPCE(){
    Service="StubPCE"
    ShortName="stubPCE"
    Directory="stubPCE"
    startService
}

startConnPCE() {
    Service="ConnectivityPCE"
    ShortName="connPCE"
    Directory="connectivityPCE"
    startService
}

startBandwidthPCE() {
    Service="BandwidthPCE"
    ShortName="bwPCE"
    Directory="bandwidthPCE"
    startService
}

startDijPCE () {
    Service="DijkstraPCE"
    ShortName="dijPCE"
    Directory="dijkstraPCE"
    startService
}

startVlanPCE () {
    Service="VlanPCE"
    ShortName="vlanPCE"
    Directory="vlanPCE"
    startService
}

startnullPCE () {
    Service="nullPCE"
    ShortName="nullpce"
    Directory="pce"
    startService

}

startnullAGG () {
    Service="PCEService"
    ShortName="nullagg"
    Directory="pce"
    startService
}

##########Subroutine to decide which PSS to start
startPSS() {
    DRAGONPSS="DRAGON"
    EOMPLSPSS="EOMPLS"
    #Get PSS choice, but keep stubPSS the default
    whichPSS="STUB"
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh Utils $CONTEXT utils)
    Service=$(echo $Config | awk -F/ '$1~//{print $2}')
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    if [ "$Conf" == "conf" ]; then
        whichPSS=$(awk -F: '$1~/PSSChoice/{print $2}' $OSCARS_HOME/$Service/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
        whichPSS=$(awk -F: '$1~/PSSChoice/{print $2}' $OSCARS_DIST/$Service/$Conf/$Yaml)
    fi
    whichPSS=$(echo $whichPSS | sed 's/^ *\(.*\) *$/\1/')
    if [ $debug ]; then
        echo "Starting PSS :$whichPSS"
    fi
    #Now start based on choice obtained
    if [ "$whichPSS" == "$DRAGONPSS" ]; then
        startDragonPSS
    elif [ "$whichPSS" == "$EOMPLSPSS" ]; then
        startEomplsPSS
    else
        startStubPSS
    fi
}

startStubPSS(){
    Service="PSSService"
    ShortName="stubPSS"
    Directory="stubPSS"
    startService
}

startDragonPSS(){
    Service="PSSService"
    ShortName="dragonPSS"
    Directory="dragonPSS"
    startService
}

startEomplsPSS(){
    Service="PSSService"
    ShortName="eomplsPSS"
    Directory="eomplsPSS"
    startService
}

startLookup(){
    Service="LookupService"
    ShortName="lookup"
    Directory="lookup"
    startService
}

startWBUI(){
# gets its port from the jetty.xml file
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh WBUIService $CONTEXT wbui jetty.xml)
    Service=$(echo $Config | awk -F/ '$1~//{print $2}')
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    if [ "$Conf" == "conf" ]; then
        if [ $debug ]; then
            echo "configuration file is $OSCARS_HOME/$Service/$Conf/$Yaml"
        fi
        port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_HOME/$Service/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
        if [ debug ]; then
            echo "configuration file is $OSCARS_DIST/$Service/$Conf/$Yaml"
        fi
        port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_DIST/$Service/$Conf/$Yaml)
    fi
    port=$(echo $port | sed "s/[^0-9]//g")
    porttest=`netstat -na | grep tcp | grep LISTEN | grep $port`
    if [ ! -z "$porttest" ]; then
        echo WBUI  already running
    else
       echo starting WBUI Server on port $port
       (cd $OSCARS_DIST/wbui; bin/startServer.sh $CONTEXT > $currDir/wbui.out 2>&1 &)
    fi
}

# execution starts here
if [ $# -lt 2 ]; then
    printUsage
fi
currDir=$(pwd)
if [ $1 == "-v" ]; then
    debug=1
    shift
fi
CONTEXT=$1
case $1 in
    d|D|dev|DEV) CONTEXT="DEVELOPMENT";;
    p|P|pro|PRO) CONTEXT="PRODUCTION";;
    t|T|test|TEST) CONTEXT="UNITTEST";;
    s|S|sdk) CONTEXT="SDK";;
esac
   
if [ "$CONTEXT" ==  "PRODUCTION" ] || [ "$CONTEXT" == "UNITTEST" ] || [ "$CONTEXT" == "DEVELOPMENT" ] || [ "$CONTEXT" == "SDK" ]; then
    echo "Start services in $CONTEXT context"
else
    echo "context  $CONTEXT is not recognized"
    printUsage
fi
shift
while [ ! -z $1 ]
    do 
    case $1 in
    ALL)
      startLookup
      startTopoBridge
      startRM
      startauthN
      startauthZ
      startCoord
#     startStubPCE
#     startnullPCE
      startnullAGG
      startDijPCE
      startConnPCE
      startBandwidthPCE
      startVlanPCE
      startPSS
      startNotificationBridge
      startWSNBroker
      startWBUI
      startOSCARSService;;  
    authN)    startauthN;;
    authZ)    startauthZ;;
    api)      startOSCARSService;;
    coord)    startCoord;;
    topoBridge) startTopoBridge;;
    rm)       startRM;;
#   stubPCE)  startStubPCE;;
    bwPCE)    startBandwidthPCE;;
    connPCE)  startConnPCE;;
    dijPCE)   startDijPCE;;
    vlanPCE)  startVlanPCE;;
    nullPCE)  startnullPCE;;
    nullAGG)  startnullAGG;;
    PSS)      startPSS;;
    stubPSS) startPSS;; #TBD- remove generic used for testing startStubPSS;;
    dragonPSS)startDragonPSS;;
    eomplsPSS)startEomplsPSS;;
    lookup)   startLookup;;
    wbui)     startWBUI;;
    notifyBridge)     startNotificationBridge;;
    wsnbroker) startWSNBroker;;
    ionui) startIONUI;;
    *)        echo $1 not a recognized server
  esac
  shift
done

