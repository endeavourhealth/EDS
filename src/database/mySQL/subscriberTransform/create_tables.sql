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

