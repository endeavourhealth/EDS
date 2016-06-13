// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS ehr;
CREATE KEYSPACE ehr WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE ehr;

DROP MATERIALIZED VIEW IF EXISTS person_resource_event_store_by_date;
DROP TABLE IF EXISTS person_resource_event_store;

DROP MATERIALIZED VIEW IF EXISTS person_resource_metadata_by_date;
DROP TABLE IF EXISTS person_resource;

CREATE TABLE person_resource (  person_id uuid,  resource_type text,  service_id uuid,  system_instance_id uuid,  resource_id text,
  effective_date timestamp,
  version text,
  last_updated timestamp,  resource_metadata text,  schema_version text,  resource_data text,  PRIMARY KEY ((person_id), resource_type, service_id, system_instance_id, resource_id));

CREATE MATERIALIZED VIEW person_resource_metadata_by_date AS 
SELECT person_id, resource_type, effective_date, service_id, system_instance_id, resource_id, resource_metadata
FROM person_resource
WHERE person_id IS NOT NULL
  AND resource_type IS NOT NULL
  AND effective_date IS NOT NULL
  AND service_id IS NOT NULL
  AND system_instance_id IS NOT NULL
  AND resource_id IS NOT NULL
PRIMARY KEY((person_id), resource_type, effective_date, service_id, system_instance_id, resource_id)
WITH CLUSTERING ORDER BY (resource_type ASC, effective_date DESC, service_id ASC, system_instance_id ASC, resource_id ASC);

CREATE TABLE person_resource_event_store (  person_id uuid,  resource_type text,  service_id uuid,  system_instance_id uuid,  resource_id text,
  version text,
  created timestamp,  mode text,
  resource_metadata text,  schema_version text,  resource_data text,  PRIMARY KEY ((person_id), resource_type, service_id, system_instance_id, resource_id, version));

CREATE MATERIALIZED VIEW person_resource_event_store_by_date AS 
SELECT person_id, created, resource_type, service_id, system_instance_id, resource_id, version, mode
FROM person_resource_event_store
WHERE person_id IS NOT NULL
  AND created IS NOT NULL
  AND resource_type IS NOT NULL
  AND service_id IS NOT NULL
  AND system_instance_id IS NOT NULL
  AND resource_id IS NOT NULL
  AND version IS NOT NULL
PRIMARY KEY((person_id), created, resource_type, service_id, system_instance_id, resource_id, version);
