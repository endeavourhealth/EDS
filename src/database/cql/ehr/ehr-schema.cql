// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS ehr;
CREATE KEYSPACE ehr WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE ehr;

DROP TABLE IF EXISTS resource_history;
DROP TABLE IF EXISTS resource_history_by_service;

DROP MATERIALIZED VIEW IF EXISTS resource_by_patient;
DROP TABLE IF EXISTS resource_by_service;

DROP TABLE IF EXISTS resource_by_exchange_batch;
DROP TABLE IF EXISTS exchange_batch;

DROP TABLE IF EXISTS resource_types_used; //old table, not re-created

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
  resource_checksum bigint,
  PRIMARY KEY ((resource_id, resource_type), version)
) WITH CLUSTERING
ORDER BY (version DESC);

alter table resource_history
WITH compression = { 'sstable_compression' : 'LZ4Compressor' };

/*CREATE TABLE resource_history_by_service (
  service_id uuid,
  system_id uuid,
  resource_type text,
  created_at timestamp,
  resource_id uuid,
  version timeuuid,
  is_deleted boolean,
  schema_version text,
  resource_data text,
  PRIMARY KEY ((service_id, system_id, resource_type), resource_id, version)
) WITH CLUSTERING ORDER BY (resource_id ASC, version DESC)
WITH compression = { 'sstable_compression' : 'LZ4Compressor' };*/

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
)
WITH compression = { 'sstable_compression' : 'LZ4Compressor' };

CREATE MATERIALIZED VIEW resource_by_patient AS
SELECT service_id, system_id, patient_id, resource_type, resource_id, schema_version, resource_metadata, resource_data
FROM resource_by_service
WHERE service_id IS NOT NULL
  AND system_id IS NOT NULL
  AND patient_id IS NOT NULL
  AND resource_type IS NOT NULL
  AND resource_id IS NOT NULL
PRIMARY KEY((service_id, system_id, patient_id), resource_type, resource_id)
WITH compression = { 'sstable_compression' : 'LZ4Compressor' };

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
)
WITH compression = { 'sstable_compression' : 'LZ4Compressor' };

CREATE TABLE exchange_batch (
  exchange_id uuid,
  batch_id uuid,
  inserted_at timestamp,
  eds_patient_id uuid,
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
	




