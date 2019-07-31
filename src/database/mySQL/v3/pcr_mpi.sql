drop database if exists pcr_mpi;
create database pcr_mpi;

use pcr_mpi;

DROP TABLE IF EXISTS person_record;
DROP TABLE IF EXISTS patient_person_link;

CREATE TABLE person_record
(
  person_id int NOT NULL AUTO_INCREMENT COMMENT 'auto-generated ID for this person',
  nhs_number character(10) NOT NULL COMMENT 'NHS number for this person',
  date_of_birth date COMMENT 'DoB for this person - if DoB will not be used to match, then this should be removed',
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


