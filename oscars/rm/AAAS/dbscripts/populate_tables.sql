delete from institutions;
insert into institutions values('NULL', 'ESnet');
insert into institutions values('NULL', 'NERSC');
insert into institutions values('NULL', 'Berkeley National Laboratory');
insert into institutions values('NULL', 'Brookhaven National Laboratory');
insert into institutions values('NULL', 'FermiLab');
insert into institutions values('NULL', 'General Atomics');
insert into institutions values('NULL', 'Internet2');

delete from authorizations;
insert into authorizations values('NULL', 'atwioscars', 1);

delete from auth_types;
insert into auth_types values('NULL', 'db password');

delete from users;
insert into users values('NULL', 'Guok', 'Chin', 'chin', ENCRYPT('esosc@rs77'), 'chin@es.net', NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, 1, 1);
insert into users values('NULL', 'Robertson', 'David', 'davidr', ENCRYPT('shyysh'), 'dwrobertson@lbl.gov', NULL, NULL, NULL, NULL, 1, NULL, NULL,NULL, 1, 3);
insert into users values('NULL', 'Lee', 'Jason', 'jason', ENCRYPT('ritazza6'),'jrlee@lbl.gov', NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, 1, 3);
