// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS transform; 
CREATE KEYSPACE transform WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE transform; 
 
DROP TABLE IF EXISTS emis_csv_code_map;
DROP TABLE IF EXISTS emis_admin_resource_cache;

DROP MATERIALIZED VIEW IF EXISTS resource_id_map_by_eds_id;
DROP TABLE IF EXISTS resource_id_map;

DROP TABLE IF EXISTS enterprise_id_map;
DROP TABLE IF EXISTS enterprise_id_max;
DROP TABLE IF EXISTS enterprise_organisation_id_map;
DROP TABLE IF EXISTS vitrucare_patient_id_map;

CREATE TABLE emis_csv_code_map (
	data_sharing_agreement_guid TEXT,
	medication boolean,
	code_id bigint,
	time_uuid timeuuid,
	code_type text,
	codeable_concept text,
	read_term TEXT,
	read_code TEXT,
	snomed_concept_id BIGINT,
	snomed_description_id BIGINT,
	snomed_term TEXT,
	national_code TEXT,
	national_code_category TEXT,
	national_code_description TEXT,
	parent_code_id bigint,
PRIMARY KEY ((data_sharing_agreement_guid), medication, code_id, time_uuid))
WITH CLUSTERING ORDER BY (medication ASC, code_id ASC, time_uuid DESC);

CREATE TABLE emis_admin_resource_cache (
	data_sharing_agreement_guid TEXT,
	emis_guid TEXT,
	resource_type TEXT,
	resource_data TEXT,
PRIMARY KEY ((data_sharing_agreement_guid), emis_guid, resource_type))
WITH compression = { 'sstable_compression' : 'LZ4Compressor' };

CREATE TABLE resource_id_map (
	service_id UUID, 
	system_id UUID, 
	resource_type text, 
	source_id text, 
	eds_id UUID, 
PRIMARY KEY ((service_id), system_id, resource_type, source_id));
	  

CREATE MATERIALIZED VIEW resource_id_map_by_eds_id AS
SELECT service_id, system_id, resource_type, source_id, eds_id
FROM resource_id_map
WHERE service_id IS NOT NULL
  AND system_id IS NOT NULL
  AND resource_type IS NOT NULL
  AND source_id IS NOT NULL
  AND eds_id IS NOT NULL
PRIMARY KEY((resource_type, eds_id), service_id, system_id, source_id);

/*CREATE TABLE enterprise_id_map (
    enterprise_table_name text,
	resource_type text, 
	resource_id UUID, 
	enterprise_id int, 
PRIMARY KEY ((enterprise_table_name), resource_type, resource_id));

CREATE INDEX enterprise_id ON transform.enterprise_id_map (enterprise_id);

CREATE TABLE enterprise_id_max (
    enterprise_table_name text,
	max_enterprise_id int,
PRIMARY KEY ((enterprise_table_name)));

	  
CREATE TABLE enterprise_organisation_id_map (
	organisation_ods_code text, 
	enterprise_id int, 
PRIMARY KEY (organisation_ods_code));	  
*/

CREATE TABLE vitrucare_patient_id_map (
	eds_patient_id UUID,
	service_id UUID,
	system_id UUID,
	created_at timestamp,
	vitrucare_id text,
PRIMARY KEY (eds_patient_id));