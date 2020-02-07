USE ehr_??; -- we can have multiple EHR databases

DROP TABLE IF EXISTS resource_history;
DROP TABLE IF EXISTS resource_current;
DROP TABLE IF EXISTS encounter_history;
DROP TABLE IF EXISTS encounter_current;

CREATE TABLE resource_history (
  service_id char(36),
  system_id char(36),
  resource_type varchar(50),
  resource_id char(36),
  created_at timestamp,
  patient_id varchar(36),
  resource_data mediumtext,
  resource_checksum bigint,
  is_deleted boolean,
  exchange_batch_id char(36),
  version char(36),
  CONSTRAINT pk_resource_history PRIMARY KEY (resource_id, resource_type, created_at DESC, version)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- index used to get data out for subscriber feeds
CREATE INDEX ix_resource_history_batch_id
ON resource_history (exchange_batch_id, resource_type, resource_id, created_at);

CREATE TABLE resource_current (
  service_id char(36),
  system_id char(36),
  resource_type varchar(50),
  resource_id char(36),
  updated_at datetime,
  patient_id varchar(36),
  resource_data mediumtext,
  resource_checksum bigint,
  resource_metadata varchar(1000),
  CONSTRAINT pk_resource_current PRIMARY KEY (service_id, patient_id, resource_type, resource_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- index used to retrieve specific resources for a service
CREATE INDEX ix_resource_current_service_type_id
ON resource_current (service_id, resource_type, resource_id);

/*CREATE INDEX ix_resource_current_service_type_id_system
ON resource_current (service_id, resource_type, resource_id, system_id);*/

/*CREATE INDEX ix_resource_current_type_id
ON resource_current (resource_type, resource_id);*/

-- index used to retrieve current version of a resource and get its checksum
CREATE INDEX ix_resource_current_id_type_checksum
ON resource_current (resource_id, resource_type, resource_checksum);

-- index used to prevent resources being duplicated if the patient ID changed
CREATE UNIQUE INDEX uix_resource_current_id_type
ON resource_current (resource_id, resource_type);


CREATE TABLE encounter_history (
    service_id char(36),
    composition_resource_id char(36),
    created_at datetime,

    encounter_id char(36),
    patient_id varchar(36),
    practitioner_id char(36),
    appointment_id char(36),
    effective_date datetime,
    effective_end_date datetime,
    episode_of_care_id char(36),
    service_provider_organisation_id char(36),
    encounter_type char(50),
    parent_encounter_id char(36),
    additional_fields_json mediumtext,

    composition_encounter_checksum bigint,
    is_deleted boolean,
    exchange_batch_id char(36),
    CONSTRAINT pk_encounter_history PRIMARY KEY (composition_resource_id, encounter_id, created_at DESC)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- index used to get data out for subscriber feeds
CREATE INDEX ix_encounter_history_batch_id
    ON encounter_history (exchange_batch_id, composition_resource_id, encounter_id, created_at);


CREATE TABLE encounter_current (
    service_id char(36),
    composition_resource_id char(36),
    updated_at datetime,

    encounter_id char(36),
    patient_id varchar(36),
    practitioner_id char(36),
    appointment_id char(36),
    effective_date datetime,
    effective_end_date datetime,
    episode_of_care_id char(36),
    service_provider_organisation_id char(36),
    encounter_type char(50),
    parent_encounter_id char(36),
    additional_fields_json mediumtext,

    composition_encounter_checksum bigint,
    CONSTRAINT pk_encounter_current PRIMARY KEY (service_id, patient_id, composition_resource_id, encounter_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- index used to retrieve specific composition resources for a service
CREATE INDEX ix_encounter_current_service_id
    ON encounter_current (service_id, composition_resource_id);

-- index used to retrieve current version of a composition encounter and check its checksum to work out if it has changed
CREATE INDEX ix_encounter_current_id_type_checksum
    ON encounter_current (composition_resource_id, encounter_id, composition_encounter_checksum);
