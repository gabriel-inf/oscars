#!/bin/bash
java -Xmx256m -Djava.net.preferIPv4Stack=true -Done-jar.main.class=net.es.oscars.client.improved.list.ListInvoker -jar target/improvedJavaClients-0.0.1-SNAPSHOT.one-jar.jar  $*