DROP KEYSPACE IF EXISTS audit;
create keyspace audit with replication = {'class':'SimpleStrategy', 'replication_factor':1};

USE audit;


CREATE TABLE exchange 
  ( 
    timestamp TIMESTAMP,
    exchangeId UUID,
    headers VARCHAR,
    body VARCHAR,
    PRIMARY KEY (exchangeId) 
  );

CREATE TABLE exchangeEvent 
  ( 
    timestamp TIMESTAMP,
    exchangeId UUID,
    event INT,
    PRIMARY KEY (exchangeId, timestamp)
  );
