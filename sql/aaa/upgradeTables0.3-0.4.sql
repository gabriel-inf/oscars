-- upgrade aaa tables from 3.2 to 4.0

USE aaa;

-- Add items to allow modification of attributes, authorizations and institutions
CREATE UNIQUE INDEX resourceName ON resources(name(6));
INSERT INTO resources VALUES(NULL, "AAA",
                        "Information about Institutions, Attributes and Authorizations",
                        NULL);
                        
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="list"),
     NULL,NULL); 
        
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="modify"),
     NULL,NULL);
     
-- Fill in description for attributes
ALTER IGNORE TABLE attributes ADD COLUMN description TEXT NOT NULL;

UPDATE  attributes SET description="make reservations" WHERE name="OSCARS-user" ;
UPDATE  attributes SET description="manage all reservations, view and update topology" WHERE name="OSCARS-engineer";
UPDATE  attributes SET description="view all reservations" WHERE name="OSCARS-operator";
UPDATE  attributes SET description="manage all reservations starting or ending at site" WHERE name="OSCARS-siteAdmin";
UPDATE  attributes SET description="make reservations and view topology" WHERE name="OSCARS-service";
UPDATE  attributes SET description="manage all users" WHERE name="OSCARS-administrator";

