# MySQL table definition syntax

database name: athena

mysql> show tables;
+---------------------+
| Tables_in_athena    |
+---------------------+
| active_reservations |
| cookiekey           |
| past_reservations   |
| reservations        |
| users               |
+---------------------+
5 rows in set (0.00 sec)

/*
TABLE: reservations
reservation_id INT UNSIGNED NOT NULL AUTO_INCREMENT
user_loginname VARCHAR(25) NOT NULL
reserv_origin_ip VARCHAR(40) NOT NULL
reserv_dest_ip VARCHAR(40) NOT NULL
reserv_bandwidth INT UNSIGNED NOT NULL
reserv_start_time DATETIME NOT NULL
reserv_end_time DATETIME NOT NULL
access_ip VARCHAR(40)
access_domain VARCHAR(200)
access_browser VARCHAR(200)
PRIMARY KEY (reservation_id)
*/

/*
IPv4 IP addresses are 15 characters long.
An IPv6 IP address is 128 bits long; in a hexadecimal format with a colon as separator after each block of 16 bits, it's 39 characters long.
3ffe:ffff:0100:f101:0210:a4ff:fee3:9566
The program cannot deal with IPv6 yet, but just in case...
*/

CREATE TABLE reservations (
	reservation_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	user_loginname VARCHAR(25) NOT NULL,
	reserv_origin_ip VARCHAR(40) NOT NULL,
	reserv_dest_ip VARCHAR(40) NOT NULL,
	reserv_bandwidth INT UNSIGNED NOT NULL,
	reserv_start_time DATETIME NOT NULL,
	reserv_end_time DATETIME NOT NULL,
	reserv_description TEXT NOT NULL,
	reserv_made_time DATETIME NOT NULL,
	access_ip VARCHAR(40),
	access_domain VARCHAR(200),
	access_browser VARCHAR(200),
	PRIMARY KEY (reservation_id)
);

mysql> DESCRIBE reservations;
+--------------------+------------------+------+-----+---------------------+----------------+
| Field              | Type             | Null | Key | Default             | Extra          |
+--------------------+------------------+------+-----+---------------------+----------------+
| reservation_id     | int(10) unsigned |      | PRI | NULL                | auto_increment |
| user_loginname     | varchar(25)      |      |     |                     |                |
| reserv_origin_ip   | varchar(40)      |      |     |                     |                |
| reserv_dest_ip     | varchar(40)      |      |     |                     |                |
| reserv_bandwidth   | int(10) unsigned |      |     | 0                   |                |
| reserv_start_time  | datetime         |      |     | 0000-00-00 00:00:00 |                |
| reserv_end_time    | datetime         |      |     | 0000-00-00 00:00:00 |                |
| reserv_description | text             |      |     |                     |                |
| reserv_made_time   | datetime         |      |     | 0000-00-00 00:00:00 |                |
| access_ip          | varchar(40)      | YES  |     | NULL                |                |
| access_domain      | varchar(200)     | YES  |     | NULL                |                |
| access_browser     | varchar(200)     | YES  |     | NULL                |                |
+--------------------+------------------+------+-----+---------------------+----------------+
12 rows in set (0.00 sec)

# 'past_reservations' table is the repository of fulfilled reservations, 
# and thus has the same structure as the 'reservations' table
CREATE TABLE past_reservations (
	reservation_id INT UNSIGNED NOT NULL,
	user_loginname VARCHAR(25) NOT NULL,
	reserv_origin_ip VARCHAR(40) NOT NULL,
	reserv_dest_ip VARCHAR(40) NOT NULL,
	reserv_bandwidth INT UNSIGNED NOT NULL,
	reserv_start_time DATETIME NOT NULL,
	reserv_end_time DATETIME NOT NULL,
	reserv_description TEXT NOT NULL,
	reserv_made_time DATETIME NOT NULL,
	access_ip VARCHAR(40),
	access_domain VARCHAR(200),
	access_browser VARCHAR(200),
	PRIMARY KEY (reservation_id)
);

mysql> DESCRIBE past_reservations;
+--------------------+------------------+------+-----+---------------------+-------+
| Field              | Type             | Null | Key | Default             | Extra |
+--------------------+------------------+------+-----+---------------------+-------+
| reservation_id     | int(10) unsigned |      | PRI | 0                   |       |
| user_loginname     | varchar(25)      |      |     |                     |       |
| reserv_origin_ip   | varchar(40)      |      |     |                     |       |
| reserv_dest_ip     | varchar(40)      |      |     |                     |       |
| reserv_bandwidth   | int(10) unsigned |      |     | 0                   |       |
| reserv_start_time  | datetime         |      |     | 0000-00-00 00:00:00 |       |
| reserv_end_time    | datetime         |      |     | 0000-00-00 00:00:00 |       |
| reserv_description | text             |      |     |                     |       |
| reserv_made_time   | datetime         |      |     | 0000-00-00 00:00:00 |       |
| access_ip          | varchar(40)      | YES  |     | NULL                |       |
| access_domain      | varchar(200)     | YES  |     | NULL                |       |
| access_browser     | varchar(200)     | YES  |     | NULL                |       |
+--------------------+------------------+------+-----+---------------------+-------+
12 rows in set (0.00 sec)

# 'active_reservations' table stores the list of active reservations/tunnels
# this is a related table to 'reservations', so it doesn't store everything in that table
# 'active_tunnel_ingress' and 'active_tunnel_egress' in Abilene are router nicknames (e.g. 'atla')
CREATE TABLE active_reservations (
	reservation_id INT UNSIGNED NOT NULL,
	user_loginname VARCHAR(25) NOT NULL,
	active_tunnel_name VARCHAR(200) NOT NULL,
	active_tunnel_ingress VARCHAR(40) NOT NULL,
	active_tunnel_egress VARCHAR(40) NOT NULL,
	active_tunnel_createdtime DATETIME NOT NULL,
	reserv_end_time DATETIME NOT NULL,
	PRIMARY KEY (reservation_id)
);

mysql> DESCRIBE active_reservations;
+---------------------------+------------------+------+-----+---------------------+-------+
| Field                     | Type             | Null | Key | Default             | Extra |
+---------------------------+------------------+------+-----+---------------------+-------+
| reservation_id            | int(10) unsigned |      | PRI | 0                   |       |
| user_loginname            | varchar(25)      |      |     |                     |       |
| active_tunnel_ingress     | varchar(40)      |      |     |                     |       |
| active_tunnel_egress      | varchar(40)      |      |     |                     |       |
| active_tunnel_createdtime | datetime         |      |     | 0000-00-00 00:00:00 |       |
| reserv_end_time           | datetime         |      |     | 0000-00-00 00:00:00 |       |
+---------------------------+------------------+------+-----+---------------------+-------+
6 rows in set (0.00 sec)

/*
TABLE: users
user_register_id INT UNSIGNED NOT NULL AUTO_INCREMENT
user_loginname VARCHAR(25) NOT NULL
user_password VARCHAR(64) NOT NULL
user_firstname VARCHAR(50) NOT NULL
user_lastname VARCHAR(50) NOT NULL
user_organization VARCHAR(100) NOT NULL
user_email_primary VARCHAR(100) NOT NULL
user_email_secondary VARCHAR(100)
user_phone_primary VARCHAR(100) NOT NULL
user_phone_secondary VARCHAR(100)
user_description TEXT
user_level TINYINT UNSIGNED NOT NULL
user_register_datetime DATETIME NOT NULL
user_activation_key VARCHAR(40),
user_pending_level TINYINT UNSIGNED,
PRIMARY KEY (user_register_id)
UNIQUE (user_loginname)
*/

CREATE TABLE users (
	user_register_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	user_loginname VARCHAR(25) NOT NULL,
	user_password VARCHAR(64) NOT NULL,
	user_firstname VARCHAR(50) NOT NULL,
	user_lastname VARCHAR(50) NOT NULL,
	user_organization VARCHAR(100) NOT NULL,
	user_email_primary VARCHAR(100) NOT NULL,
	user_email_secondary VARCHAR(100),
	user_phone_primary VARCHAR(100) NOT NULL,
	user_phone_secondary VARCHAR(100),
	user_description TEXT,
	user_level TINYINT UNSIGNED NOT NULL,
	user_register_datetime DATETIME NOT NULL,
	user_activation_key VARCHAR(40),
	user_pending_level TINYINT UNSIGNED,
	PRIMARY KEY (user_register_id),
	UNIQUE (user_loginname)
);

mysql> DESCRIBE users;
+------------------------+---------------------+------+-----+---------------------+----------------+
| Field                  | Type                | Null | Key | Default             | Extra          |
+------------------------+---------------------+------+-----+---------------------+----------------+
| user_register_id       | int(10) unsigned    |      | PRI | NULL                | auto_increment |
| user_loginname         | varchar(25)         |      | UNI |                     |                |
| user_password          | varchar(64)         |      |     |                     |                |
| user_firstname         | varchar(50)         |      |     |                     |                |
| user_lastname          | varchar(50)         |      |     |                     |                |
| user_organization      | varchar(100)        |      |     |                     |                |
| user_email_primary     | varchar(100)        |      |     |                     |                |
| user_email_secondary   | varchar(100)        | YES  |     | NULL                |                |
| user_phone_primary     | varchar(100)        |      |     |                     |                |
| user_phone_secondary   | varchar(100)        | YES  |     | NULL                |                |
| user_description       | text                | YES  |     | NULL                |                |
| user_level             | tinyint(3) unsigned |      |     | 0                   |                |
| user_register_datetime | datetime            |      |     | 0000-00-00 00:00:00 |                |
| user_activation_key    | varchar()         | YES  |     | NULL                |                |
| user_pending_level     | tinyint(3) unsigned | YES  |     | NULL                |                |
+------------------------+---------------------+------+-----+---------------------+----------------+
15 rows in set (0.00 sec)

/*
TABLE: cookiekey
cookiekey_id INT UNSIGNED NOT NULL AUTO_INCREMENT
user_loginname VARCHAR(25) NOT NULL
randomkey VARCHAR(32) NOT NULL
PRIMARY KEY (cookiekey_id)
UNIQUE (user_loginname)
*/

CREATE TABLE cookiekey (
	cookiekey_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	user_loginname VARCHAR(25) NOT NULL,
	randomkey VARCHAR(32) NOT NULL,
	PRIMARY KEY (cookiekey_id),
	UNIQUE (user_loginname)
);

mysql> DESCRIBE cookiekey;
+----------------+------------------+------+-----+---------+----------------+
| Field          | Type             | Null | Key | Default | Extra          |
+----------------+------------------+------+-----+---------+----------------+
| cookiekey_id   | int(10) unsigned |      | PRI | NULL    | auto_increment |
| user_loginname | varchar(25)      |      | UNI |         |                |
| randomkey      | varchar(32)      |      |     |         |                |
+----------------+------------------+------+-----+---------+----------------+
3 rows in set (0.00 sec)

