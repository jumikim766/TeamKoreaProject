CREATE DATABASE teamkorea;
CREATE USER 'teamkorea'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON teamkorea.* TO 'teamkorea'@'localhost';
FLUSH PRIVILEGES;