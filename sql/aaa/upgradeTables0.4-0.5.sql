-- upgrade aaa tables from  0.4.0 0.5

USE aaa;

-- This one got left out in 0.4 releases before 1/3/09 
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="create"),
    (select id from constraints where name="all-users"));
  
  -- Table to look up an institution associated with a domain (for site admin
-- privileges)

CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- topologyId for a domain -- matches topologyIdent in bss domains table
    domainTopologyId                TEXT NOT NULL,
        -- key of corresponding domain in domains table
    institution            INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX row ON sites(domainTopologyId(7),institution);
      
   -- Doesn't make sense for a user not to be able to see and modify own profile
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-site-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-site-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
     
     
    -- Give OSCARS-admin permission to create all users
DROP PROCEDURE IF EXISTS updateAdminAuth;
DELIMITER //
CREATE PROCEDURE updateAdminAuth()
BEGIN
    DECLARE authId INT;
    SELECT id INTO authId FROM authorizations WHERE 
        attrId=(SELECT id FROM attributes WHERE name="OSCARS-administrator") AND
        resourceId=(SELECT id FROM resources WHERE name="Users") AND
        permissionId=(SELECT id FROM permissions WHERE name="create");
    
    IF authId IS NOT NULL THEN
        UPDATE authorizations SET constraintId=(SELECT id FROM constraints WHERE name="all-users"), 
            constraintValue="true" WHERE id=authId;
    END IF;
END
//
DELIMITER ;
CALL updateAdminAuth;

    -- move sites from bss to aaa
DROP PROCEDURE IF EXISTS moveSites;
DELIMITER //
CREATE PROCEDURE moveSites()
BEGIN
    DECLARE dName TEXT;
    DECLARE iName TEXT;
    DECLARE iId INT;
    DECLARE finished INT DEFAULT 0;
    DECLARE siteCur CURSOR FOR SELECT bss.domains.topologyIdent, bss.sites.name 
        FROM bss.sites INNER JOIN bss.domains WHERE bss.domains.id=bss.sites.domainId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;
    
    OPEN siteCur;
    moveSite: LOOP
        FETCH siteCur INTO dName, iName;
	    IF finished THEN
	        LEAVE moveSite;
        END IF;
        SELECT id INTO iId FROM aaa.institutions WHERE name=iName;
        INSERT INTO aaa.sites VALUES(NULL, dName, iId);
    END LOOP moveSite;
    
END
//
DELIMITER ;
CALL moveSites;