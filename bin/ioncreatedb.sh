#!/bin/sh

SQL=$1
SQLROOT=$2
passwd=$3
echo "$SQL, $SQLROOT, $passwd"
$SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/ion-war/sql/createTables.sql;
$SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/ion-war/sql/adminSetup.sql;

