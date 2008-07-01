-- upgrade aaa tables from 3.2 to 4.0

USE aaa;
ALTER IGNORE TABLE attributes ADD COLUMN description TEXT;

UPDATE  attributes SET description="make reservations" WHERE name="OSCARS-user" ;
UPDATE  attributes SET description="manage all reservations, view and update topology" WHERE name="OSCARS-engineer";
UPDATE  attributes SET description="view all reservations" WHERE name="OSCARS-operator";
UPDATE  attributes SET description="manage all reservations starting or ending at site" WHERE name="OSCARS-siteAdmin";
UPDATE  attributes SET description="make reservations and view topology" WHERE name="OSCARS-service";
UPDATE  attributes SET description="manage all users" WHERE name="OSCARS-administrator";