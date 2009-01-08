-- upgrade aaa tables from  0.4.0 0.5

USE aaa;

-- This one got left out in 0.4 releases before 1/3/09 
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="create"),
    (select id from constraints where name="all-users"));
    