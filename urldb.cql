DROP KEYSPACE IF EXISTS urldb;

CREATE KEYSPACE urldb WITH replication = {'class':'SimpleStrategy','replication_factor':1};

USE urldb;

CREATE TABLE urls (
  short_url TEXT,
  long_url TEXT,
  PRIMARY KEY (short_url)
);


CREATE TABLE urls_by_date (
  short_url TEXT,
  date TEXT,
  event_time TIMESTAMP,
  stats MAP<TEXT, TEXT>,
  PRIMARY KEY ((short_url,date), event_time)
);
