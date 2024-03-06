create user dbuser;
grant all privileges on *.* to dbuser@'%' identified by 'dbpass';
grant file on *.* to dbuser@'%';
