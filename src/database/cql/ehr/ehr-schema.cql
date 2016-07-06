// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS ehr;
CREATE KEYSPACE ehr WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE ehr;

DROP MATERIALIZED VIEW IF EXISTS person_resource_event_store_by_date;
DROP TABLE IF EXISTS person_resource_event_store;

DROP MATERIALIZED VIEW IF EXISTS person_resource_metadata_by_date;
DROP TABLE IF EXISTS person_resource;

DROP TABLE IF EXISTS person_consent_global;
DROP TABLE IF EXISTS person_consent_protocol;
DROP TABLE IF EXISTS person_consent_organisation;

DROP MATERIALIZED VIEW IF EXISTS person_identifier;
DROP TABLE IF EXISTS person_identifier_by_nhs_number;

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

CREATE TABLE person_consent_global (
	person_id UUID,
	timestamp TIMESTAMP, 
	consent_given BOOLEAN,
	PRIMARY KEY ((person_id), timestamp))
	WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE person_consent_protocol (
	person_id UUID,
	protocol_id UUID,
	timestamp TIMESTAMP, 
	consent_given BOOLEAN,
	PRIMARY KEY ((person_id), protocol_id, timestamp))
	WITH CLUSTERING ORDER BY (protocol_id ASC, timestamp DESC);

CREATE TABLE person_consent_organisation (
	person_id UUID,
	protocol_id UUID,
	organisation_id UUID,
	timestamp TIMESTAMP, 
	consent_given BOOLEAN,
	PRIMARY KEY ((person_id), protocol_id, organisation_id, timestamp))
	WITH CLUSTERING ORDER BY (protocol_id ASC, organisation_id ASC, timestamp DESC);
	

CREATE TABLE person_identifier (
	organisation_id UUID,
	service_id UUID,
	local_id text,
	nhs_number text,
	forenames text,
	surname text,
	date_of_birth TIMESTAMP,
	postcode text,
	sex text,
	person_id UUID,
	timestamp TIMESTAMP,
	PRIMARY KEY (organisation_id, service_id, local_id, timestamp));
	WITH CLUSTERING ORDER BY (organisation_id ASC, service_id ASC, local_id ASC, timestamp DESC);
	
CREATE MATERIALIZED VIEW person_identifier_by_nhs_number AS  
	SELECT organisation_id, service_id, local_id, nhs_number, person_id, timestamp 
	FROM person_identifier  
	WHERE nhs_number IS NOT NULL  
	AND organisation_id IS NOT NULL 
	AND timestamp IS NOT NULL 
	AND person_id IS NOT NULL 
	AND local_id IS NOT NULL 
	AND service_id IS NOT NULL
	PRIMARY KEY(nhs_number, organisation_id, service_id, local_id, timestamp); 

	
