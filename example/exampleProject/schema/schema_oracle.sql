CREATE TABLE NLSBUNDLE (
  ID     INTEGER NOT NULL,
  DOMAIN VARCHAR(255)
);
CREATE TABLE NLSTEXT (
  ID         INTEGER NOT NULL,
  KEY        VARCHAR(255),
  TRANSLATED VARCHAR(4000),
  LOCALE     VARCHAR(20),
  BundleID   INTEGER
);