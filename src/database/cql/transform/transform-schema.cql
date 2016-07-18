// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS transform; 
CREATE KEYSPACE transform WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }; 

USE transform; 
 
DROP TABLE IF EXISTS emis_csv_code_map;

  
CREATE TABLE emis_csv_code_map (
  service_id uuid,
  system_instance_id uuid,
  medication boolean,
  code_id bigint,
  time_uuid timeuuid,
  code_type text,
  codeable_concept text,
  PRIMARY KEY ((service_id), system_instance_id, medication, code_id, time_uuid))
  WITH CLUSTERING ORDER BY (system_instance_id ASC, medication ASC, code_id ASC, time_uuid DESC);
