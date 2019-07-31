drop database if exists pcr_mpi;
create database pcr_mpi;

use pcr_mpi;

DROP TABLE IF EXISTS person_record;
DROP TABLE IF EXISTS patient_person_link;
DROP TABLE IF EXISTS patient_person_link_history;
DROP TABLE IF EXISTS organisation_person_routing;
DROP TABLE IF EXISTS patient_search_local_identifier;
DROP TABLE IF EXISTS patient_search_episode;
DROP TABLE IF EXISTS patient_search;


CREATE TABLE person_record
(
  person_id int NOT NULL AUTO_INCREMENT COMMENT 'auto-generated ID for this person',
  nhs_number character(10) NOT NULL COMMENT 'NHS number for this person',
  date_of_birth date COMMENT 'DoB for this person - if DoB will not be used to match, then this should be removed',
  publisher_pcr_database_name varchar(255) NOT NULL COMMENT 'name of the publisher pcr_patient DB that this person record is stored on',
  gp_practice_ods_code varchar(10) COMMENT 'ODS code of the registered GP practice - not used for matching, but detecting if a person needs moving to a different DB',
  CONSTRAINT pk_person_record PRIMARY KEY (person_id)
) COMMENT 'defines the person ID and attributes used to match to it';

CREATE UNIQUE INDEX ix_nhs_number ON person_record (nhs_number);


CREATE TABLE patient_person_link
(
  patient_id int NOT NULL COMMENT 'refers to patient.id in pcr_patient databases',
  service_id character(36) NOT NULL COMMENT 'refers to admin.service table',
  person_id int NOT NULL COMMENT 'refers to person_record table',
  CONSTRAINT pk_patient_person_link PRIMARY KEY (patient_id)
) COMMENT 'defines the current linkage between patient records and their person ID';

CREATE INDEX ix_person_id ON patient_person_link (person_id);


CREATE TABLE patient_person_link_history
(
  patient_id int NOT NULL,
  service_id character(36) NOT NULL,
  updated timestamp NOT NULL,
  new_person_id int NOT NULL,
  previous_person_id int,
  CONSTRAINT pk_patient_person_link_history PRIMARY KEY (patient_id, updated)
) COMMENT 'stores the history of matched person ID for a patient ID';

CREATE INDEX ix_updated ON patient_person_link_history (updated);


CREATE TABLE organisation_person_routing (
	gp_practice_ods_code varchar(10) NOT NULL COMMENT 'ODS code of a GP practice',
    publisher_pcr_database_name varchar(255) NOT NULL COMMENT 'name of the publisher pcr_patient DB that persons registered at this pracitce should be stored on',
    CONSTRAINT pk_organisation_person_routing PRIMARY KEY (gp_practice_ods_code)
) COMMENT 'stores the routing of which pcr_patient database person records should be stored on';





CREATE TABLE patient_search
(
	service_id char(36) NOT NULL,
	nhs_number varchar(10),
	forenames varchar(500),
	surname varchar(500),
	date_of_birth date,
	date_of_death date,
	address_line_1 VARCHAR(255),
	address_line_2 VARCHAR(255),
	address_line_3 VARCHAR(255),
	city VARCHAR(255),
	district VARCHAR(255),
	postcode varchar(8),
	gender varchar(7),
	patient_id int NOT NULL,
	last_updated timestamp NOT NULL,
	registered_practice_ods_code VARCHAR(50),
	dt_deleted datetime,
	CONSTRAINT pk_patient_search PRIMARY KEY (service_id, patient_id)
);

CREATE INDEX ix_patient
  ON patient_search (patient_id);

-- duplicate of primary key (clusterd index) so removed
/*CREATE INDEX ix_service_patient
  ON patient_search (service_id, patient_id);*/

CREATE INDEX ix_service_date_of_birth
  ON patient_search (service_id, date_of_birth, dt_deleted);

-- swap index to be NHS Number first, since that's more selective than a long list of service IDs
/*CREATE INDEX ix_service_nhs_number
  ON patient_search (service_id, nhs_number);*/

CREATE INDEX ix_service_nhs_number_2
  ON patient_search (nhs_number, service_id, dt_deleted);

CREATE INDEX ix_service_surname_forenames
  ON patient_search (service_id, surname, forenames, dt_deleted);



CREATE TABLE patient_search_episode
(
	service_id char(36) NOT NULL,
	patient_id int NOT NULL,
	episode_id int NOT NULL,
	registration_start date,
	registration_end date,
	care_mananger VARCHAR(255),
	organisation_name VARCHAR(255),
	organisation_type_code varchar(10),
	registration_type_code varchar(10),
	last_updated timestamp NOT NULL,
	registration_status_code varchar(10),
	dt_deleted datetime,
	CONSTRAINT pk_patient_search_episode PRIMARY KEY (service_id, patient_id, episode_id)
);

-- unique index required so patient merges trigger a change in patient_id
CREATE UNIQUE INDEX uix_patient_search_episode_id
  ON patient_search_episode (episode_id);

CREATE TABLE patient_search_local_identifier
(
	service_id char(36) NOT NULL,
	local_id varchar(1000),
	local_id_system varchar(1000),
	patient_id int NOT NULL,
	last_updated timestamp NOT NULL,
	dt_deleted datetime,
	CONSTRAINT pk_patient_search_local_identifier PRIMARY KEY (service_id, patient_id, local_id_system, local_id),
	CONSTRAINT fk_patient_search_local_identifier_patient_id FOREIGN KEY (service_id, patient_id)
		REFERENCES patient_search (service_id, patient_id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- index so patient search by local ID works in timely fashion
CREATE INDEX ix_patient_search_local_identifier_id_service_patient
  ON patient_search_local_identifier (local_id, service_id, patient_id, dt_deleted);
