DROP KEYSPACE IF EXISTS audit;
create keyspace audit with replication = {'class':'SimpleStrategy', 'replication_factor' : 1};

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

CREATE TABLE user_event
  (
    user_id uuid,
    organisation_id uuid,
    module text,
    submodule text,
    action text,
    timestamp timestamp,
    data text,
    PRIMARY KEY (user_id, module, submodule, action, timestamp)
  );

CREATE MATERIALIZED VIEW user_event_by_module_user_timestamp AS
SELECT user_id, organisation_id, module, submodule, action, timestamp, data
FROM audit.user_event
WHERE user_id IS NOT NULL
AND module IS NOT NULL
AND submodule IS NOT NULL
AND action IS NOT NULL
AND timestamp IS NOT NULL
PRIMARY KEY (module, user_id, timestamp, submodule, action)
with CLUSTERING order BY (timestamp desc);

CREATE MATERIALIZED VIEW user_event_by_module_user_organisation_timestamp AS
SELECT user_id, organisation_id, module, submodule, action, timestamp, data
FROM audit.user_event
WHERE user_id IS NOT NULL
AND organisation_id IS NOT NULL
AND module IS NOT NULL
AND submodule IS NOT NULL
AND action IS NOT NULL
AND timestamp IS NOT NULL
PRIMARY KEY (module, user_id, organisation_id, timestamp, submodule, action)
with CLUSTERING order BY (timestamp desc);