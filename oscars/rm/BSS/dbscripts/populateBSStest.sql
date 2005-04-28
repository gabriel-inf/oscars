-- this is esnet at the top level
insert networks values(1, 'ESnet');

-- this is juno1
insert into routers values(1, True, 'juno1', '198.128.1.52', 1);
insert into interfaces value(1, 1, 622080000, 'so-1', 'juno1->dev-rt20-e', 1);
insert into ipaddrs value(1, '10.10.0.1', 1);
insert into interfaces value(2, 1, 100000000, 'fe-0', 'juno1->dev-rt1', 1);
insert into ipaddrs value(2, '10.0.1.1', 2);
insert into interfaces value(3, 1, 1000000000, 'ge-1', 'juno1->testrig', 1);
insert into ipaddrs value(3, '192.168.0.1', 3);
insert into ipaddrs value(15, '198.128.1.52', 1);

-- this is dev-rt20-e
insert into routers values(2, True, 'dev-rt20-e', '198.128.1.138', 1);
insert into interfaces value(4, 1, 622080000, 'so-2', 'dev-rt20-e->juno', 2);
insert into ipaddrs value(4, '10.10.0.2', 4);
insert into interfaces value(5, 1, 1000000000, 'ge-2', 'dev-rt20-e->testrig', 2);
insert into ipaddrs value(5, '192.168.2.1', 5);
insert into interfaces value(6, 1, 100000000, 'fe-0', 'dev-rt20-e->dev-rt3', 2);
insert into ipaddrs value(6, '10.10.2.1', 6);
insert into interfaces value(7, 1, 100000000, 'fe-1', 'dev-rt20-e->dev-rt1', 2);
insert into ipaddrs value(7, '10.10.1.6', 7);
insert into interfaces value(8, 1, 2000000000, 'ae-0', 'dev-rt20-e->dev-rt1', 2);
insert into ipaddrs value(8, '10.10.1.2', 8);
insert into ipaddrs value(16, '198.128.1.138', 4);

-- this is dev-rt3
insert into routers values(3, True, 'dev-rt3', '198.128.1.164', 1);
insert into interfaces value(9, 1, 100000000, 'fe-0', 'dev-rt3->dev-rt20-e', 3);
insert into ipaddrs value(9, '10.10.2.2', 9);
insert into interfaces value(10, 1, 100000000, 'fe-0', 'dev-rt3->nocdev1', 3);
insert into ipaddrs value(10, '192.168.3.1', 10);
insert into ipaddrs value(17, '198.128.1.164', 10);

-- this is dev-rt1
insert into routers values(4, True, 'dev-rt1', '198.128.1.91', 1);
insert into interfaces value(11, 1, 100000000, 'fa6', 'dev-rt1->juno1', 4);
insert into ipaddrs value(11, '10.10.0.5', 11);
insert into interfaces value(12, 1, 100000000, 'fa5', 'dev-rt1->nocdev2', 4);
insert into ipaddrs value(12, '192.168.1.2', 12);
insert into interfaces value(13, 1, 100000000, 'fa5/1', 'dev-rt1->dev-rt20-e', 4);
insert into ipaddrs value(13, '10.10.1.5', 13);
insert into interfaces value(14, 1, 100000000, 'pc1', 'dev-rt1->dev-rt20-e', 4);
insert into ipaddrs value(14, '10.10.1.1', 14);
insert into ipaddrs value(18, '198.128.1.91', 14);

