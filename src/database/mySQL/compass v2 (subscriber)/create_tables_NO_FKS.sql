-- use subscriber_new_pseudo;
-- use subscriber_new_pi;

DROP PROCEDURE IF EXISTS update_person_record;
DROP PROCEDURE IF EXISTS update_person_record_2;
drop trigger if exists after_patient_insert;
drop trigger if exists after_patient_update;
drop trigger if exists after_patient_delete;
drop trigger if exists after_person_insert;
drop trigger if exists after_person_update;
drop trigger if exists after_person_delete;
drop trigger if exists after_allergy_intolerance_insert;
drop trigger if exists after_allergy_intolerance_update;
drop trigger if exists after_allergy_intolerance_delete;
drop trigger if exists after_encounter_event_insert;
drop trigger if exists after_encounter_event_update;
drop trigger if exists after_encounter_event_delete;
drop trigger if exists after_encounter_additional_insert;
drop trigger if exists after_encounter_additional_update;
drop trigger if exists after_encounter_additional_delete;
drop trigger if exists after_encounter_insert;
drop trigger if exists after_encounter_update;
drop trigger if exists after_encounter_delete;
drop trigger if exists after_registration_status_history_insert;
drop trigger if exists after_registration_status_history_update;
drop trigger if exists after_registration_status_history_delete;
drop trigger if exists after_episode_of_care_insert;
drop trigger if exists after_episode_of_care_update;
drop trigger if exists after_episode_of_care_delete;
drop trigger if exists after_flag_insert;
drop trigger if exists after_flag_update;
drop trigger if exists after_flag_delete;
drop trigger if exists after_location_insert;
drop trigger if exists after_location_update;
drop trigger if exists after_location_delete;
drop trigger if exists after_medication_order_insert;
drop trigger if exists after_medication_order_update;
drop trigger if exists after_medication_order_delete;
drop trigger if exists after_medication_statement_insert;
drop trigger if exists after_medication_statement_update;
drop trigger if exists after_medication_statement_delete;
drop trigger if exists after_observation_insert;
drop trigger if exists after_observation_update;
drop trigger if exists after_observation_delete;
drop trigger if exists after_observation_additional_insert;
drop trigger if exists after_observation_additional_update;
drop trigger if exists after_observation_additional_delete;
drop trigger if exists after_organization_insert;
drop trigger if exists after_organization_update;
drop trigger if exists after_organization_delete;
drop trigger if exists after_practitioner_insert;
drop trigger if exists after_practitioner_update;
drop trigger if exists after_practitioner_delete;
drop trigger if exists after_procedure_request_insert;
drop trigger if exists after_procedure_request_update;
drop trigger if exists after_procedure_request_delete;
drop trigger if exists after_pseudo_id_insert;
drop trigger if exists after_pseudo_id_update;
drop trigger if exists after_pseudo_id_delete;
drop trigger if exists after_referral_request_insert;
drop trigger if exists after_referral_request_update;
drop trigger if exists after_referral_request_delete;
drop trigger if exists after_schedule_insert;
drop trigger if exists after_schedule_update;
drop trigger if exists after_schedule_delete;
drop trigger if exists after_appointment_insert;
drop trigger if exists after_appointment_update;
drop trigger if exists after_appointment_delete;
drop trigger if exists after_patient_contact_insert;
drop trigger if exists after_patient_contact_update;
drop trigger if exists after_patient_contact_delete;
drop trigger if exists after_patient_address_insert;
drop trigger if exists after_patient_address_update;
drop trigger if exists after_patient_address_delete;
drop trigger if exists after_diagnostic_order_insert;
drop trigger if exists after_diagnostic_order_update;
drop trigger if exists after_diagnostic_order_delete;
drop trigger if exists after_patient_pseudo_id_insert;
drop trigger if exists after_patient_pseudo_id_update;
drop trigger if exists after_patient_pseudo_id_delete;
DROP TABLE IF EXISTS patient_pseudo_id;
DROP TABLE IF EXISTS allergy_intolerance;
DROP TABLE IF EXISTS diagnostic_order;
DROP TABLE IF EXISTS medication_order;
DROP TABLE IF EXISTS medication_statement;
DROP TABLE IF EXISTS flag;
DROP TABLE IF EXISTS observation;
DROP TABLE IF EXISTS observation_additional;
DROP TABLE IF EXISTS procedure_request;
DROP TABLE IF EXISTS referral_request;
DROP TABLE IF EXISTS pseudo_id; -- deleted table
DROP TABLE IF EXISTS patient_contact;
DROP TABLE IF EXISTS patient_address;
DROP TABLE IF EXISTS patient_additional;
DROP TABLE IF EXISTS patient_uprn;
DROP TABLE IF EXISTS event_log;
DROP TABLE IF EXISTS encounter_event;
DROP TABLE IF EXISTS encounter_additional;
DROP TABLE IF EXISTS encounter;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS registration_status_history;
DROP TABLE IF EXISTS episode_of_care;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS `schedule`;
DROP TABLE IF EXISTS practitioner;
DROP TABLE IF EXISTS organization;
DROP TABLE IF EXISTS patient_address_match;


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
  CONSTRAINT pk_location_id PRIMARY KEY (id)
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
  gmc_code character varying(50),
  CONSTRAINT pk_practitioner_id PRIMARY KEY (id)
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
  CONSTRAINT pk_schedule_id PRIMARY KEY (organization_id, id)
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
  gender_concept_id int,
  nhs_number character varying(255),
  date_of_birth date,
  date_of_death date,
  current_address_id bigint,
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
  gender_concept_id int,
  nhs_number character varying(255),
  date_of_birth date,
  date_of_death date,
  current_address_id bigint,
  ethnic_code_concept_id int,
  registered_practice_organization_id bigint,
  CONSTRAINT pk_patient_id_organization_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
  CONSTRAINT pk_episode_of_care_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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


-- Table: registration_status_history
  
CREATE TABLE registration_status_history (
   id bigint(20) NOT NULL,
   organization_id bigint(20) NOT NULL,
   patient_id bigint(20) NOT NULL,
   person_id bigint(20) NOT NULL,
   episode_of_care_id bigint(20) DEFAULT NULL,
   registration_status_concept_id int(11) DEFAULT NULL,
   start_date datetime DEFAULT NULL,
   end_date datetime DEFAULT NULL,
   PRIMARY KEY (organization_id, id, patient_id, person_id)
 );
 
 CREATE UNIQUE INDEX ux_registration_status_history_id ON registration_status_history (id);

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
  planned_duration integer,
  actual_duration integer,
  appointment_status_concept_id int,
  patient_wait integer,
  patient_delay integer,
  date_time_sent_in datetime,
  date_time_left datetime,
  source_id varchar(36),
  cancelled_date datetime,
  CONSTRAINT pk_appointment_id PRIMARY KEY (organization_id,person_id,id)
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
  date_precision_concept_id int,
  episode_of_care_id bigint,
  service_provider_organization_id bigint,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  type text,
  sub_type text,
  admission_method varchar(40),
  end_date date,
  institution_location_id text,
  date_recorded datetime,
  CONSTRAINT pk_encounter_id PRIMARY KEY (organization_id,person_id,id)
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




-- Table: encounter_event


CREATE TABLE encounter_event
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint NOT NULL COMMENT 'parent encounter record',
  practitioner_id bigint,
  appointment_id bigint,
  clinical_effective_date datetime,
  date_precision_concept_id int,
  episode_of_care_id bigint,
  service_provider_organization_id bigint,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  type text,
  sub_type text,
  admission_method varchar(40),
  end_date date,
  institution_location_id bigint,
  date_recorded datetime,
  finished boolean,
  CONSTRAINT pk_encounter_event_id PRIMARY KEY (organization_id, person_id, id)
);

-- required for upserts to work
CREATE UNIQUE INDEX encounter_event_id
  ON encounter_event
  (id);



-- Table: encounter_additional
CREATE TABLE encounter_additional (
  id bigint NOT NULL COMMENT 'same as the id column on the encounter table',
  property_id int NOT NULL COMMENT 'IM concept id reference (i.e. Admission method)',
  value_id int NULL COMMENT 'IM concept id reference (i.e. Emergency admission)',
  json_value JSON NULL COMMENT 'where there is no mapped value_id, just raw JSON (i.e. birth delivery details)',
  CONSTRAINT pk_encounter_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX encounter_additional_value_id
    ON encounter_additional
        (value_id);


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
  date_precision_concept_id int,
  is_review boolean NOT NULL,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  date_recorded datetime,
  CONSTRAINT pk_allergy_intolerance_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
  date_precision_concept_id int,
  -- is_active boolean NULL,
  cancellation_date date,
  dose character varying(1000),
  quantity_value real,
  quantity_unit character varying(255),
  authorisation_type_concept_id int,
  core_concept_id int,
  non_core_concept_id int,
  bnf_reference varchar(6),
  age_at_event decimal (5,2),
  issue_method text,
  CONSTRAINT pk_medication_statement_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
  date_precision_concept_id int,
  dose character varying(1000),
  quantity_value real,
  quantity_unit character varying(255),
  duration_days integer,
  estimated_cost real,
  medication_statement_id bigint,
  core_concept_id int,
  non_core_concept_id int,
  bnf_reference varchar(6),
  age_at_event decimal (5,2),
  issue_method text,
  CONSTRAINT pk_medication_order_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
  date_precision_concept_id int,
  is_active boolean NOT NULL,
  flag_text text,
  CONSTRAINT pk_flag_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
  date_precision_concept_id int,
  result_value real,
  result_value_units character varying(50),
  result_date date,
  result_text text,
  result_concept_id int,
  is_problem boolean NOT NULL,
  is_review boolean NOT NULL,
  problem_end_date date,
  parent_observation_id bigint,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  episodicity_concept_id int,
  is_primary boolean,
  date_recorded datetime,
  CONSTRAINT pk_observation_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
 
 -- Table: observation_additional 
CREATE TABLE observation_additional (
  id bigint NOT NULL COMMENT 'same as the id column on the observation table',
  property_id bigint NOT NULL COMMENT 'IM reference (i.e. significance)', -- IM reference 
  value_id bigint(50) NULL COMMENT 'IM reference (i.e. minor, significant)',
  json_value json NULL COMMENT 'the JSON data itself ',
  CONSTRAINT pk_observation_additional_id PRIMARY KEY (id, property_id)
);

 -- Table: diagnostic_order

CREATE TABLE diagnostic_order
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint,
  practitioner_id bigint,
  clinical_effective_date date,
  date_precision_concept_id int,
  result_value real,
  result_value_units character varying(50),
  result_date date,
  result_text text,
  result_concept_id int,
  is_problem boolean NOT NULL,
  is_review boolean NOT NULL,
  problem_end_date date,
  parent_observation_id bigint,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  episodicity_concept_id int,
  is_primary boolean,
  CONSTRAINT pk_diagnostic_order_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
);

CREATE UNIQUE INDEX diagnostic_order_id
  ON diagnostic_order
  (id);

CREATE INDEX diagnostic_order_patient_id
  ON diagnostic_order
  (patient_id);

CREATE INDEX diagnostic_order_core_concept_id
  ON diagnostic_order
  (core_concept_id);

CREATE INDEX diagnostic_order_core_concept_id_is_problem
  ON diagnostic_order
  (`core_concept_id`,`is_problem`);

CREATE INDEX diagnostic_order_core_concept_id_result_value
  ON diagnostic_order
  (`core_concept_id`,`result_value`);

CREATE INDEX diagnostic_order_non_core_concept_id
  ON diagnostic_order
  (non_core_concept_id);

CREATE INDEX ix_diagnostic_order_organization_id
  ON diagnostic_order
  (organization_id);

CREATE INDEX ix_diagnostic_order_clinical_effective_date
  ON diagnostic_order
  (clinical_effective_date);

CREATE INDEX ix_diagnostic_order_person_id
  ON diagnostic_order
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
  date_precision_concept_id int,
  status_concept_id int,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  date_recorded datetime,
  CONSTRAINT pk_procedure_request_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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
  date_precision_concept_id int,
  requester_organization_id bigint,
  recipient_organization_id bigint,
  referral_request_priority_concept_id int,
  referral_request_type_concept_id int,
  mode character varying(50),
  outgoing_referral boolean,
  is_review boolean NOT NULL,
  core_concept_id int,
  non_core_concept_id int,
  age_at_event decimal (5,2),
  date_recorded datetime,
  CONSTRAINT pk_referral_request_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
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

/*
-- Table: pseudo_id

CREATE TABLE pseudo_id
(
  id bigint NOT NULL,
  patient_id bigint NOT NULL,
  salt_key_name varchar(50) NOT NULL,
  pseudo_id character varying(255) NULL,
  CONSTRAINT pk_pseudo_id PRIMARY KEY (`patient_id`, `salt_key_name`)
);

CREATE UNIQUE INDEX pseudo_id_id
  ON pseudo_id
  (id);


CREATE INDEX pseudo_id_pseudo_id
  ON pseudo_id
  (pseudo_id);*/

-- Table: patient_uprn

CREATE TABLE patient_uprn (
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
    CONSTRAINT pk_patient_id_organization_id PRIMARY KEY (`organization_id`,`person_id`,`patient_id`)
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
  use_concept_id            int COMMENT 'use of contact (e.g. mobile, home, work)',
  type_concept_id            int COMMENT 'type of contact (e.g. phone, email)',
  start_date date,
  end_date				   date,
  value                      varchar(255) COMMENT 'the actual phone number or email address',
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY (`organization_id`,`id`,`patient_id`,`person_id`)
) COMMENT 'stores contact details (e.g. phone) for patients';

create unique index ux_patient_contact_id on patient_contact (id);

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
  city varchar(255),
  postcode                 varchar(255),
  use_concept_id          int	NOT NULL COMMENT 'use of address (e.g. home, temporary)',
  start_date date,
  end_date				   date,
  lsoa_2001_code           varchar(9),
  lsoa_2011_code           varchar(9),
  msoa_2001_code           varchar(9),
  msoa_2011_code           varchar(9),
  ward_code                varchar(9),
  local_authority_code     varchar(9),
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY (`organization_id`,`id`,`patient_id`,`person_id`)
) COMMENT 'stores address details for patients';

create unique index ux_patient_address_id on patient_address (id);

CREATE TABLE patient_additional (
  id bigint NOT NULL COMMENT 'same as the id column on the patient table ',
  property_id character varying(255)  NOT NULL COMMENT 'IM reference (e.g. Cause of death)',
  value_id character varying(255) NOT NULL COMMENT 'IM reference (e.g. COVID)',
  CONSTRAINT pk_patient_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX ix_patient_additional_id
    ON patient_additional
    (value_id);

-- Table: event_log

CREATE TABLE event_log (
  dt_change datetime(3) NOT NULL COMMENT 'date time the change was made to this DB',
  change_type tinyint NOT NULL COMMENT 'type of transaction 0=insert, 1=update, 2=delete',
  table_id tinyint NOT NULL COMMENT 'identifier of the table changed',
  record_id biginT NOT NULL COMMENT 'id of the record changed'
);
-- note: purposefully no primary key or any other constraint

CREATE TABLE patient_pseudo_id
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  salt_name varchar(50) NOT NULL,
  skid varchar(255) NOT NULL,
  is_nhs_number_valid boolean NOT NULL,
  is_nhs_number_verified_by_publisher boolean NOT NULL,
  CONSTRAINT pk_patient_pseudo_id PRIMARY KEY (`organization_id`,`person_id`,`id`)
);

CREATE UNIQUE INDEX ux_patient_pseudo_id ON patient_pseudo_id (id);

CREATE INDEX patient_pseudo_id_patient ON patient_pseudo_id (patient_id);

-- Table: patient_address_match
CREATE TABLE `patient_address_match` (
  `id` bigint(20) NOT NULL,
  `uprn` varchar(255) COLLATE utf8_bin NOT NULL,
  `status` tinyint(1) DEFAULT NULL,
  `classification` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `xcoordinate` double DEFAULT NULL,
  `ycoordinate` double DEFAULT NULL,
  `qualifier` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `algorithm` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_date` datetime DEFAULT NULL,
  `abp_address_number` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_street` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_locality` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_town` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_postcode` varchar(10) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_organization` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_postcode` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_street` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_number` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_building` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_flat` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `algorithm_version` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `epoc` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`,`uprn`),
  KEY `patient_address_uprn_index` (`uprn`),
  KEY `patient_address_patient_address_id` (`id`,`uprn`)
);

CREATE TABLE patient_address_ralf
(
    id bigint NOT NULL,
    organization_id bigint NOT NULL,
    patient_id bigint NOT NULL,
    person_id bigint NOT NULL,
    patient_address_id bigint NOT NULL,
    patient_address_match_uprn_ralf00 varchar(255) NOT NULL,
    salt_name varchar(50) NOT NULL,
    skid varchar(255) NOT NULL,
    CONSTRAINT pk_patient_address_ralf PRIMARY KEY (id, patient_address_id, patient_address_match_uprn_ralf00)
);

CREATE UNIQUE INDEX ux_patient_address_ralf_id ON patient_address_ralf (id);

CREATE INDEX patient_address_ralf_patient_id ON patient_address_ralf (patient_id);

CREATE INDEX patient_address_ralf_patient_address_id ON patient_address_ralf (patient_address_id);

CREATE INDEX patient_address_ralf_patient_address_match_uprn_ralf_00 ON patient_address_ralf (patient_address_match_uprn_ralf00);

DELIMITER //
CREATE PROCEDURE update_person_record_2(
	IN _new_person_id bigint
)
BEGIN

	DECLARE _best_patient_id bigint DEFAULT -1;

	SET _best_patient_id = (
		SELECT id
		FROM
		(SELECT
			p.id,
			IF (e.registration_type_concept_id = 1335267, 1, 0) as `registration_type_rank`, -- if reg type = GMS then up-rank
			IF (e.registration_status_concept_id is null or e.registration_status_concept_id not in (1335283, 1335284, 1335285), 1, 0) as `registration_status_rank`, -- if pre-registered status, then down-rank
			IF (p.date_of_death is not null, 1, 0) as `death_rank`, --  records is a date of death more likely to be actively used, so up-vote
			IF (e.date_registered_end is null, '9999-12-31', e.date_registered_end) as `date_registered_end_sortable` -- up-vote non-ended ones
		FROM patient p
		LEFT OUTER JOIN episode_of_care e
			ON e.organization_id = p.organization_id
			AND e.patient_id = p.id
		WHERE
			p.person_id = _new_person_id
		ORDER BY
			registration_status_rank desc, -- avoid pre-registered records if possible
			death_rank desc, -- records marked as deceased are more likely to be used than ones not
			registration_type_rank desc, -- prefer GMS registrations over others
			date_registered desc, -- want the most recent registration
			date_registered_end_sortable desc
		LIMIT 1) AS `tmp`
	);

	REPLACE INTO person
	SELECT person_id, organization_id, title, first_names, last_name, gender_concept_id, nhs_number, date_of_birth, date_of_death, current_address_id, ethnic_code_concept_id, registered_practice_organization_id
	FROM patient
	WHERE id = _best_patient_id;

END //
DELIMITER ;


DELIMITER //
CREATE PROCEDURE update_person_record(
	IN _new_person_id bigint,
    IN _old_person_id bigint
)
BEGIN

    DECLARE _patients_remaning INT DEFAULT 1;

	IF (_new_person_id IS NOT NULL) THEN
		CALL update_person_record_2(_new_person_id);
	END IF;

    IF (_old_person_id IS NOT NULL) THEN

		SET _patients_remaning = (select count(1) from patient where person_id = _old_person_id);

        IF (_patients_remaning = 0) THEN
			DELETE FROM person
            WHERE id = _old_person_id;
        ELSE
			CALL update_person_record_2(_old_person_id);
        END IF;

    END IF;


END //
DELIMITER ;







DELIMITER $$
CREATE TRIGGER after_patient_insert
  AFTER INSERT ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        2, -- patient
        NEW.id
    );

    -- and update the person table too
    CALL update_person_record(NEW.person_id, null);
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_update
  AFTER UPDATE ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        2, -- patient
        NEW.id
    );

    -- and update the person table too
    CALL update_person_record(NEW.person_id, OLD.person_id);
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_delete
  AFTER DELETE ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        2, -- delete
        2, -- patient
        OLD.id
    );

    -- and update the person table too
    CALL update_person_record(null, OLD.person_id);
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_person_insert
  AFTER INSERT ON person
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        3, -- person
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_person_update
  AFTER UPDATE ON person
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        3, -- person
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_person_delete
  AFTER DELETE ON person
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        3, -- person
        OLD.id
    );
  END$$
DELIMITER ;





DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_insert
  AFTER INSERT ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        4, -- allergy_intolerance
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_update
  AFTER UPDATE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        4, -- allergy_intolerance
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_delete
  AFTER DELETE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        4, -- allergy_intolerance
        OLD.id
    );
  END$$
DELIMITER ;





DELIMITER $$
CREATE TRIGGER after_encounter_event_insert
  AFTER INSERT ON encounter_event
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        25, -- encounter_event
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_event_update
  AFTER UPDATE ON encounter_event
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        25, -- encounter_event
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_event_delete
  AFTER DELETE ON encounter_event
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        25, -- encounter_event
        OLD.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_encounter_additional_insert
  AFTER INSERT ON encounter_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        26, -- encounter_additional
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_additional_update
  AFTER UPDATE ON encounter_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        26, -- encounter_additional
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_additional_delete
  AFTER DELETE ON encounter_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        26, -- encounter_additional
        OLD.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_insert
  AFTER INSERT ON encounter
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        5, -- encounter
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_update
  AFTER UPDATE ON encounter
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        5, -- encounter
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_encounter_delete
  AFTER DELETE ON encounter
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        5, -- encounter
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_episode_of_care_insert
  AFTER INSERT ON episode_of_care
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        6, -- episode_of_care
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_episode_of_care_update
  AFTER UPDATE ON episode_of_care
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        6, -- episode_of_care
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_episode_of_care_delete
  AFTER DELETE ON episode_of_care
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        6, -- episode_of_care
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_registration_status_history_insert
  AFTER INSERT ON registration_status_history
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        23, -- registration_status_history
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_registration_status_history_update
  AFTER UPDATE ON registration_status_history
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        23, -- registration_status_history
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_registration_status_history_delete
  AFTER DELETE ON registration_status_history
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        23, -- registration_status_history
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_flag_insert
  AFTER INSERT ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        7, -- flag
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_flag_update
  AFTER UPDATE ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        7, -- flag
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_flag_delete
  AFTER DELETE ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        7, -- flag
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_location_insert
  AFTER INSERT ON location
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        8, -- location
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_location_update
  AFTER UPDATE ON location
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        8, -- location
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_location_delete
  AFTER DELETE ON location
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        8, -- location
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_medication_order_insert
  AFTER INSERT ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        9, -- medication_order
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_medication_order_update
  AFTER UPDATE ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        9, -- medication_order
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_medication_order_delete
  AFTER DELETE ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        9, -- medication_order
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_medication_statement_insert
  AFTER INSERT ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        10, -- medication_statement
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_medication_statement_update
  AFTER UPDATE ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        10, -- medication_statement
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_medication_statement_delete
  AFTER DELETE ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        10, -- medication_statement
        OLD.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_observation_insert
  AFTER INSERT ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        11, -- observation
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_observation_update
  AFTER UPDATE ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        11, -- observation
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_observation_delete
  AFTER DELETE ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        11, -- observation
        OLD.id
    );
  END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_observation_additional_insert
  AFTER INSERT ON observation_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        28, -- observation_additional
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_observation_additional_update
  AFTER UPDATE ON observation_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        28, -- observation_additional
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_observation_additional_delete
  AFTER DELETE ON observation_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        28, -- observation_additional
        OLD.id
    );
  END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_organization_insert
  AFTER INSERT ON organization
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        12, -- organization
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_organization_update
  AFTER UPDATE ON organization
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        12, -- organization
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_organization_delete
  AFTER DELETE ON organization
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        12, -- organization
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_practitioner_insert
  AFTER INSERT ON practitioner
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        13, -- practitioner
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_practitioner_update
  AFTER UPDATE ON practitioner
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        13, -- practitioner
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_practitioner_delete
  AFTER DELETE ON practitioner
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        13, -- practitioner
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_procedure_request_insert
  AFTER INSERT ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        14, -- procedure_request
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_procedure_request_update
  AFTER UPDATE ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        14, -- procedure_request
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_procedure_request_delete
  AFTER DELETE ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        14, -- procedure_request
        OLD.id
    );
  END$$
DELIMITER ;



/*

DELIMITER $$
CREATE TRIGGER after_pseudo_id_insert
  AFTER INSERT ON pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        15, -- pseudo_id
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_pseudo_id_update
  AFTER UPDATE ON pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        15, -- pseudo_id
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_pseudo_id_delete
  AFTER DELETE ON pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        15, -- pseudo_id
        OLD.id
    );
  END$$
DELIMITER ;

*/



DELIMITER $$
CREATE TRIGGER after_referral_request_insert
  AFTER INSERT ON referral_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        16, -- referral_request
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_referral_request_update
  AFTER UPDATE ON referral_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        16, -- referral_request
        NEW.id
    );
  END$$
DELIMITER ;





DELIMITER $$
CREATE TRIGGER after_referral_request_delete
  AFTER DELETE ON referral_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        16, -- referral_request
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_schedule_insert
  AFTER INSERT ON schedule
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        17, -- schedule
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_schedule_update
  AFTER UPDATE ON schedule
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        17, -- schedule
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_schedule_delete
  AFTER DELETE ON schedule
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        17, -- schedule
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_appointment_insert
  AFTER INSERT ON appointment
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        18, -- appointment
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_appointment_update
  AFTER UPDATE ON appointment
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        18, -- appointment
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_appointment_delete
  AFTER DELETE ON appointment
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        18, -- appointment
        OLD.id
    );
  END$$
DELIMITER ;




DELIMITER $$
CREATE TRIGGER after_patient_contact_insert
  AFTER INSERT ON patient_contact
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        19, -- patient_contact
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_contact_update
  AFTER UPDATE ON patient_contact
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        19, -- patient_contact
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_contact_delete
  AFTER DELETE ON patient_contact
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        19, -- patient_contact
        OLD.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_address_insert
  AFTER INSERT ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        20, -- patient_address
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_address_update
  AFTER UPDATE ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        20, -- patient_address
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_address_delete
  AFTER DELETE ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        20, -- patient_address
        OLD.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_diagnostic_order_insert
  AFTER INSERT ON diagnostic_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        21, -- diagnostic_order
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_diagnostic_order_update
  AFTER UPDATE ON diagnostic_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        21, -- diagnostic_order
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_diagnostic_order_delete
  AFTER DELETE ON diagnostic_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        21, -- diagnostic_order
        OLD.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_pseudo_id_insert
  AFTER INSERT ON patient_pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        27, -- patient_pseudo_id
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_pseudo_id_update
  AFTER UPDATE ON patient_pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        27, -- patient_pseudo_id
        NEW.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_pseudo_id_delete
  AFTER DELETE ON patient_pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        27, -- patient_pseudo_id
        OLD.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_additional_insert
  AFTER INSERT ON patient_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        28, -- patient_additional
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_additional_update
  AFTER UPDATE ON patient_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        28, -- patient_additional
        NEW.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_additional_delete
  AFTER DELETE ON patient_additional
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        28, -- patient_additional
        OLD.id
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_address_ralf_insert
    AFTER INSERT ON patient_address_ralf
    FOR EACH ROW
BEGIN
    INSERT INTO event_log (
        dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
                 now(3), -- current time inc ms
                 0, -- insert
                 30, -- patient_address_ralf
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_address_ralf_update
    AFTER UPDATE ON patient_address_ralf
    FOR EACH ROW
BEGIN
    INSERT INTO event_log (
        dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
                 now(3), -- current time inc ms
                 1, -- update
                 30, -- patient_address_ralf
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_address_ralf_delete
    AFTER DELETE ON patient_address_ralf
    FOR EACH ROW
BEGIN
    INSERT INTO event_log (
        dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
                 now(3), -- current time inc ms
                 2, -- delete
                 30, -- patient_address_ralf
                 OLD.id
             );
END$$
DELIMITER ;
