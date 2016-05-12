// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS ehr;
CREATE KEYSPACE ehr WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE ehr;

DROP TABLE IF EXISTS patient_resource;

CREATE TABLE patient_resource (  patient_id uuid,  service_id uuid,  resource_type text,  resource_id uuid,  effective_date timestamp,
  last_updated timestamp,  resource_meta_data text,  schema_version text,  resource_data text,  PRIMARY KEY ((patient_id), service_id, resource_type, resource_id));

CREATE MATERIALIZED VIEW patient_resource_by_date AS
SELECT patient_id, resource_type, effective_date, service_id, resource_id, resource_meta_data
FROM patient_resource
WHERE patient_id IS NOT NULL
  AND resource_type IS NOT NULL
  AND effective_date IS NOT NULL
  AND service_id IS NOT NULL
  AND resource_id IS NOT NULL
PRIMARY KEY((patient_id), resource_type, effective_date, service_id, resource_id)
WITH CLUSTERING ORDER BY (resource_type ASC, effective_date DESC, service_id ASC, resource_id ASC);





DROP TABLE IF EXISTS clinical_resource;

CREATE TABLE clinical_resource (  patient_id uuid,  service_id uuid,  resource_type text,  resource_id uuid,  effective_year int,
  last_updated timestamp,  schema_version text,  resource_data text,  PRIMARY KEY ((patient_id), service_id, resource_type, resource_id));

DROP TABLE IF EXISTS clinical_resource_index;

CREATE TABLE clinical_resource_index (  patient_id uuid,  resource_type text,  effective_year int,  meta_data text,  PRIMARY KEY ((patient_id), resource_type, effective_year))
WITH CLUSTERING ORDER BY (resource_type ASC, effective_year DESC);