USE ehr;

DROP TABLE IF EXISTS resource_history;
DROP TABLE IF EXISTS resource_current;
DROP TABLE IF EXISTS exchange_batch;


CREATE TABLE resource_history (
  service_id varchar(36),
  system_id varchar(36),
  resource_type varchar(50),
  resource_id varchar(36),
  created_at timestamp,
  patient_id varchar(36),
  is_deleted boolean,
  resource_data text,
  resource_checksum bigint,
  exchange_batch_id varchar(36),
  CONSTRAINT pk_resource_history PRIMARY KEY (resource_id, resource_type, created_at DESC)
);

CREATE INDEX ix_resource_history_batch_id
ON resource_history (exchange_batch_id, resource_type, resource_id, created_at);

CREATE TABLE resource_current (
  service_id varchar(36),
  system_id varchar(36),
  resource_type varchar(50),
  resource_id varchar(36),
  updated_at datetime,
  patient_id varchar(36),
  is_deleted boolean,
  resource_data text,
  resource_checksum bigint,
  resource_metadata varchar(1000),
  CONSTRAINT pk_resource_current PRIMARY KEY (service_id, patient_id, resource_type, resource_id, system_id)
);

CREATE INDEX ix_resource_current_service_system_type_id
ON resource_current (service_id, system_id, resource_type, resource_id);

CREATE INDEX ix_resource_current_service_type_id_system
ON resource_current (service_id, resource_type, resource_id, system_id);

CREATE INDEX ix_resource_current_service_system_patient_type_id
ON resource_current (service_id, system_id, patient_id, resource_type, resource_id);

CREATE TABLE exchange_batch (
  exchange_id varchar(36),
  batch_id varchar(36),
  inserted_at datetime,
  eds_patient_id varchar(36),
  CONSTRAINT pk_exchange_batch PRIMARY KEY (exchange_id, batch_id)
);





