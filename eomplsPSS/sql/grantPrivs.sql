CREATE DATABASE IF NOT EXISTS eomplspss;
CREATE DATABASE IF NOT EXISTS testeomplspss;
GRANT select, insert, update, delete ON eomplspss.* TO 'oscars'@'localhost' IDENTIFIED BY 'mypass';
GRANT select, insert, update, delete, create, drop, alter on `testeomplspss`.* TO 'oscars'@'localhost' IDENTIFIED BY 'mypass';
