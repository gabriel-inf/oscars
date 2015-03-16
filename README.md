#OSCARS

Short for "On-demand Secure Circuits and Advance Reservation System," OSCARS is a freely available open-source product. As developed by the Department of Energy’s high-performance science network ESnet, OSCARS was designed by network engineers who specialize in supporting the U.S. national laboratory system and its data-intensive collaborations.

##Building OSCARS

###Preparing Your Environment

Make sure the following are installed on your system:

* [Java](https://www.java.com) 1.7
* The latest version of [MySQL](http://www.mysql.com)
* The latest version of [Maven](http://maven.apache.org) 

You will also need to set the following environment variables:
* OSCARS_DIST  - directory where sources are kept
* OSCARS_HOME  - directory where the oscars runtime configurations will be

Finally, you will need to copy and the template files below to the same location but without the .template extension (e.g. authN/config/authN.yaml.template -> authN/config/authN.yaml) 
* authN/config/authN.yaml.template
* authZ/config/authZ.yaml.template
* coordinator/config/pce-configuration-template.xml
* resourceManager/config/resourceManager.yaml.template
* topoBridge/config/topoBridge.yaml.template
* wbui/config/wbui.yaml.template


###Building using maven

Run the following commands:

```bash
cd $OSCARS_DIST
mvn -DskipTests install
```

## Testing OSCARS
Currently many of the modules contain unit tests. Some of the tests require other components to be running and may fail if they are not present. You can run the unit tests alone with the command:

```bash
mvn test
```

You may also install only if the tests pass by running:

```bash
mvn install
```

##Running OSCARS

###Starting OSCARS

You may start all OSCARS service with the following command:

```bash
$OSCARS_DIST/bin/startServers.sh PRODCUCTION ALL
```

*Note: You may start individual services by replacing the second argument with the module name*
        
###Stopping OSCARS

```bash
$OSCARS_DIST/bin/stopServers.sh ALL
```

