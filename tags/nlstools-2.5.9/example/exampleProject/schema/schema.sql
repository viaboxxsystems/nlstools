create table NLSBUNDLE (ID INTEGER NOT NULL, DOMAIN varchar(255))
create table NLSTEXT (KEY varchar(255), TRANSLATED varchar(4000), LOCALE varchar(20), BundleID INTEGER)