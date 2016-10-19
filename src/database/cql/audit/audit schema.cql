DROP KEYSPACE IF EXISTS audit;
create keyspace audit with replication = {'class':'SimpleStrategy', 'replication_factor' : 1};

USE audit;

DROP MATERIALIZED VIEW IF EXISTS user_event_by_module_user_timestamp;
DROP MATERIALIZED VIEW IF EXISTS user_event_by_module_user_organisation_timestamp;

DROP TABLE IF EXISTS user_event;
DROP TABLE IF EXISTS exchange_transform_error_state;
DROP TABLE IF EXISTS exchange_transform_error_to_re_process;
DROP TABLE IF EXISTS exchange_transform_audit;
DROP TABLE IF EXISTS exchange_event;
DROP TABLE IF EXISTS exchange;

//no longer used - but tidy up old tables
DROP MATERIALIZED VIEW IF EXISTS exchange_transform_by_service_and_system; 
DROP TABLE IF EXISTS exchange_transform; 
DROP TABLE IF EXISTS exchangeEvent; 


CREATE TABLE exchange 
( 
    exchange_id UUID,
    timestamp TIMESTAMP,
    headers VARCHAR,
    body VARCHAR,
    PRIMARY KEY (exchange_id) 
);

CREATE TABLE exchange_event 
( 
	exchange_id UUID,
    timestamp TIMESTAMP,
    event INT,
    PRIMARY KEY (exchange_id, timestamp)
);

CREATE TABLE exchange_transform_audit 
( 
	service_id UUID,
    system_id UUID,
    exchange_id UUID,
    version TIMEUUID,
    started TIMESTAMP,
    ended TIMESTAMP,
    error_xml TEXT,
    PRIMARY KEY ((service_id, system_id), exchange_id, version)
) WITH CLUSTERING ORDER BY (exchange_id ASC, version DESC);

CREATE TABLE exchange_transform_error_state 
( 
	service_id UUID,
    system_id UUID,
    exchange_ids_in_error LIST<UUID>,
    PRIMARY KEY (service_id, system_id)
);

CREATE TABLE exchange_transform_error_to_re_process
( 
	service_id UUID,
    system_id UUID,
    exchange_id UUID,
    PRIMARY KEY ((service_id, system_id), exchange_id)
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