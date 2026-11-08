-- use subscriber_new_pseudo;
-- use subscriber_new_pi;

DROP PROCEDURE IF EXISTS update_person_record;
DROP PROCEDURE IF EXISTS update_person_record_2;
drop trigger if exists after_patient_insert;
drop trigger if exists after_patient_update;
drop trigger if exists after_patient_delete;
drop trigger if exists after_patient_additional_insert;
drop trigger if exists after_patient_additional_update;
drop trigger if exists after_patient_additional_delete;
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
drop trigger if exists after_appointment_additional_insert;
drop trigger if exists after_appointment_additional_update;
drop trigger if exists after_appointment_additional_delete;
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
drop trigger if exists after_organization_metadata_insert;
drop trigger if exists after_organization_metadata_update;
drop trigger if exists after_organization_metadata_delete;
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
drop trigger if exists after_referral_request_additional_insert;
drop trigger if exists after_referral_request_additional_update;
drop trigger if exists after_referral_request_additional_delete;
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
drop trigger if exists after_patient_address_match_insert;
drop trigger if exists after_patient_address_match_update;
drop trigger if exists after_patient_address_match_delete;
drop trigger if exists after_patient_address_ralf_insert;
drop trigger if exists after_patient_address_ralf_update;
drop trigger if exists after_patient_address_ralf_delete;
drop trigger if exists after_schedule_additional_update;
drop trigger if exists after_schedule_additional_insert;
drop trigger if exists after_schedule_additional_delete;
DROP TABLE IF EXISTS patient_address_match;
DROP TABLE IF EXISTS patient_address_ralf;
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
DROP TABLE IF EXISTS referral_request_additional;
DROP TABLE IF EXISTS pseudo_id; -- deleted table
DROP TABLE IF EXISTS patient_contact;
DROP TABLE IF EXISTS patient_address;
DROP TABLE IF EXISTS patient_uprn;
DROP TABLE IF EXISTS patient_additional;
DROP TABLE IF EXISTS event_log;
DROP TABLE IF EXISTS encounter_event;
DROP TABLE IF EXISTS encounter_additional;
DROP TABLE IF EXISTS appointment_additional;
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
DROP TABLE IF EXISTS organization_metadata;
DROP TABLE IF EXISTS concept;
DROP TABLE IF EXISTS concept_map;
DROP TABLE IF EXISTS schedule_additional;

-- Table: organization

CREATE TABLE organization
(
    id bigint NOT NULL COMMENT 	'Unique Id of the organisation'	,
    ods_code varchar(50) NULL COMMENT 	'ODS Code of the organisation'	,
    name varchar(255) NULL COMMENT 	'Name of the organisation'	,
    type_code varchar(50) NULL COMMENT 	'The type of organisation'	,
    type_desc varchar(255) NULL COMMENT 	'Textual description of the type of organisation eg GP Practice'	,
    postcode varchar(10) NULL COMMENT 	'The postcode of the organisation'	,
    parent_organization_id bigint NULL COMMENT 	'The id of the parent organisation'	,
  CONSTRAINT pk_organization_id PRIMARY KEY (id)
);

CREATE UNIQUE INDEX organization_id
  ON organization
  (id);

CREATE INDEX fki_organization_parent_organization_id
  ON organization
  (parent_organization_id);

-- Table: organization_metadata

create table organization_metadata
(
    id bigint comment 'organisation ID, corresponds to same ID in the organizaton table',
    publishing_software varchar(50) comment 'software name of publishing system, i.e. SystmOne',
    last_data_to_dds datetime comment 'date time data was last sent to DDS',
    last_data_cutoff datetime comment 'cutoff date time of the last extract from the publishing system',
    CONSTRAINT pk_organization_metadata PRIMARY KEY (id, publishing_software)
);

-- Table: location

CREATE TABLE location (
  id bigint NOT NULL COMMENT 	'Unique Id of the location'	,
  name varchar(255) NULL COMMENT 	'The name of a location set by the publisher. E.g. ward, clinic, domiciliary'	,
  type_code varchar(50) NULL COMMENT 	'The type of location'	,
  type_desc varchar(255) NULL COMMENT 	'Textual description of the type of location eg GP Practice'	,
  postcode varchar(10) NULL COMMENT 	'The postcode of the location'	,
  managing_organization_id bigint NULL COMMENT 	'Reference to the managing organisation of the location'	,
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
    id bigint NOT NULL COMMENT 	'Unique Id of the practitioner',
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)',
    name varchar(1024) NULL COMMENT 	'Name of the practitioner',
    role_code varchar(50) NULL COMMENT 	'The code representing the role of the practitioner',
    role_desc varchar(255) NULL COMMENT 	'Textual description of the role of the practitioner eg General Medical Practitioner',
    gmc_code varchar(50) NULL COMMENT 	'The GMC code of the practitioner',
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
    id bigint NOT NULL COMMENT 	'Unique Id of the schedule'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    practitioner_id bigint NULL COMMENT 	'Reference to the practitioner who owns the schedule'	,
    start_date datetime NULL COMMENT 	'The start date of the schedule'	,
    type varchar(255) NULL COMMENT 	'The type of schedule eg Timed Appointments'	,
    location varchar(255) NULL COMMENT 	'Textual description of the location the schedule was held at'	,
    name varchar(150) NULL COMMENT 	'The name of the schedule'	,
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
    id bigint NOT NULL COMMENT 	'Unique Id of the person'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    title varchar(255) NULL COMMENT 	'The title of the person'	,
    first_names varchar(255) NULL COMMENT 	'The first names of the person'	,
    last_name varchar(255) NULL COMMENT 	'The last name of the person'	,
    gender_concept_id int NULL COMMENT 	'Reference to the gender of the person'	,
    nhs_number varchar(255) NULL COMMENT 	'The NHS number of the person'	,
    date_of_birth date NULL COMMENT 	'The date of birth of the person'	,
    date_of_death date NULL COMMENT 	'The date of death of the person'	,
    current_address_id bigint NULL COMMENT 	'Reference to the current address of the person'	,
    ethnic_code_concept_id int NULL COMMENT 	'Reference to the ethnicity of the person'	,
    registered_practice_organization_id bigint NULL COMMENT 	'Reference to the organisation the person is registered at'	,
    birth_year smallint,
    birth_month tinyint,
    birth_week tinyint,
  CONSTRAINT pk_person_id PRIMARY KEY (id)
);

CREATE UNIQUE INDEX person_id
  ON person
  (id);

-- Table: patient

CREATE TABLE patient
(
    id bigint NOT NULL COMMENT 	'Unique Id of the patient'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    title varchar(255)  COMMENT 	'The title of the patient'	,
    first_names varchar(255)  COMMENT 	'The first names of the patient'	,
    last_name varchar(255)  COMMENT 	'The last name of the patient'	,
    gender_concept_id int  COMMENT 	'Reference to the gender of the patient'	,
    nhs_number varchar(255)  COMMENT 	'The NHS number of the patient'	,
    date_of_birth date  COMMENT 	'The date of birth of the patient'	,
    date_of_death date  COMMENT 	'The date of death of the patient'	,
    current_address_id bigint  COMMENT 	'Reference to the current address of the patient'	,
    ethnic_code_concept_id int  COMMENT 	'Reference to the ethnicity of the patient'	,
    registered_practice_organization_id bigint  COMMENT 	'Reference to the organisation the patient is registered at'	,
    birth_year smallint,
    birth_month tinyint,
    birth_week tinyint,
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

-- Table: patient_additional

CREATE TABLE patient_additional (
  id bigint NOT NULL COMMENT 'same as the id column on the patient table ',
  property_id int NOT NULL COMMENT 'IM reference (e.g. Cause of death)',
  value_id int NULL COMMENT 'IM reference (e.g. COVID)',
  json_value json NULL COMMENT 'where there is no mapped value_id, just raw JSON',
  text_value varchar(255) NULL COMMENT 'where there is no mapped value_id or raw JSON, just a basic text value (i.e. GP Practitioner number)',
  CONSTRAINT pk_patient_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX ix_patient_additional_id
    ON patient_additional
    (value_id);
CREATE INDEX ix_patient_additional_text_value
    ON patient_additional
    (text_value);

-- Table: episode_of_care

CREATE TABLE episode_of_care
(
    id bigint NOT NULL COMMENT 	'Unique Id of the episode of care'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    registration_type_concept_id int NULL COMMENT 	'Reference to the registration type of the patient'	,
    registration_status_concept_id int NULL COMMENT 	'Reference to the registration status of the patient'	,
    date_registered date NULL COMMENT 	'The date the registration was started  for this episode of care'	,
    date_registered_end date NULL COMMENT 	'The date the registration was ended  for this episode of care'	,
    usual_gp_practitioner_id bigint NULL COMMENT 	'Reference to the usual GP for this episode of care'	,
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


-- Table: registration_status_history

CREATE TABLE registration_status_history (
 id bigint(20) NOT NULL COMMENT 	'Unique Id of the registration status history'	,
 organization_id bigint(20) NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
 patient_id bigint(20) NOT NULL COMMENT 	'Reference to the patient this registration status history belongs to'	,
 person_id bigint(20) NOT NULL COMMENT 	'Reference to the person this registration status history belongs to'	,
 episode_of_care_id bigint(20) NULL COMMENT 	'Reference to the episode of care this status history belongs to'	,
 registration_status_concept_id int(11) NULL COMMENT 	'Reference to the registration status'	,
 start_date datetime NULL COMMENT 	'The start date for the period this registration status history was valid'	,
 end_date datetime NULL COMMENT 	'The end date for the period this registration status history was valid'	,
   PRIMARY KEY (organization_id, id, patient_id, person_id),
  CONSTRAINT fk_registration_status_history_episode_id FOREIGN KEY (episode_of_care_id)
      REFERENCES episode_of_care (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
 );

 CREATE UNIQUE INDEX ux_registration_status_history_id ON registration_status_history (id);


-- Table: appointment

CREATE TABLE appointment
(
    id bigint NOT NULL COMMENT 	'Unique Id of the appointment'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    schedule_id bigint NULL COMMENT 	'The schedule the patient was put on to book multiple appointments. ID unique to the applied schedule'	,
    start_date datetime NULL COMMENT 	'The start date of the appointment'	,
    planned_duration int NULL COMMENT 	'The time allocated for the appointment, not necessarily the actual duration always in minutes'	,
    actual_duration int NULL COMMENT 	'Time between sent in and left always in minutes'	,
    appointment_status_concept_id int NULL COMMENT 	'The status of the appointment e.g. arrived/sent in/left/DNA'	,
    patient_wait int NULL COMMENT 	'How long the patient waited from being marked as arrived to being sent in'	,
    patient_delay int NULL COMMENT 	'How long the patient was delayed for'	,
    date_time_sent_in datetime NULL COMMENT 	'Date and time the patient was sent into the practitioner'	,
    date_time_left datetime NULL COMMENT 	'Date and time the patient left the practitioner'	,
    source_id varchar(36) NULL COMMENT 	'Unique reference to the source of the appointment'	,
    cancelled_date datetime NULL COMMENT 	'The date the appointment was cancelled'	,
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
    id bigint NOT NULL COMMENT 	'Unique Id of the encounter'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    appointment_id bigint NULL COMMENT 	'Reference to the appointment this encounter took part on'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the clinical code is recorded for'	,
    date_precision_concept_id int NULL COMMENT 	'Reference to the precision of the date of the encounter'	,
    episode_of_care_id bigint NULL COMMENT 	'Is this instance of the code a review of a previous encounter'	,
    service_provider_organization_id bigint NULL COMMENT 	'Reference to the service provider organisation'	,
    core_concept_id int NULL COMMENT 	'Reference to the type of encounter'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the type of encounter'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age the patient was when this encounter took place'	,
    type text NULL COMMENT 	'Unused'	,
    sub_type text NULL COMMENT 	'Unused'	,
    admission_method varchar(40) NULL COMMENT 	'The admission method of the encounter'	,
    end_date datetime NULL COMMENT 	'The end date of the encounter'	,
    institution_location_id text NULL COMMENT 	'Reference to the institution the encounter took place at'	,
    date_recorded datetime NULL COMMENT 	'The date the encounter was recorded'	,
  CONSTRAINT pk_encounter_id PRIMARY KEY (organization_id,person_id,id),
  /*known examples of Emis consultations referring to unknown appointments, so removed this
  CONSTRAINT fk_encounter_appointment_id FOREIGN KEY (appointment_id)
      REFERENCES appointment (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,*/
  CONSTRAINT fk_encounter_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
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




-- Table: encounter_event


CREATE TABLE encounter_event
(
    id bigint NOT NULL COMMENT 	'Unique Id of the encounter event'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The patient this event belongs to'	,
    person_id bigint NOT NULL COMMENT 	'The person this event belongs to'	,
    encounter_id bigint NOT NULL COMMENT 	'Reference to the parent encounter record'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    appointment_id bigint NULL COMMENT 	'Reference to the appointment this encounter took part on'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the encounter took place'	,
    date_precision_concept_id int NULL COMMENT 	'Reference to the precision of the date of the encounter'	,
    episode_of_care_id bigint NULL COMMENT 	'Reference to the episode of care this encounter belongs to'	,
    service_provider_organization_id bigint NULL COMMENT 	'Reference to the service provider organisation'	,
    core_concept_id int NULL COMMENT 	'Reference to the type of encounter'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the type of encounter'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age of the patient at the time of the encounter'	,
    type text NULL COMMENT 	'Unused'	,
    sub_type text NULL COMMENT 	'Unused'	,
    admission_method varchar(40) NULL COMMENT 	'The admission method of the encounter'	,
    end_date datetime NULL COMMENT 	'The end date of the encounter'	,
    institution_location_id bigint NULL COMMENT 	'Reference to the institution the encounter took place at'	,
    date_recorded datetime NULL COMMENT 	'The date the encounter was recorded'	,
    finished boolean NULL COMMENT 	'Whether the encounter is finished'	,
  CONSTRAINT pk_encounter_event_id PRIMARY KEY (organization_id, person_id, id),
  CONSTRAINT fk_encounter_event_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_event_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_event_episode_of_care_id FOREIGN KEY (episode_of_care_id)
      REFERENCES episode_of_care (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_encounter_event_service_provider_organization_id FOREIGN KEY (service_provider_organization_id)
      REFERENCES organization (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
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
  text_value varchar(255) NULL COMMENT 'where there is no mapped value_id, or raw JSON, just a basic text value (i.e. FIN number)',
  CONSTRAINT pk_encounter_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX encounter_additional_value_id
    ON encounter_additional
        (value_id);

-- Table: appointment_additional
CREATE TABLE appointment_additional
(
    id          bigint NOT NULL COMMENT 'same as the id column on the patient table ',
    property_id int    NOT NULL COMMENT 'IM reference (e.g. appointment type)',
    value_id    int NULL COMMENT 'IM reference (e.g. appointment type)',
    json_value  json NULL COMMENT 'where there is no mapped value_id, just raw JSON',
    text_value  varchar(255) NULL COMMENT 'where there is no mapped value_id or raw JSON, just a basic text value (i.e. GP Practitioner number)',
    CONSTRAINT pk_appointment_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX appointment_additional_value_id
    on appointment_additional
        (value_id);

-- Table: allergy_intolerance

CREATE TABLE allergy_intolerance
(
    id bigint NOT NULL COMMENT 	'Unique Id of the allergy'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint NULL COMMENT 	'Reference to the encounter this allergy was record in'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the clinical code is recorded for'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    is_review boolean NOT NULL COMMENT 	'Is this instance of the code a review of a previous encounter'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the allergy'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the allergy'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age the patient was at the time of this event'	,
    date_recorded datetime NULL COMMENT 	'The date the allergy was recorded'	,
    CONSTRAINT pk_allergy_intolerance_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_allergy_intolerance_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_allergy_intolerance_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_allergy_intolerance_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
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
    id bigint(20) NOT NULL COMMENT 	'Unique Id of the medication'	,
    organization_id bigint(20) NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint(20) NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint(20) NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint(20) NULL COMMENT 	'Reference to the encounter this medication was recorded in'	,
    practitioner_id bigint(20) NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the medication was clinical relevant'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    cancellation_date date NULL COMMENT 	'The date the medication was cancelled'	,
    dose varchar(1000) NULL COMMENT 	'Texual description of the dose of the medication'	,
    quantity_value real NULL COMMENT 	'The value of the medication that was prescribed eg 50'	,
    quantity_unit varchar(255) NULL COMMENT 	'The unit of the medication that was prescribed eg tablets'	,
    authorisation_type_concept_id int NULL COMMENT 	'Reference to the authorisation type'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the medication'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the medication'	,
    bnf_reference varchar(6) NULL COMMENT 	'A reference to the drug in the BNF dictionary'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age the patient was at the time of this event'	,
    issue_method text NULL COMMENT 	'The issue method of the medication eg hand written'	,
    date_recorded datetime,
  CONSTRAINT pk_medication_statement_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
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
  (core_concept_id);

-- Table: medication_order

CREATE TABLE medication_order
(
    id bigint NOT NULL COMMENT 	'Unique Id of the medication order'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint NULL COMMENT 	'Reference to the encounter the medication order was issued in'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the medication order was issued'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    dose varchar(1000) NULL COMMENT 	'Textual description of the dose'	,
    quantity_value real NULL COMMENT 	'The value of the medication that was prescribed eg 50'	,
    quantity_unit varchar(255) NULL COMMENT 	'The unit of the medication that was prescribed eg tablets'	,
    duration_days int NULL COMMENT 	'How many days the medication is prescribed for'	,
    estimated_cost real NULL COMMENT 	'The estimated cost of the medication'	,
    medication_statement_id bigint NULL COMMENT 	'Reference to the medication statement.  A medication statement can have many medication orders'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the medication'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the medication'	,
    bnf_reference varchar(6) NULL COMMENT 	'A reference to the drug in the BNF dictionary'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age the patient was at the time of this event'	,
    issue_method text NULL COMMENT 	'The issue method of the medication eg hand written'	,
  date_recorded datetime,
  CONSTRAINT pk_medication_order_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_medication_order_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_order_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_medication_order_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
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
    id bigint NOT NULL COMMENT 	'Unique Id of the flag'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    effective_date datetime NULL COMMENT 	'The date the flag was entered onto the patients record'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    is_active boolean NOT NULL COMMENT 	'Whether the flag is active or not'	,
    flag_text text  COMMENT 	'This is a warning set by the publisher regarding he patient'	,
  CONSTRAINT pk_flag_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_flag_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
  REFERENCES patient (id, organization_id) MATCH SIMPLE
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
    id bigint NOT NULL COMMENT 	'Unique Id of the observation'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint NULL COMMENT 	'Reference to the encounter the observation was recorded at'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the observation was identified by a clinician'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    result_value real NULL COMMENT 	'The value of the result of the observation'	,
    result_value_units varchar(50) NULL COMMENT 	'The units of the result of the observation'	,
    result_date date NULL COMMENT 	'The date of the result'	,
    result_text text  COMMENT 	'Any text associated with the result'	,
    result_concept_id int NULL COMMENT 	'Reference to the clinical coding of the result'	,
    is_problem boolean NOT NULL COMMENT 	'Whether the observation is marked as a problem'	,
    is_review boolean NOT NULL COMMENT 	'Whether the observation is a review of an existing problem'	,
    problem_end_date date NULL COMMENT 	'The end date of the problem'	,
    parent_observation_id bigint(20) NULL COMMENT 	'Reference to the parent observation in a complex observation eg systolic and diastolic blood pressures will have a parent observation of Blood pressure'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the observation'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the observation'	,
    age_at_event decimal (5,2) NULL COMMENT 	'The age of the patient at the time of the observation'	,
    episodicity_concept_id int NULL COMMENT 	'Reference to the episodicity of the problem eg First, review, flare'	,
    is_primary boolean NULL COMMENT 	'Whether the observation is a primary observation'	,
    date_recorded datetime NULL COMMENT 	'The date the observation was recorded in the system'	,
  CONSTRAINT pk_observation_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_observation_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_observation_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_observation_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
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

 -- Table: observation_additional

CREATE TABLE observation_additional (
  id bigint NOT NULL COMMENT 'same as the id column on the observation table',
  property_id int NOT NULL COMMENT 'IM reference (i.e. significance)', -- IM dbid reference
  value_id int NULL COMMENT 'IM reference (i.e. minor, significant)',
  json_value json NULL COMMENT 'the JSON data itself ',
  text_value varchar(255) NULL COMMENT 'where there is no mapped value_id or raw JSON, just a basic text value',
  CONSTRAINT pk_observation_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX observation_additional_value_id
    ON observation_additional
        (value_id);

 -- Table: diagnostic_order

CREATE TABLE diagnostic_order
(
    id bigint NOT NULL COMMENT 	'Unique Id of the diagnostic order'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint NULL COMMENT 	'Reference to the encounter the observation was recorded at'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the diagnostic order was identified by a clinician'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    result_value real NULL COMMENT 	'The value of the result of the observation'	,
    result_value_units varchar(50) NULL COMMENT 	'The units of the result of the observation'	,
    result_date date NULL COMMENT 	'The date of the result'	,
    result_text text NULL COMMENT 	'Any text associated with the result'	,
    result_concept_id int NULL COMMENT 	'Reference to the clinical coding of the result'	,
    is_problem boolean NOT NULL COMMENT 	'Whether the observation is marked as a problem'	,
    is_review boolean NOT NULL COMMENT 	'Whether the observation is a review of an existing problem'	,
    problem_end_date date NULL COMMENT 	'The end date of the problem'	,
    parent_observation_id bigint NULL COMMENT 	'Reference to the parent observation in a complex observation eg systolic and diastolic blood pressures will have a parent observation of Blood pressure'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the observation'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the observation'	,
    age_at_event decimal (5,2) NULL COMMENT 	'The age of the patient at the time of the observation'	,
    episodicity_concept_id int NULL COMMENT 	'Reference to the episodicity of the problem eg First, review, flare'	,
    is_primary boolean NULL COMMENT 	'Whether the diagnostic order is a primary order'	,
    date_recorded datetime,
  CONSTRAINT pk_diagnostic_order_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_diagnostic_order_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_diagnostic_order_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_diagnostic_order_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
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
    id bigint NOT NULL COMMENT 	'Unique Id of the procedure'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint NULL COMMENT 	'Reference to the encounter the procedure was administered at'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the procedure was administered by a clinician'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    status_concept_id int NULL COMMENT 	'Reference to the status of the procedure'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the procedure'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the procedure'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age of the patient at the time of the procedure'	,
    date_recorded datetime NULL COMMENT 	'The date the procedure was recorded in the source system'	,
  CONSTRAINT pk_procedure_request_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  /*got an Emis procedure request referring to a confidential encounter, so can't enforce this
  CONSTRAINT fk_procedure_request_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,*/
  CONSTRAINT fk_procedure_request_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_procedure_request_practitioner_id FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id) MATCH SIMPLE
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
    id bigint NOT NULL COMMENT 	'Unique Id of the referral'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    encounter_id bigint NULL COMMENT 	'Reference to the encounter the referral was made in'	,
    practitioner_id bigint NULL COMMENT 	'The clinician the activity is recorded against'	,
    clinical_effective_date datetime NULL COMMENT 	'The date the referral was made'	,
    date_precision_concept_id int NULL COMMENT 	'Identifies the precision of the clinical effectiveness date to either year (1) month (2) day (5) minute (12) second (13) millisecond (14)'	,
    requester_organization_id bigint NULL COMMENT 	'Reference to the organisation that made the refereral request'	,
    recipient_organization_id bigint NULL COMMENT 	'Reference to the organization receiving the referral'	,
    referral_request_priority_concept_id int NULL COMMENT 	'Reference to the priority of the referral'	,
    referral_request_type_concept_id int NULL COMMENT 	'Reference to the type of referral request'	,
    mode varchar(50) NULL COMMENT 	'The mode of the referral'	,
    outgoing_referral boolean NULL COMMENT 	'Whether this is an outgoing referral'	,
    is_review boolean NOT NULL COMMENT 	'Whether this referral is a review'	,
    core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the referral'	,
    non_core_concept_id int NULL COMMENT 	'Reference to the clinical coding of the referral'	,
    age_at_event decimal(5,2) NULL COMMENT 	'The age of the patient at the time of the referral'	,
    date_recorded datetime NULL COMMENT 	'The date the referral request was added to the source system'	,
  CONSTRAINT pk_referral_request_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  /*got an Emis referral request referring to a confidential encounter, so can't enforce this
  CONSTRAINT fk_referral_request_encounter_id FOREIGN KEY (encounter_id)
      REFERENCES encounter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,*/
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


-- Table: referral_request_additional

CREATE TABLE referral_request_additional (
     id          bigint NOT NULL COMMENT 'the id of the referral ',
     property_id int    NOT NULL COMMENT 'concept id for the e.g. referral service offered ',
     value_id    int NULL COMMENT 'currently unused ',
     json_value  json NULL COMMENT 'currently unused ',
     text_value  varchar(255) NULL COMMENT 'text name of the referral service offered, e.g. Ealing North MINT ',
     CONSTRAINT pk_referral_request_additional_id PRIMARY KEY (id, property_id)
);
CREATE INDEX ix_referral_request_additional_id
    ON referral_request_additional
        (value_id);
CREATE INDEX ix_referral_request_additional_text_value
    ON referral_request_additional
        (text_value);

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
  (pseudo_id);
*/

-- Table: patient_uprn

CREATE TABLE patient_uprn (
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times',
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)',
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations',
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
    id bigint NOT NULL COMMENT 	'Unique Id of the patient contact'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    use_concept_id int NULL COMMENT 	'use of contact (e.g. mobile, home,work'	,
    type_concept_id int NULL COMMENT 	'type of contact (e.g. phone, email)'	,
    start_date date NULL COMMENT 	'The start date of the contact being valid'	,
    end_date date NULL COMMENT 	'The end date of the contact being valid'	,
    value varchar(255) NULL COMMENT 	'The value of the contact information eg phone number, email address'	,
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY (`organization_id`,`id`,`patient_id`,`person_id`),
  CONSTRAINT fk_patient_contact_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id)
) COMMENT 'stores contact details (e.g. phone) for patients';

create unique index ux_patient_contact_id on patient_contact (id);

-- Table: patient_address

CREATE TABLE patient_address
(
    id bigint NOT NULL COMMENT 	'Unique Id of the address'	,
    organization_id bigint NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint NOT NULL COMMENT 	'The organisations record for this person’s registration. Patients may have multiple records across clinical systems and may have registered at an organisation multiple times'	,
    person_id bigint NOT NULL COMMENT 	'Unique individual across all organisations'	,
    address_line_1 varchar(255) NULL COMMENT 	'The first line of the address'	,
    address_line_2 varchar(255) NULL COMMENT 	'The second line of the address'	,
    address_line_3 varchar(255) NULL COMMENT 	'The third line of the address'	,
    address_line_4 varchar(255) NULL COMMENT 	'The fourth line of the address'	,
    city varchar(255) NULL COMMENT 	'The city'	,
    postcode varchar(255) NULL COMMENT 	'The postcode'	,
    use_concept_id int NOT NULL COMMENT 	'use of address (e.g. home, temporary)'	,
    start_date date NULL COMMENT 	'The start date of this address being relevant'	,
    end_date date NULL COMMENT 	'The end date of this address being relevant'	,
    lsoa_2001_code varchar(9) NULL COMMENT 	'A reference to the LSOA_2001 code'	,
    lsoa_2011_code varchar(9) NULL COMMENT 	'A reference to the LSOA_2011 code'	,
    msoa_2001_code varchar(9) NULL COMMENT 	'A reference to the MSOA_2001 code'	,
    msoa_2011_code varchar(9) NULL COMMENT 	'A reference to the MSOA_2011 code'	,
    ward_code varchar(9) NULL COMMENT 	'The ward the address belongs to'	,
    local_authority_code varchar(9) NULL COMMENT 	'The local authority the address belongs to'	,
    townsend_deprivation_index real,
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY (`organization_id`,`id`,`patient_id`,`person_id`),
  CONSTRAINT fk_patient_address_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id)
) COMMENT 'stores address details for patients';

create unique index ux_patient_address_id on patient_address (id);

-- Table: event_log

CREATE TABLE event_log (
  dt_change datetime(3) NOT NULL COMMENT 'date time the change was made to this DB',
  change_type tinyint NOT NULL COMMENT 'type of transaction 0=insert, 1=update, 2=delete',
  table_id tinyint NOT NULL COMMENT 'identifier of the table changed',
  record_id biginT NOT NULL COMMENT 'id of the record changed'
);
-- note: purposefully no primary key or any other constraint

-- Table: patient_pseudo_id

CREATE TABLE patient_pseudo_id
(
    id bigint(20) NOT NULL COMMENT 	'Unique Id of the patient pseudo id'	,
    organization_id bigint(20) NOT NULL COMMENT 	'Owning organisation (i.e. publisher)'	,
    patient_id bigint(20) NOT NULL COMMENT 	'Reference to the patient this registration status history belongs to'	,
    person_id bigint(20) NOT NULL COMMENT 	'Reference to the person this registration status history belongs to'	,
    salt_name varchar(50) NOT NULL COMMENT 	'The name of the salt used to create the pseudo id'	,
    skid varchar(255) NOT NULL COMMENT 	'"Secure Key IDentifier" - the pseudonymised ID generated for this patient using the salt specified in the salt_name column. Typically generated from patient NHS number, but is configurable on a case by case basis (e.g. may include date of birth)'	,
    is_nhs_number_valid boolean NOT NULL COMMENT 	'Whether the nhs number is valid'	,
    is_nhs_number_verified_by_publisher boolean NOT NULL COMMENT 	'Whether the nhs number has been verified by the publisher'	,
  CONSTRAINT pk_patient_pseudo_id PRIMARY KEY (`organization_id`,`person_id`,`id`),
  CONSTRAINT fk_patient_pseudo_id FOREIGN KEY (patient_id, organization_id)
      REFERENCES patient (id, organization_id)
);

CREATE UNIQUE INDEX ux_patient_pseudo_id ON patient_pseudo_id (id);

CREATE INDEX patient_pseudo_id_patient ON patient_pseudo_id (patient_id);

-- Table: patient_address_match

CREATE TABLE `patient_address_match` (
  `id`  bigint(50) NOT NULL AUTO_INCREMENT,
  `patient_address_id`  bigint(20) NOT NULL,
  `uprn` varchar(255) COLLATE utf8_bin,
  `uprn_ralf00` varchar(255),
  `status` tinyint(1) DEFAULT NULL,
  `uprn_property_classification` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `uprn_xcoordinate` double DEFAULT NULL,
  `uprn_ycoordinate` double DEFAULT NULL,
  `qualifier` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `match_rule` varchar(4096) CHARACTER SET utf8 DEFAULT NULL,
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
  `epoch` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `patient_address_uprn_index` (`uprn`),
  KEY `patient_address_patient_address_id` (`id`,`uprn`),
  CONSTRAINT `patient_address_uprn_patient_address_id_fk` FOREIGN KEY (`patient_address_id`) REFERENCES `patient_address` (`id`)
);

-- Table: patient_address_ralf

CREATE TABLE patient_address_ralf (
    id bigint NOT NULL,
    organization_id bigint NOT NULL,
    patient_id bigint NOT NULL,
    person_id bigint NOT NULL,
    patient_address_id bigint NOT NULL,
    patient_address_match_uprn_ralf00 varchar(255) NOT NULL,
    salt_name varchar(50) NOT NULL,
    ralf varchar(255) NOT NULL,
    CONSTRAINT pk_patient_address_ralf PRIMARY KEY (id, patient_address_id, patient_address_match_uprn_ralf00),
    CONSTRAINT fk_patient_address_ralf_organization_id FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_patient_address_ralf_patient_id FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_patient_address_ralf_person_id FOREIGN KEY (person_id) REFERENCES person (id),
    CONSTRAINT fk_patient_address_ralf_patient_address_id FOREIGN KEY (patient_address_id) REFERENCES patient_address (id)
);

CREATE INDEX ux_patient_address_ralf_id ON patient_address_ralf (id);

CREATE INDEX patient_address_ralf_patient_id ON patient_address_ralf (patient_id);

CREATE INDEX patient_address_ralf_patient_address_id ON patient_address_ralf (patient_address_id);

CREATE INDEX patient_address_ralf_patient_address_match_uprn_ralf_00 ON patient_address_ralf (patient_address_match_uprn_ralf00);

-- Table: concept

CREATE TABLE concept (
   dbid int(11) NOT NULL COMMENT 'Unique concept int DB identifier',
   document int(11) COMMENT 'Document this concept originated from',
   id varchar(150) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'Unique human-readable concept id',
   draft tinyint(1),
   name varchar(255) NULL COMMENT 	'Short name'	,
   description varchar(400) NULL COMMENT 	'Full name (or term for ontological concepts)'	,
   scheme bigint(20) NULL COMMENT 	'The coding scheme for the code (Read, CTV3, SNOMED etc)'	,
   code varchar(40) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 	'The code (non-unique unless coupled with a scheme)'	,
   use_count bigint(20) NULL DEFAULT 0 COMMENT 	'Rough indicator of number of occurences of the concept'	,
   updated datetime NOT NULL COMMENT 	'The timestamp of the last update to the concept'	,
   PRIMARY KEY (dbid),
   KEY ix_scheme_code (scheme,code),
   KEY ix_code (code)
 );

create index ix_dbid_code on concept (dbid, code);

-- Table: concept_map

 CREATE TABLE concept_map (
   legacy int(11) NOT NULL COMMENT 	'the legacy (read 2, ctv3, icd10, opcs4, emis local, etc) concept',
   core int(11) NOT NULL COMMENT 	'the core (snomed, discovery) concept that the legacy concept maps to',
   updated datetime NOT NULL COMMENT 	'Timestamp the map was last updated/added',
   id int(11),
   deleted tinyint(1),
   PRIMARY KEY (id),
   UNIQUE KEY concept_map_uq (legacy,deleted,updated)
 );

create index ix_legacy_core on concept_map (legacy, core);

CREATE TABLE IF NOT EXISTS database_version (
    version varchar(255) NOT NULL COMMENT 'version of the database',
    valid_from date NOT NULL COMMENT 'date that the version was updated',
    valid_to date NULL COMMENT 'date that the version was made outdated',
    wiki_page varchar(255) COMMENT 'the wiki page where this database structure is found',
    PRIMARY KEY (valid_from),
    UNIQUE KEY valid_to_uq (valid_to)
);

CREATE TABLE schedule_additional(
    id          bigint NOT NULL COMMENT 'the location id of the appointment ',
    property_id int    NOT NULL COMMENT 'concept id for the locationType',
    value_id    int NULL COMMENT 'unused',
    json_value  json NULL COMMENT 'unused',
    text_value  varchar(255) NULL COMMENT 'text representation of the schedule location type',
    CONSTRAINT pk_schedule_additional_id PRIMARY KEY (id, property_id)
);

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
	SELECT person_id, organization_id, title, first_names, last_name, gender_concept_id, nhs_number, date_of_birth, date_of_death,
	       current_address_id, ethnic_code_concept_id, registered_practice_organization_id, birth_year, birth_month, birth_week
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
    CREATE TRIGGER after_appointment_additional_insert
        AFTER INSERT
        ON appointment_additional
        FOR EACH ROW
    BEGIN
        INSERT INTO event_log (dt_change,
                               change_type,
                               table_id,
                               record_id)
        VALUES (now(3), -- current time inc ms
                0, -- insert
                39, -- appointment
                NEW.id);
        END$$
        DELIMITER ;

        DELIMITER $$
        CREATE TRIGGER after_appointment_additional_update
            AFTER UPDATE
            ON appointment_additional
            FOR EACH ROW
        BEGIN
            INSERT INTO event_log (dt_change,
                                   change_type,
                                   table_id,
                                   record_id)
            VALUES (now(3), -- current time inc ms
                    1, -- update
                    39, -- appointment_additional
                    NEW.id);
            END$$
            DELIMITER ;

            DELIMITER $$
            CREATE TRIGGER after_appointment_additional_delete
                AFTER DELETE
                ON appointment_additional
                FOR EACH ROW
            BEGIN
                INSERT INTO event_log (dt_change,
                                       change_type,
                                       table_id,
                                       record_id)
                VALUES (now(3), -- current time inc ms
                        2, -- delete
                        39, -- appointment_additional
                        OLD.id);
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
CREATE TRIGGER after_referral_request_additional_insert
    AFTER INSERT ON referral_request_additional
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
                 41, -- referral_request_additional
                 NEW.id
             );
    END$$
    DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_referral_request_additional_update
AFTER UPDATE ON referral_request_additional
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
             41, -- referral_request_additional
             NEW.id
         );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_referral_request_additional_delete
    AFTER DELETE ON referral_request_additional
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
                 41, -- referral_request_additional
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
        29, -- patient_additional
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
        29, -- patient_additional
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
        29, -- patient_additional
        OLD.id
    );
  END$$
DELIMITER ;



DELIMITER $$
CREATE TRIGGER after_patient_address_match_insert
    AFTER INSERT ON patient_address_match
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
                 22, -- patient_address_match
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_address_match_update
    AFTER UPDATE ON patient_address_match
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
                 22, -- patient_address_match
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_patient_address_match_delete
    AFTER DELETE ON patient_address_match
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
                 22, -- patient_address_match
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

DELIMITER $$
CREATE TRIGGER after_organization_metadata_insert
    AFTER INSERT ON organization_metadata
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
                 24, -- organization_metadata
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_organization_metadata_update
    AFTER UPDATE ON organization_metadata
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
                 24, -- organization_metadata
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_organization_metadata_delete
    AFTER DELETE ON organization_metadata
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
                 24, -- organization_metadata
                 OLD.id
             );
END$$

DELIMITER $$
CREATE TRIGGER after_schedule_additional_delete
    AFTER DELETE ON schedule_additional
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
                 40, -- schedule_additional
                 OLD.id
             );
END$$

DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_schedule_additional_insert
    AFTER INSERT ON schedule_additional
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
                 40, -- schedule_additional
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_schedule_additional_update
    AFTER UPDATE ON schedule_additional
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
                 40, -- schedule_additional
                 NEW.id
             );
END$$

DELIMITER ;

DELIMITER $$
    CREATE procedure update_database_version (IN ver varchar(255))
    BEGIN
    DELETE FROM database_version where valid_from = current_date();
    UPDATE database_version
    SET valid_to = CURRENT_DATE()
    WHERE valid_to is null;

    INSERT INTO database_version (version, valid_from, valid_to, wiki_page)
    VALUES (ver, current_date(), NULL, CONCAT('https://wiki.discoverydataservice.org/Compass_',UPPER(ver),'_Schema_Documentation'));
    END$$
DELIMITER ;

-- (adding a table increments a major version e.g. v2.2 adding a column increments minor version eg 2.1.1)
call update_database_version('2.3.0'); -- update this each time we update the version of the database