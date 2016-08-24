// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS transform; 
CREATE KEYSPACE transform WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }; 

USE transform; 
 
DROP TABLE IF EXISTS emis_csv_code_map;

DROP MATERIALIZED VIEW IF EXISTS resource_id_map_by_eds_id;
DROP TABLE IF EXISTS resource_id_map;

  
CREATE TABLE emis_csv_code_map (
	service_id uuid,
	system_id uuid,
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
PRIMARY KEY ((service_id), system_id, medication, code_id, time_uuid))
WITH CLUSTERING ORDER BY (system_id ASC, medication ASC, code_id ASC, time_uuid DESC);

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

	