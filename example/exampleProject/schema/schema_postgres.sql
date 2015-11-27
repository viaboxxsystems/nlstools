create table NLSBundle (
	id int8 not null,
	domain varchar(255),
	primary key (id)
);

create table NLSText (
	id int8 not null,
	key varchar(255),
	locale varchar(20),
	translated varchar(4000),
	bundleID int8 not null,
	primary key (id)
);