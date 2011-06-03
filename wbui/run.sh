 #!/bin/bash
#-Djavax.net.debug=ssl:keymanager
java -Xmx256m -Djetty.logs=$OSCARS_HOME/logs -Djava.net.preferIPv4Stack=true -jar target/wbui-0.0.1-SNAPSHOT.one-jar.jar $*
