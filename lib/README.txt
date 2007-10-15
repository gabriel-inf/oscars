Berkeley Lab is unable to redistribute some third-party jar's.  We are
looking into the possibility of having another site host these jar's in
one place.

1.  The Axis2 jar's.  You  need to get these from the Axis2 1.3 distribution:

    http://ws.apache.org/axis2/download/1_3/download.cgi

    Download the standard binary distribution.

2.  The Rampart (Java security) jar's.  Download only the Rampart module
    from

    http://ws.apache.org/axis2/modules/index.html

    This is temporary; we need to improve our build process.

3.  jta.jar:  You only need this for running the standalone tests.  The best
    way to get it is through the Hibernate distribution:

    http://www.hibernate.org/6.html  Download only Hibernate Core.

4.  servlet-api.jar:  If you have a running Tomcat server, you can get this
    from $CATALINA_HOME/lib

5.  mysql-connector-java-3.1.14-bin.jar:  You can download this from
    
    http://dev.mysql.com/downloads/connector/j/3.1.html


