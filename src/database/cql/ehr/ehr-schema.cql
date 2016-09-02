// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS ehr;
CREATE KEYSPACE ehr WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE ehr;

DROP TABLE IF EXISTS resource_history;
DROP TABLE IF EXISTS resource_history_by_service;

DROP MATERIALIZED VIEW IF EXISTS resource_by_patient;
DROP TABLE IF EXISTS resource_by_service;

DROP TABLE IF EXISTS resource_by_exchange_batch;
DROP TABLE IF EXISTS exchange_batch;



// 1. Get Resource based on ID and Type				= resource_history
// 2. Get Resource History						= resource_history
// 3. Get Resources based on Type for a Service			= resource_by_service
// 4. Get Resources based on Type for a Service with Date Range	= resource_history_by_service
// 5. Get Resources based on Type for a Patient			= resource_by_patient
// 6. Get Resources based on Exchange Batch				= resource_by_exchange_batch

CREATE TABLE resource_history (
  resource_id uuid,
  resource_type text,
  version timeuuid,
  created_at timestamp,
  service_id uuid,
  system_id uuid,
  is_deleted boolean,
  schema_version text,
  resource_data text,
  PRIMARY KEY ((resource_id, resource_type), version)
) WITH CLUSTERING ORDER BY (version DESC);

CREATE TABLE resource_history_by_service (
  service_id uuid,
  system_id uuid,
  resource_type text,
  created_at timestamp,
  resource_id uuid,
  version timeuuid,
  is_deleted boolean,
  schema_version text,
  resource_data text,
  PRIMARY KEY ((service_id, system_id, resource_type), created_at, resource_id, version)
) WITH CLUSTERING ORDER BY (created_at DESC, resource_id ASC, version DESC);

CREATE TABLE resource_by_service (
  service_id uuid,
  system_id uuid,
  resource_type text,
  resource_id uuid,
  current_version timeuuid,
  updated_at timestamp,
  patient_id uuid,
  schema_version text,
  resource_metadata text,
  resource_data text,
  PRIMARY KEY((service_id, system_id, resource_type), resource_id)
);

CREATE MATERIALIZED VIEW resource_by_patient AS
SELECT service_id, system_id, patient_id, resource_type, resource_id, schema_version, resource_metadata, resource_data
FROM resource_by_service
WHERE service_id IS NOT NULL
  AND system_id IS NOT NULL
  AND patient_id IS NOT NULL
  AND resource_type IS NOT NULL
  AND resource_id IS NOT NULL
PRIMARY KEY((service_id, system_id, patient_id), resource_type, resource_id);

CREATE TABLE resource_by_exchange_batch (
  batch_id uuid,
  exchange_id uuid,
  resource_type text,
  resource_id uuid,
  version timeuuid,
  is_deleted boolean,
  schema_version text,
  resource_data text,
  PRIMARY KEY((batch_id), resource_type, resource_id, version)
);

CREATE TABLE exchange_batch (
  exchange_id uuid,
  batch_id uuid,
  inserted_at timestamp,
  PRIMARY KEY((exchange_id), batch_id)
);

DROP TABLE IF EXISTS person_consent_global;
DROP TABLE IF EXISTS person_consent_protocol;
DROP TABLE IF EXISTS person_consent_organisation;

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
	


DROP MATERIALIZED VIEW IF EXISTS patient_identifier_by_nhs_number;
DROP MATERIALIZED VIEW IF EXISTS patient_identifier_by_patient_id;
DROP TABLE IF EXISTS patient_identifier_by_local_id;
DROP TABLE IF EXISTS patient_identifier; --old table name
	
CREATE TABLE patient_identifier_by_local_id (
	service_id UUID,
	system_id UUID,
	nhs_number text,
	forenames text,
	surname text,
	date_of_birth TIMESTAMP,
	postcode text,
	gender text,
	patient_id UUID,
	timestamp TIMESTAMP,
	version TIMEUUID,
	local_id TEXT,
	local_id_system TEXT,
	PRIMARY KEY ((service_id), system_id, local_id, local_id_system, version))
	WITH CLUSTERING ORDER BY (system_id ASC, local_id ASC, local_id_system ASC, version DESC);
	
CREATE MATERIALIZED VIEW patient_identifier_by_nhs_number AS  
	SELECT service_id, system_id, nhs_number, forenames, surname, date_of_birth, postcode, gender,
			patient_id, timestamp, version, local_id, local_id_system
	FROM patient_identifier_by_local_id  
	WHERE service_id IS NOT NULL  
	AND system_id IS NOT NULL  
	AND nhs_number IS NOT NULL  
	AND forenames IS NOT NULL  
	AND surname IS NOT NULL  
	AND date_of_birth IS NOT NULL  
	AND postcode IS NOT NULL  
	AND gender IS NOT NULL  
	AND patient_id IS NOT NULL  
	AND timestamp IS NOT NULL  
	AND version IS NOT NULL  
	AND local_id IS NOT NULL  
	AND local_id_system IS NOT NULL  
	PRIMARY KEY ((nhs_number), service_id, system_id, local_id, local_id_system, version);

	
CREATE MATERIALIZED VIEW patient_identifier_by_patient_id AS  
	SELECT service_id, system_id, nhs_number, forenames, surname, date_of_birth, postcode, gender,
			patient_id, timestamp, version, local_id, local_id_system
	FROM patient_identifier_by_local_id  
	WHERE service_id IS NOT NULL  
	AND system_id IS NOT NULL  
	AND nhs_number IS NOT NULL  
	AND forenames IS NOT NULL  
	AND surname IS NOT NULL  
	AND date_of_birth IS NOT NULL  
	AND postcode IS NOT NULL  
	AND gender IS NOT NULL  
	AND patient_id IS NOT NULL  
	AND timestamp IS NOT NULL  
	AND version IS NOT NULL  
	AND local_id IS NOT NULL  
	AND local_id_system IS NOT NULL  
	PRIMARY KEY ((patient_id), service_id, system_id, local_id, local_id_system, version)
	WITH CLUSTERING ORDER BY (service_id ASC, system_id ASC, local_id ASC, local_id_system ASC, version DESC);
	
DROP TABLE IF EXISTS resource_types_used;

CREATE TABLE resource_types_used (
	service_id UUID,
	system_id UUID,
	resource_type text,
	PRIMARY KEY ((service_id), system_id, resource_type));
	
