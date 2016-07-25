// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS transform; 
CREATE KEYSPACE transform WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }; 

USE transform; 
 
DROP TABLE IF EXISTS emis_csv_code_map;

DROP TABLE IF EXISTS resource_id_map;

  
CREATE TABLE emis_csv_code_map (
  service_id uuid,
  system_id uuid,
  medication boolean,
  code_id bigint,
  time_uuid timeuuid,
  code_type text,
  codeable_concept text,
  PRIMARY KEY ((service_id), system_id, medication, code_id, time_uuid))
  WITH CLUSTERING ORDER BY (system_id ASC, medication ASC, code_id ASC, time_uuid DESC);

CREATE TABLE resource_id_map (
	service_id UUID, 
	system_id UUID, 
	resource_type text, 
	source_id text, 
	eds_id UUID, 
	PRIMARY KEY ((service_id), system_id, resource_type, source_id));
	  