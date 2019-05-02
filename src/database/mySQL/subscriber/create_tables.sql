-- Data Checking (Patient Identifiable) database WITH foreign keys

drop database subscriber_pi;
create database if not exists subscriber_pi;

use subscriber_pi;

DROP TABLE IF EXISTS date_precision;
DROP TABLE IF EXISTS organization;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS practitioner;
DROP TABLE IF EXISTS `schedule`;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS episode_of_care;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS encounter;
DROP TABLE IF EXISTS allergy_intolerance;
DROP TABLE IF EXISTS medication_statement;
DROP TABLE IF EXISTS medication_order;
DROP TABLE IF EXISTS flag;
DROP TABLE IF EXISTS observation;
DROP TABLE IF EXISTS procedure_request;
DROP TABLE IF EXISTS referral_request;
DROP TABLE IF EXISTS pseudo_id;
DROP TABLE IF EXISTS patient_contact;
DROP TABLE IF EXISTS patient_address;
DROP TABLE IF EXISTS patient_uprn;
DROP TABLE IF EXISTS subscriber_tables;
DROP TABLE IF EXISTS event_log;

-- Table: date_precision

CREATE TABLE date_precision
(
  id smallint NOT NULL,
  value character varying(11) NOT NULL,
  CONSTRAINT pk_date_precision_id PRIMARY KEY (id)
);
  
INSERT INTO date_precision (id, value) VALUES (1, 'year');
INSERT INTO date_precision (id, value) VALUES (2, 'month');
INSERT INTO date_precision (id, value) VALUES (5, 'day');
INSERT INTO date_precision (id, value) VALUES (12, 'minute');
INSERT INTO date_precision (id, value) VALUES (13, 'second');
INSERT INTO date_precision (id, value) VALUES (14, 'millisecond');

-- Table: organization

CREATE TABLE organization
(
  id bigint NOT NULL,
  ods_code character varying(50),
  name character varying(255),
  type_code character varying(50),
  type_desc character varying(255),
  postcode character varying(10),
  parent_organization_id bigint,
  CONSTRAINT pk_organization_id PRIMARY KEY (id)
);

CREATE UNIQUE INDEX organization_id
  ON organization
  (id);

CREATE INDEX fki_organization_parent_organization_id
  ON organization
  (parent_organization_id);

-- Table: location

CREATE TABLE location (
  id bigint NOT NULL,
  name character varying(255),
  type_code character varying(50),
  type_desc character varying(255),
  postcode character varying(10),
  managing_organization_id bigint,
  CONSTRAINT pk_location_id PRIMARY KEY (id),
  CONSTRAINT fk_location_organisation_id FOREIGN KEY (managing_organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION    
);

CREATE UNIQUE INDEX location_id
  ON location
  (id);
  
CREATE INDEX fk_location_managing_organisation_id
  ON location
  (managing_organization_id);

-- Table: practitioner

CREATE TABLE practitioner
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  name character varying(1024),
  role_code character varying(50),
  role_desc character varying(255),
  CONSTRAINT pk_practitioner_id PRIMARY KEY (id),
  CONSTRAINT fk_practitioner_organisation_id FOREIGN KEY (organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX practitioner_id
  ON practitioner
  (id);

-- Table: schedule

CREATE TABLE schedule
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  practitioner_id bigint,
  start_date date,
  type character varying(255),
  location character varying(255),
  name varchar(150), 
  CONSTRAINT pk_schedule_id PRIMARY KEY (organization_id, id),
  CONSTRAINT fk_schedule_organization_id FOREIGN KEY (organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: schedule_id

-- DROP INDEX schedule_id;

CREATE UNIQUE INDEX schedule_id
  ON schedule
  (id);

-- Table: person

CREATE TABLE person
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  title varchar(50),
  first_names varchar(255),
  last_name varchar(255),
  gender_concept_id int NOT NULL,
  nhs_number character varying(255),
  date_of_birth date,
  date_of_death date,
  postcode character varying(20),
  ethnic_code_concept_id int,
  registered_practice_organization_id bigint,
  CONSTRAINT pk_person_id PRIMARY KEY (id)
);

CREATE UNIQUE INDEX person_id
  ON person
  (id);  

-- Table: patient

CREATE TABLE patient
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  person_id bigint NOT NULL,
  title varchar(50),
  first_names varchar(255),
  last_name varchar(255),  
  gender_concept_id int NOT NULL,
  nhs_number character varying(255),
  date_of_birth date,
  date_of_death date,
  current_address_id bigint,
  ethnic_code_concept_id int,
  registered_practice_organization_id bigint,
  CONSTRAINT pk_patient_id_organization_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_patient_organization_id FOREIGN KEY (organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX patient_id
  ON patient
  (id);
  
CREATE INDEX patient_person_id
  ON patient
  (person_id);

-- Table: episode_of_care

CREATE TABLE episode_of_care
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  registration_type_concept_id int,
  registration_status_concept_id int,
  date_registered date,
  date_registered_end date,
  usual_gp_practitioner_id bigint,
  CONSTRAINT pk_episode_of_care_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_episode_of_care_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_episode_of_care_practitioner_id FOREIGN KEY (usual_gp_practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX episode_of_care_id
  ON episode_of_care
  (id);
  
CREATE INDEX episode_of_care_patient_id
  ON episode_of_care
  (patient_id);
  
CREATE INDEX episode_of_care_registration_type_concept_id
  ON episode_of_care
  (registration_type_concept_id);

CREATE INDEX episode_of_care_date_registered
  ON episode_of_care
  (date_registered);
  
CREATE INDEX episode_of_care_date_registered_end
  ON episode_of_care
  (date_registered_end);
  
CREATE INDEX episode_of_care_person_id
  ON episode_of_care
  (person_id);
  
CREATE INDEX episode_of_care_organization_id
  ON episode_of_care
  (organization_id);

-- Table: appointment

CREATE TABLE appointment
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  practitioner_id bigint,
  schedule_id bigint,
  start_date datetime,
  planned_duration integer NOT NULL,
  actual_duration integer,
  appointment_status_concept_id int NOT NULL,
  patient_wait integer,
  patient_delay integer,
  date_time_sent_in datetime,
  date_time_left datetime,
  source_id varchar(36), 
  cancelled_date datetime,
  CONSTRAINT pk_appointment_id PRIMARY KEY (organization_id,person_id,id),
  CONSTRAINT fk_appointment_organization_id FOREIGN KEY (organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_appointment_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX appointment_id
  ON appointment
  (id);

CREATE INDEX appointment_patient_id
  ON appointment
  (patient_id);

-- Table: encounter

CREATE TABLE encounter
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  practitioner_id bigint,
  appointment_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  episode_of_care_id bigint,
  service_provider_organization_id bigint,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  age_at_event decimal (5,2),
  type text,
  sub_type text,
  is_primary boolean,
  admission_method varchar(40),
  end_date date,
  institution_location_id text,
  CONSTRAINT pk_encounter_id PRIMARY KEY (organization_id,person_id,id),
  CONSTRAINT fk_encounter_appointment_id FOREIGN KEY (appointment_id)
      REFERENCES appointment (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_episode_of_care_id FOREIGN KEY (episode_of_care_id)
      REFERENCES episode_of_care (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_service_provider_organization_id FOREIGN KEY (service_provider_organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION		  
);

CREATE UNIQUE INDEX encounter_id
  ON encounter
  (id);

CREATE INDEX encounter_patient_id
  ON encounter
  (patient_id);

CREATE INDEX fki_encounter_appointment_id
  ON encounter
  (appointment_id);
  
CREATE INDEX fki_encounter_patient_id_organization_id
  ON encounter
  (patient_id, organization_id);

CREATE INDEX encounter_core_concept_id_clinical_effective_date
  ON encounter
  (core_concept_id, clinical_effective_date);

-- Table: allergy_intolerance

CREATE TABLE allergy_intolerance
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  is_review boolean NOT NULL,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  age_at_event decimal (5,2),
  is_primary boolean,  
  CONSTRAINT pk_allergy_intolerance_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_allergy_intolerance_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_allergy_intolerance_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_allergy_intolerance_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_allergy_intolerance_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX allergy_intolerance_id
  ON allergy_intolerance
  (id);

CREATE INDEX allergy_intolerance_patient_id
  ON allergy_intolerance
  (patient_id);

CREATE INDEX allergy_intolerance_core_concept_id
  ON allergy_intolerance
  (core_concept_id);

-- Table: medication_statement

CREATE TABLE medication_statement
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  is_active boolean NOT NULL,
  cancellation_date date,
  dose character varying(1000),
  quantity_value real,
  quantity_unit character varying(255),
  medication_statement_authorisation_type_concept_id int NOT NULL,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  bnf_reference int(6),
  age_at_event decimal (5,2),
  issue_method text,
  CONSTRAINT pk_medication_statement_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_medication_statement_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_statement_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_statement_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_statement_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION                
);

CREATE UNIQUE INDEX medication_statement_id
  ON medication_statement
  (id);

CREATE INDEX medication_statement_patient_id
  ON medication_statement
  (patient_id);

CREATE INDEX medication_statement_dmd_id
  ON medication_statement
  (patient_id);
  
-- Table: medication_order

CREATE TABLE medication_order
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  dose character varying(1000),
  quantity_value real,
  quantity_unit character varying(255),
  duration_days integer,
  estimated_cost real,
  medication_statement_id bigint,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  bnf_reference int(6),
  age_at_event decimal (5,2),
  issue_method text,
  CONSTRAINT pk_medication_order_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_medication_order_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_order_medication_statement_id FOREIGN KEY (medication_statement_id)
      REFERENCES medication_statement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_order_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_order_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_order_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION                  
);

CREATE UNIQUE INDEX medication_order_id
  ON medication_order
  (id);

CREATE INDEX medication_order_patient_id
  ON medication_order
  (patient_id);

CREATE INDEX medication_order_core_concept_id
  ON medication_order
  (core_concept_id);

-- Table: flag

CREATE TABLE flag
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  effective_date date,
  date_precision_id smallint,
  is_active boolean NOT NULL,
  flag_text text,
  CONSTRAINT pk_flag_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_flag_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
  REFERENCES patient (id, organization_id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_flag_date_precision FOREIGN KEY (date_precision_id)
  REFERENCES date_precision (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX flag_id
  ON flag
  (id);

CREATE INDEX flag_patient_id
  ON flag
  (patient_id);

-- Table: observation

CREATE TABLE observation
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  result_value real,
  result_value_units character varying(50),
  result_date date,
  result_text text,
  result_concept_id bigint,
  is_problem boolean NOT NULL,
  is_review boolean NOT NULL,
  problem_end_date date,
  parent_observation_id bigint,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  age_at_event decimal (5,2),
  episodicity_concept_id bigint,
  is_primary boolean,  
  CONSTRAINT pk_observation_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_observation_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_observation_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_observation_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_observation_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION                  
);

CREATE UNIQUE INDEX observation_id
  ON observation
  (id);

CREATE INDEX observation_patient_id
  ON observation
  (patient_id);

CREATE INDEX observation_core_concept_id
  ON observation
  (core_concept_id);

CREATE INDEX observation_core_concept_id_is_problem
  ON observation
  (`core_concept_id`,`is_problem`);

CREATE INDEX observation_core_concept_id_result_value
  ON observation
  (`core_concept_id`,`result_value`);

CREATE INDEX observation_non_core_concept_id
  ON observation
  (non_core_concept_id);
    
CREATE INDEX ix_observation_organization_id
  ON observation
  (organization_id);
  
CREATE INDEX ix_observation_clinical_effective_date
  ON observation
  (clinical_effective_date);

CREATE INDEX ix_observation_person_id
  ON observation
  (person_id);	
	
-- Table: procedure_request

CREATE TABLE procedure_request
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  procedure_request_status_concept_id int,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  age_at_event decimal (5,2),    
  is_primary boolean,  
  CONSTRAINT pk_procedure_request_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_procedure_request_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_procedure_request_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_procedure_request_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_procedure_request_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX procedure_request_id
  ON procedure_request
  (id);

CREATE INDEX procedure_request_patient_id
  ON procedure_request
  (patient_id);

-- Table: referral_request

CREATE TABLE referral_request
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  requester_organization_id bigint,
  recipient_organization_id bigint,
  referral_request_priority_concept_id int,
  referral_request_type_concept_id int,
  mode character varying(50),
  outgoing_referral boolean,
  is_review boolean NOT NULL,
  core_concept_id int NOT NULL,
  non_core_concept_id int NOT NULL,
  age_at_event decimal (5,2),
  is_primary boolean,  
  CONSTRAINT pk_referral_request_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_referral_request_date_precision FOREIGN KEY (date_precision_id)
      REFERENCES date_precision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_referral_request_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_referral_request_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_referral_request_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_referral_request_recipient_organization_id FOREIGN KEY (recipient_organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_referral_request_requester_organization_id FOREIGN KEY (requester_organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX referral_request_id
  ON referral_request
  (id);

CREATE INDEX referral_request_patient_id
  ON referral_request
  (patient_id);

CREATE INDEX referral_request_core_concept_id
  ON referral_request
  (core_concept_id);
  
-- Table: pseudo_id

CREATE TABLE pseudo_id
(
  patient_id character varying(255) NOT NULL,
  salt_key_name varchar(50) NOT NULL,
  pseudo_id character varying(255) NULL,
  CONSTRAINT pk_pseudo_id PRIMARY KEY (`patient_id`, `salt_key_name`)             
);

CREATE INDEX pseudo_id_pseudo_id
  ON pseudo_id
  (pseudo_id);

create table patient_uprn (
	patient_id bigint,
    organization_id bigint,
    person_id bigint,
    uprn bigint,
    qualifier varchar(50),
    `algorithm` varchar(255),
    `match` varchar(255),
    no_address boolean,
    invalid_address boolean,
    missing_postcode boolean,
    invalid_postcode boolean,
    CONSTRAINT pk_patient_id_organization_id PRIMARY KEY (`organization_id`,`person_id`,`patient_id`),
    CONSTRAINT fk_patient_uprn_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE UNIQUE INDEX patient_uprn_id
  ON patient_uprn
  (patient_id);

-- Table: patient_contact
  
CREATE TABLE patient_contact
(
  id                         bigint       NOT NULL,
  organization_id 			 bigint		  NOT NULL,
  patient_id                 bigint       NOT NULL,
  person_id 				 bigint,
  type_concept_id            bigint       NOT NULL COMMENT 'type of contact (e.g. home phone, mobile phone, email)',
  value                      varchar(255) NOT NULL COMMENT 'the actual phone number or email address',
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY (`organization_id`,`id`,`patient_id`,`person_id`),
  CONSTRAINT fk_patient_contact_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id)
) AUTO_INCREMENT = 1 COMMENT 'stores contact details (e.g. phone) for patients';

-- Table: patient_address

CREATE TABLE patient_address
(
  id                       bigint 			NOT NULL,
  organization_id 		   bigint		    NOT NULL,
  patient_id               bigint          	NOT NULL,
  person_id 			   bigint,
  address_line_1           varchar(255),
  address_line_2           varchar(255),
  address_line_3           varchar(255),
  address_line_4           varchar(255),
  postcode                 varchar(10),
  type_concept_id          bigint       	NOT NULL COMMENT 'type of address (e.g. home, temporary)',
  date_to				   date,
  lsoa_2001_code           varchar(9),
  lsoa_2011_code           varchar(9),
  msoa_2001_code           varchar(9),
  msoa_2011_code           varchar(9),
  ward_code                varchar(9),
  local_authority_code     varchar(9),
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY (`organization_id`,`id`,`patient_id`,`person_id`),
  CONSTRAINT fk_patient_address_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id)
) AUTO_INCREMENT = 1 COMMENT 'stores address details for patients';

-- Table: subscriber_tables

CREATE TABLE subscriber_tables
(
  id         tinyint      NOT NULL AUTO_INCREMENT ,
  table_name varchar(255) NOT NULL,
  PRIMARY KEY (id)
) AUTO_INCREMENT =1 COMMENT 'lookup of all the table names in this database';

-- Table: event_log

CREATE TABLE event_log
(
  id                         bigint   NOT NULL,
  entry_date                 datetime NOT NULL,
  entry_mode                 tinyint  NOT NULL COMMENT 'entry mode i.e. 0=insert, 1=update, 2=delete',
  table_id                   tinyint  NOT NULL COMMENT 'the table ID relevant to the entry',
  CONSTRAINT event_log_table_id
  FOREIGN KEY (table_id)
  REFERENCES subscriber_tables (id)
) AUTO_INCREMENT=1 COMMENT 'represents the transaction log of all core table entries';