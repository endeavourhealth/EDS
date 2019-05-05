USE subscriber_transform_???? -- db name varies

DROP TABLE IF EXISTS enterprise_id_map;
DROP TABLE IF EXISTS enterprise_organisation_id_map;
DROP TABLE IF EXISTS household_id_map;
DROP TABLE IF EXISTS pseudo_id_map;
DROP TABLE IF EXISTS enterprise_age;
DROP TABLE IF EXISTS enterprise_person_id_map;
DROP TABLE IF EXISTS enterprise_person_update_history;
DROP TABLE IF EXISTS exchange_batch_extra_resources;
DROP TABLE IF EXISTS enterprise_instance_map;
DROP TABLE IF EXISTS vitru_care_patient_id_map;
DROP TABLE IF EXISTS pcr_id_map;
DROP TABLE IF EXISTS pcr_organisation_id_map;
DROP TABLE IF EXISTS pcr_person_id_map;
DROP TABLE IF EXISTS pcr_person_update_history;
DROP TABLE IF EXISTS pcr_instance_map;
DROP TABLE IF EXISTS pcr_free_text_id_map;
DROP TABLE IF EXISTS pcr_practitioner_id_map;
DROP TABLE IF EXISTS pcr_event_id_map;
DROP TABLE IF EXISTS code_set_codes;
DROP TABLE IF EXISTS code_set;
DROP TABLE IF EXISTS subscriber_id_map;
DROP TABLE IF EXISTS subscriber_pseudo_id_map;

CREATE TABLE enterprise_id_map
(
  resource_type varchar(50) NOT NULL,
  resource_id char(36) NOT NULL,
  enterprise_id bigint NOT NULL,
  CONSTRAINT pk_enterprise_id_map PRIMARY KEY (resource_id, resource_type)
);

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_enterprise_id_map_auto_increment
ON enterprise_id_map (enterprise_id);

ALTER TABLE enterprise_id_map MODIFY COLUMN enterprise_id INT auto_increment;



CREATE TABLE enterprise_organisation_id_map
(
  service_id char(36) NOT NULL,
  enterprise_id bigint NOT NULL,
  CONSTRAINT pk_enterprise_organisation_id_map PRIMARY KEY (service_id)
);


CREATE TABLE household_id_map
(
  postcode char(8) NOT NULL,
  line_1 varchar(255) NOT NULL,
  line_2 varchar(255) NOT NULL,
  household_id bigint NOT NULL,
  CONSTRAINT pk_household_id_map PRIMARY KEY (postcode, line_1, line_2)
);

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_household_id_map_auto_increment
ON household_id_map (household_id);

ALTER TABLE household_id_map MODIFY COLUMN household_id INT auto_increment;


CREATE TABLE pseudo_id_map
(
  patient_id char(36) NOT NULL PRIMARY KEY,
  pseudo_id varchar(255) NOT NULL
);

create index ix_pseudo_id_map_pseudo_id on pseudo_id_map(pseudo_id);

CREATE TABLE enterprise_age
(
  enterprise_patient_id bigint NOT NULL PRIMARY KEY,
  date_of_birth date NOT NULL,
  date_next_change date NOT NULL
);

CREATE INDEX ix_date_next_change
  ON enterprise_age (date_next_change);


CREATE TABLE enterprise_person_id_map
(
  enterprise_person_id bigint NOT NULL,
  person_id char(36) NOT NULL,
  CONSTRAINT pk_enterprise_person_id_map PRIMARY KEY (person_id)
);

-- this unique index is needed to make the column auto-increment
CREATE UNIQUE INDEX uix_enterprise_person_id_map_auto_increment
ON enterprise_person_id_map (enterprise_person_id);

ALTER TABLE enterprise_person_id_map MODIFY COLUMN enterprise_person_id INT auto_increment;


CREATE TABLE enterprise_person_update_history
(
  date_run timestamp NOT NULL,
  CONSTRAINT pk_person_update_history PRIMARY KEY (date_run)
);

CREATE TABLE vitru_care_patient_id_map (
	eds_patient_id char(36),
	service_id char(36),
	created_at datetime,
	vitrucare_id varchar(250),
    CONSTRAINT pk_resource_id_map PRIMARY KEY (eds_patient_id)
);

CREATE TABLE exchange_batch_extra_resources (
	exchange_id char(36) NOT NULL,
    batch_id char(36) NOT NULL,
    resource_id char(36) NOT NULL,
    resource_type varchar(50) NOT NULL,
    CONSTRAINT pk_exchange_batch_extra_resources PRIMARY KEY (exchange_id, batch_id, resource_id, resource_type)
);

CREATE TABLE enterprise_instance_map
(
	resource_type varchar(50) NOT NULL,
	resource_id_from char(36) NOT NULL,
	resource_id_to char(36),
	mapping_value varchar(1000),
	CONSTRAINT pk_enterprise_instance_map PRIMARY KEY (resource_id_from, resource_type)
);

CREATE INDEX ix_enterprise_instance_map_type_value
ON enterprise_instance_map (resource_type, mapping_value);

CREATE TABLE pcr_id_map
(
  resource_id   varchar(36) NOT NULL COMMENT 'resourceId from source',
  resource_type varchar(50) NOT NULL COMMENT 'resource type from source',
  pcr_id        bigint      NOT NULL COMMENT 'ID in PCR',
  -- source_db     int         NOT NULL COMMENT 'Pointer to pcr_db_map reference to source db',
  CONSTRAINT pk_pcr_id_map PRIMARY KEY (resource_id, resource_type)
)  COMMENT 'To track PCR data back to source';

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_pcr_id_map_auto_increment
  ON pcr_id_map (pcr_id);

ALTER TABLE pcr_id_map MODIFY COLUMN pcr_id bigint auto_increment;

CREATE TABLE pcr_organisation_id_map
(
  service_id char(36) NOT NULL,
  pcr_id bigint NOT NULL,
  CONSTRAINT pk_pcr_organisation_id_map PRIMARY KEY (service_id)
);

CREATE TABLE pcr_person_id_map
(
  person_id char(36) NOT NULL,
  pcr_person_id bigint NOT NULL,
  CONSTRAINT pk_pcr_person_id_map PRIMARY KEY (person_id)
);

-- this unique index is needed to make the column auto-increment
CREATE UNIQUE INDEX uix_pcr_person_id_map_auto_increment
  ON pcr_person_id_map (pcr_person_id);

ALTER TABLE pcr_person_id_map MODIFY COLUMN pcr_person_id bigint auto_increment;


CREATE TABLE pcr_person_update_history
(
  date_run timestamp NOT NULL,
  CONSTRAINT pk_pcr_person_update_history PRIMARY KEY (date_run)
);

CREATE TABLE pcr_instance_map
(
  resource_type varchar(50) NOT NULL,
  resource_id_from char(36) NOT NULL,
  resource_id_to char(36),
  mapping_value varchar(1000),
  CONSTRAINT pk_pcr_instance_map PRIMARY KEY (resource_id_from, resource_type)
);

CREATE INDEX ix_pcr_instance_map_type_value
  ON pcr_instance_map (resource_type, mapping_value);


CREATE TABLE pcr_free_text_id_map
(
  resource_id   varchar(36) NOT NULL COMMENT 'resourceId from source',
  resource_type varchar(50) NOT NULL COMMENT 'resource type from source',
  pcr_id        bigint      NOT NULL COMMENT 'ID in PCR',
  -- source_db     int         NOT NULL COMMENT 'Pointer to pcr_db_map reference to source db',
  CONSTRAINT pk_pcr_free_text_id_map PRIMARY KEY (resource_id, resource_type)
)  COMMENT 'To track PCR data back to source';

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_pcr_free_text_id_map_auto_increment
  ON pcr_free_text_id_map (pcr_id);

ALTER TABLE pcr_free_text_id_map MODIFY COLUMN pcr_id bigint auto_increment;


CREATE TABLE pcr_practitioner_id_map
(
  resource_id   varchar(36) NOT NULL COMMENT 'resourceId from source',
  resource_type varchar(50) NOT NULL COMMENT 'resource type from source',
  pcr_id        bigint      NOT NULL COMMENT 'ID in PCR',
  -- source_db     int         NOT NULL COMMENT 'Pointer to pcr_db_map reference to source db',
  CONSTRAINT pk_pcr_practitioner_id_map PRIMARY KEY (resource_id, resource_type)
)  COMMENT 'To track PCR data back to source';

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_pcr_practitioner_id_map_auto_increment
  ON pcr_practitioner_id_map (pcr_id);

ALTER TABLE pcr_practitioner_id_map MODIFY COLUMN pcr_id bigint auto_increment;

CREATE TABLE pcr_event_id_map
(
  resource_id   varchar(36) NOT NULL COMMENT 'resourceId from source',
  resource_type varchar(50) NOT NULL COMMENT 'resource type from source',
  pcr_id        bigint      NOT NULL COMMENT 'ID in PCR',
  -- source_db     int         NOT NULL COMMENT 'Pointer to pcr_db_map reference to source db',
  CONSTRAINT pk_pcr_event_id_map PRIMARY KEY (resource_id, resource_type)
)  COMMENT 'To track PCR data back to source';

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_pcr_event_id_map_auto_increment
  ON pcr_event_id_map (pcr_id);

ALTER TABLE pcr_event_id_map MODIFY COLUMN pcr_id bigint auto_increment;

CREATE TABLE code_set
(
  id int NOT NULL,
  code_set_name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE code_set_codes
(
  code_set_id int NOT NULL,
  read2_concept_id varchar(12) COLLATE utf8_bin,
  ctv3_concept_id varchar(12) COLLATE utf8_bin,
  sct_concept_id varchar (18),
  CONSTRAINT pk_code_set PRIMARY KEY (code_set_id, read2_concept_id, ctv3_concept_id, sct_concept_id),
  CONSTRAINT code_set_codes_code_set_id
  FOREIGN KEY (code_set_id)
  REFERENCES code_set (id)
);

CREATE TABLE subscriber_id_map
(
  subscriber_table tinyint NOT NULL COMMENT 'ID of the target table this ID is for',
  subscriber_id bigint NOT NULL COMMENT 'unique ID allocated for the subscriber DB',
  source_id varchar(250) NOT NULL COMMENT 'Source ID (e.g. FHIR reference) that this ID is mapped from',
  dt_previously_sent datetime NULL COMMENT 'the date time of the previously sent version of this resource (or null if deleted)',
  CONSTRAINT pk_subscriber_id_map PRIMARY KEY (source_id, subscriber_table)
);

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_subscriber_id_map_auto_increment
ON subscriber_id_map (subscriber_id);

ALTER TABLE subscriber_id_map MODIFY COLUMN subscriber_id INT auto_increment;



CREATE TABLE subscriber_pseudo_id_map
(
  patient_id char(36) NOT NULL,
  subscriber_patient_id bigint NOT NULL,
  salt_key_name varchar(255) NOT NULL,
  pseudo_id varchar(255) NOT NULL,
  CONSTRAINT pk_subscriber_pseudo_id_map PRIMARY KEY (patient_id, subscriber_patient_id, salt_key_name)
);

create index ix_subscriber_pseudo_id_map_pseudo_id on subscriber_pseudo_id_map(salt_key_name, pseudo_id);

