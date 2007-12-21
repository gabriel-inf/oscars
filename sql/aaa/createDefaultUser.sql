-- Create a default user account with username oscars-admin and password oscars

INSERT INTO users VALUES(NULL, 'oscars-admin', NULL, NULL, 'OSCARS', 'ADMIN', 
    'oscars-admin@nowhere.net', '5555555555','osSyzhoUttaAI', NULL, NULL, NULL, 
     NULL, NULL, NULL, NULL, 3);

INSERT INTO userAttributes VALUES(NULL,
	(select id from users where login = "oscars-admin"), 
        (select id from attributes where name="OSCARS-engineer"));
        
INSERT INTO userAttributes VALUES(NULL,
	(select id from users where login = "oscars-admin"), 
        (select id from attributes where name="OSCARS-administrator"));