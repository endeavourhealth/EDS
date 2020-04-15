
-- drop TRIGGER
IF OBJECT_ID('dbo.after_patient_insert') IS NOT NULL DROP TRIGGER after_patient_insert
GO

IF OBJECT_ID('dbo.after_patient_update') IS NOT NULL DROP TRIGGER after_patient_update
GO

IF OBJECT_ID('dbo.after_patient_delete') IS NOT NULL DROP TRIGGER after_patient_delete
GO


-- drop SP
IF OBJECT_ID('dbo.update_person_record') IS NOT NULL DROP PROCEDURE update_person_record
GO

IF OBJECT_ID('dbo.update_person_record_2') IS NOT NULL DROP PROCEDURE update_person_record_2
GO


-- drop TABLES
IF OBJECT_ID('dbo.patient_uprn', 'U') IS NOT NULL DROP TABLE dbo.patient_uprn
GO

IF OBJECT_ID('dbo.medication_order', 'U') IS NOT NULL DROP TABLE dbo.medication_order
GO

IF OBJECT_ID('dbo.medication_statement', 'U') IS NOT NULL DROP TABLE dbo.medication_statement
GO

IF OBJECT_ID('dbo.flag', 'U') IS NOT NULL DROP TABLE dbo.flag
GO

IF OBJECT_ID('dbo.allergy_intolerance', 'U') IS NOT NULL DROP TABLE dbo.allergy_intolerance
GO

IF OBJECT_ID('dbo.condition', 'U') IS NOT NULL DROP TABLE dbo.condition
GO

IF OBJECT_ID('dbo.specimen', 'U') IS NOT NULL DROP TABLE dbo.specimen
GO

IF OBJECT_ID('dbo.diagnostic_order', 'U') IS NOT NULL DROP TABLE dbo.diagnostic_order
GO

IF OBJECT_ID('dbo.diagnostic_report', 'U') IS NOT NULL DROP TABLE dbo.diagnostic_report
GO

IF OBJECT_ID('dbo.family_member_history', 'U') IS NOT NULL DROP TABLE dbo.family_member_history
GO

IF OBJECT_ID('dbo.immunization', 'U') IS NOT NULL DROP TABLE dbo.immunization
GO

IF OBJECT_ID('dbo.observation', 'U') IS NOT NULL DROP TABLE dbo.observation
GO

IF OBJECT_ID('[dbo.procedure]', 'U') IS NOT NULL DROP TABLE [dbo.procedure]
GO

IF OBJECT_ID('dbo.procedure_request', 'U') IS NOT NULL DROP TABLE dbo.procedure_request
GO

IF OBJECT_ID('dbo.referral_request', 'U') IS NOT NULL DROP TABLE dbo.referral_request
GO

IF OBJECT_ID('dbo.encounter_raw', 'U') IS NOT NULL DROP TABLE dbo.encounter_raw
GO

IF OBJECT_ID('dbo.encounter_detail', 'U') IS NOT NULL DROP TABLE dbo.encounter_detail
GO

IF OBJECT_ID('dbo.encounter_event', 'U') IS NOT NULL DROP TABLE dbo.encounter_event
GO

IF OBJECT_ID('dbo.encounter', 'U') IS NOT NULL DROP TABLE dbo.encounter
GO

IF OBJECT_ID('dbo.appointment', 'U') IS NOT NULL DROP TABLE dbo.appointment
GO

IF OBJECT_ID('dbo.episode_of_care', 'U') IS NOT NULL DROP TABLE dbo.episode_of_care
GO

IF OBJECT_ID('dbo.patient', 'U') IS NOT NULL DROP TABLE dbo.patient
GO

IF OBJECT_ID('dbo.person', 'U') IS NOT NULL DROP TABLE dbo.person
GO

IF OBJECT_ID('dbo.schedule', 'U') IS NOT NULL DROP TABLE dbo.schedule
GO

IF OBJECT_ID('dbo.practitioner', 'U') IS NOT NULL DROP TABLE dbo.practitioner
GO

IF OBJECT_ID('dbo.location', 'U') IS NOT NULL DROP TABLE dbo.location
GO

IF OBJECT_ID('dbo.organization', 'U') IS NOT NULL DROP TABLE dbo.organization
GO

IF OBJECT_ID('dbo.date_precision', 'U') IS NOT NULL DROP TABLE dbo.date_precision
GO

IF OBJECT_ID('dbo.appointment_status', 'U') IS NOT NULL DROP TABLE dbo.appointment_status
GO

IF OBJECT_ID('dbo.procedure_request_status', 'U') IS NOT NULL DROP TABLE dbo.procedure_request_status
GO

IF OBJECT_ID('dbo.referral_request_priority', 'U') IS NOT NULL DROP TABLE dbo.referral_request_priority
GO

IF OBJECT_ID('dbo.referral_request_type', 'U') IS NOT NULL DROP TABLE dbo.referral_request_type
GO

IF OBJECT_ID('dbo.medication_statement_authorisation_type', 'U') IS NOT NULL DROP TABLE dbo.medication_statement_authorisation_type
GO

IF OBJECT_ID('dbo.patient_gender', 'U') IS NOT NULL DROP TABLE dbo.patient_gender
GO

IF OBJECT_ID('dbo.registration_type', 'U') IS NOT NULL DROP TABLE dbo.registration_type
GO

IF OBJECT_ID('dbo.registration_status', 'U') IS NOT NULL DROP TABLE dbo.registration_status
GO

IF OBJECT_ID('dbo.lsoa_lookup', 'U') IS NOT NULL DROP TABLE dbo.lsoa_lookup
GO

IF OBJECT_ID('dbo.msoa_lookup', 'U') IS NOT NULL DROP TABLE dbo.msoa_lookup
GO

IF OBJECT_ID('dbo.ward_lookup', 'U') IS NOT NULL DROP TABLE dbo.ward_lookup
GO

IF OBJECT_ID('dbo.local_authority_lookup', 'U') IS NOT NULL DROP TABLE dbo.local_authority_lookup
GO

IF OBJECT_ID('dbo.ethnicity_lookup', 'U') IS NOT NULL DROP TABLE dbo.ethnicity_lookup
GO

IF OBJECT_ID('dbo.link_distributor', 'U') IS NOT NULL DROP TABLE dbo.link_distributor
GO

IF OBJECT_ID('dbo.patient_address', 'U') IS NOT NULL DROP TABLE dbo.patient_address
GO

IF OBJECT_ID('dbo.patient_contact', 'U') IS NOT NULL DROP TABLE dbo.patient_contact
GO

IF OBJECT_ID('dbo.registration_status_history', 'U') IS NOT NULL DROP TABLE dbo.registration_status_history
GO


-- create TABLES
CREATE TABLE [ethnicity_lookup] 
(
  [ethnic_code] varchar(1) NOT NULL,
  [ethnic_name] varchar(100) NULL DEFAULT NULL,
  PRIMARY KEY ([ethnic_code])
)
GO

INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('A', 'British')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('B', 'Irish')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('C', 'Any other White background')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('D', 'White and Black Caribbean')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('E', 'White and Black African')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('F', 'White and Asian')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('G', 'Any other mixed background')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('H', 'Indian')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('J', 'Pakistani')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('K', 'Bangladeshi')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('L', 'Any other Asian background')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('M', 'Caribbean')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('N', 'African')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('P', 'Any other Black background')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('R', 'Chinese')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('S', 'Any other ethnic group')
INSERT INTO ethnicity_lookup (ethnic_code, ethnic_name) VALUES ('Z', 'Not stated')
GO

CREATE TABLE lsoa_lookup
(
  lsoa_code character(9) NOT NULL,
  lsoa_name character varying(255),
  imd_score decimal(5, 3),
  imd_rank integer,
  imd_decile integer,
  income_score decimal(5, 3),
  income_rank integer,
  income_decile integer,
  employment_score decimal(5, 3),
  employment_rank integer,
  employment_decile integer,
  education_score decimal(5, 3),
  education_rank integer,
  education_decile integer,
  health_score decimal(5, 3),
  health_rank integer,
  health_decile integer,
  crime_score decimal(5, 3),
  crime_rank integer,
  crime_decile integer,
  housing_and_services_barriers_score decimal(5, 3),
  housing_and_services_barriers_rank integer,
  housing_and_services_barriers_decile integer,
  living_environment_score decimal(5, 3),
  living_environment_rank integer,
  living_environment_decile integer,
  idaci_score decimal(5, 3),
  idaci_rank integer,
  idaci_decile integer,
  idaopi_score decimal(5, 3),
  idaopi_rank integer,
  idaopi_decile integer,
  children_and_young_sub_domain_score decimal(5, 3),
  children_and_young_sub_domain_rank integer,
  children_and_young_sub_domain_decile  integer,
  adult_skills_sub_somain_score decimal(5, 3),
  adult_skills_sub_somain_rank integer,
  adult_skills_sub_somain_decile integer,
  grographical_barriers_sub_domain_score decimal(5, 3),
  grographical_barriers_sub_domain_rank integer,
  grographical_barriers_sub_domain_decile integer,
  wider_barriers_sub_domain_score decimal(5, 3),
  wider_barriers_sub_domain_rank integer,
  wider_barriers_sub_domain_decile integer,
  indoors_sub_domain_score decimal(5, 3),
  indoors_sub_domain_rank integer,
  indoors_sub_domain_decile integer,
  outdoors_sub_domain_score decimal(5, 3),
  outdoors_sub_domain_rank integer,
  outdoors_sub_domain_decile integer,
  total_population integer,
  dependent_children_0_to_15 integer,
  population_16_to_59 integer,
  older_population_60_and_over integer,
  CONSTRAINT pk_lsoa_lookup PRIMARY KEY (lsoa_code)
)
GO

CREATE TABLE msoa_lookup
(
  msoa_code character(9) NOT NULL,
  msoa_name character varying(255),
  CONSTRAINT pk_msoa_lookup PRIMARY KEY (msoa_code)
)
GO

CREATE TABLE local_authority_lookup
(
  local_authority_code varchar(9) NOT NULL,
  local_authority_name varchar(255),
  CONSTRAINT pk_local_authority_lookup PRIMARY KEY (local_authority_code)
)
GO

CREATE TABLE ward_lookup
(
  ward_code varchar(9) NOT NULL,
  ward_name varchar(255),
  CONSTRAINT pk_ward_lookup PRIMARY KEY (ward_code)
)
GO

CREATE TABLE date_precision
(
  id smallint NOT NULL,
  value character varying(11) NOT NULL,
  CONSTRAINT pk_date_precision_id PRIMARY KEY (id)
)
GO

INSERT INTO date_precision (id, value) VALUES (1, 'year')
INSERT INTO date_precision (id, value) VALUES (2, 'month')
INSERT INTO date_precision (id, value) VALUES (5, 'day')
INSERT INTO date_precision (id, value) VALUES (12, 'minute')
INSERT INTO date_precision (id, value) VALUES (13, 'second')
INSERT INTO date_precision (id, value) VALUES (14, 'millisecond')
GO

CREATE TABLE appointment_status
(
  id smallint NOT NULL,
  value character varying(50) NOT NULL,
  CONSTRAINT pk_appointment_status_id PRIMARY KEY (id)
)
GO

INSERT INTO appointment_status (id, value) VALUES (0, 'Proposed')
INSERT INTO appointment_status (id, value) VALUES (1, 'Pending')
INSERT INTO appointment_status (id, value) VALUES (2, 'Booked')
INSERT INTO appointment_status (id, value) VALUES (3, 'Arrived')
INSERT INTO appointment_status (id, value) VALUES (4, 'Fulfilled')
INSERT INTO appointment_status (id, value) VALUES (5, 'Cancelled')
INSERT INTO appointment_status (id, value) VALUES (6, 'No Show')
GO

CREATE TABLE procedure_request_status
(
  id smallint NOT NULL,
  value character varying(50) NOT NULL,
  CONSTRAINT pk_procedure_request_status_id PRIMARY KEY (id)
)
GO

INSERT INTO procedure_request_status (id, value) VALUES (0, 'Proposed')
INSERT INTO procedure_request_status (id, value) VALUES (1, 'Draft')
INSERT INTO procedure_request_status (id, value) VALUES (2, 'Requested')
INSERT INTO procedure_request_status (id, value) VALUES (3, 'Received')
INSERT INTO procedure_request_status (id, value) VALUES (4, 'Accepted')
INSERT INTO procedure_request_status (id, value) VALUES (5, 'In Progress')
INSERT INTO procedure_request_status (id, value) VALUES (6, 'Completed')
INSERT INTO procedure_request_status (id, value) VALUES (7, 'Suspended')
INSERT INTO procedure_request_status (id, value) VALUES (8, 'Rejected')
INSERT INTO procedure_request_status (id, value) VALUES (9, 'Aborted')
GO

CREATE TABLE referral_request_priority
(
  id smallint NOT NULL,
  value character varying(50) NOT NULL,
  CONSTRAINT pk_referral_request_priority_id PRIMARY KEY (id)
)
GO

INSERT INTO referral_request_priority (id, value) VALUES (0, 'Routine')
INSERT INTO referral_request_priority (id, value) VALUES (1, 'Urgent')
INSERT INTO referral_request_priority (id, value) VALUES (2, 'Two Week Wait')
INSERT INTO referral_request_priority (id, value) VALUES (3, 'Soon')
GO

CREATE TABLE referral_request_type
(
  id smallint NOT NULL,
  value character varying(50) NOT NULL,
  CONSTRAINT pk_referral_request_type_id PRIMARY KEY (id)
)
GO

INSERT INTO referral_request_type (id, value) VALUES (0, 'Unknown')
INSERT INTO referral_request_type (id, value) VALUES (1, 'Assessment')
INSERT INTO referral_request_type (id, value) VALUES (2, 'Investigation')
INSERT INTO referral_request_type (id, value) VALUES (3, 'Management advice')
INSERT INTO referral_request_type (id, value) VALUES (4, 'Patient reassurance')
INSERT INTO referral_request_type (id, value) VALUES (5, 'Self referral')
INSERT INTO referral_request_type (id, value) VALUES (6, 'Treatment')
INSERT INTO referral_request_type (id, value) VALUES (7, 'Outpatient')
INSERT INTO referral_request_type (id, value) VALUES (8, 'Performance of a procedure / operation')
INSERT INTO referral_request_type (id, value) VALUES (9, 'Community Care')
INSERT INTO referral_request_type (id, value) VALUES (10, 'Admission')
INSERT INTO referral_request_type (id, value) VALUES (11, 'Day Care')
INSERT INTO referral_request_type (id, value) VALUES (12, 'Assessment & Education')
GO


CREATE TABLE medication_statement_authorisation_type
(
  id smallint NOT NULL,
  value character varying(50) NOT NULL,
  CONSTRAINT pk_medication_statement_authorisation_type_id PRIMARY KEY (id)
)
GO

INSERT INTO medication_statement_authorisation_type (id, value) VALUES (0, 'Acute')
INSERT INTO medication_statement_authorisation_type (id, value) VALUES (1, 'Repeat')
INSERT INTO medication_statement_authorisation_type (id, value) VALUES (2, 'Repeat Dispensing')
INSERT INTO medication_statement_authorisation_type (id, value) VALUES (3, 'Automatic')
GO

CREATE TABLE patient_gender
(
  id smallint NOT NULL,
  value character varying(10) NOT NULL,
  CONSTRAINT pk_patient_gender_id PRIMARY KEY (id)
)
GO

INSERT INTO patient_gender (id, value) VALUES (0, 'Male')
INSERT INTO patient_gender (id, value) VALUES (1, 'Female')
INSERT INTO patient_gender (id, value) VALUES (2, 'Other')
INSERT INTO patient_gender (id, value) VALUES (3, 'Unknown')
GO

CREATE TABLE registration_status
(
  id smallint NOT NULL,
  code character varying(10) NOT NULL,
  description character varying(50) NOT NULL,
  is_active bit NOT NULL,
  CONSTRAINT pk_registration_status_id PRIMARY KEY (id)
)
GO

INSERT INTO registration_status VALUES (0, 'PR1', 'Patient has presented', 'false')
INSERT INTO registration_status VALUES (1, 'PR2', 'Medical card received', 'false')
INSERT INTO registration_status VALUES (2, 'PR3', 'Application Form FP1 submitted', 'false')
INSERT INTO registration_status VALUES (3, 'R1', 'Registered', 'true')
INSERT INTO registration_status VALUES (4, 'R2', 'Medical record sent by FHSA', 'true')
INSERT INTO registration_status VALUES (5, 'R3', 'Record Received', 'true')
INSERT INTO registration_status VALUES (6, 'R4', 'Left Practice. Still Registered', 'true')
INSERT INTO registration_status VALUES (7, 'R5', 'Correctly registered', 'true')
INSERT INTO registration_status VALUES (8, 'R6', 'Short stay', 'true')
INSERT INTO registration_status VALUES (9, 'R7', 'Long stay', 'true')
INSERT INTO registration_status VALUES (10, 'D1', 'Death', 'false')
INSERT INTO registration_status VALUES (11, 'D2', 'Dead (Practice notification)', 'false')
INSERT INTO registration_status VALUES (12, 'D3', 'Record Requested by FHSA', 'false')
INSERT INTO registration_status VALUES (13, 'D4', 'Removal to New HA/HB', 'false')
INSERT INTO registration_status VALUES (14, 'D5', 'Internal transfer', 'false')
INSERT INTO registration_status VALUES (15, 'D6', 'Mental hospital', 'false')
INSERT INTO registration_status VALUES (16, 'D7', 'Embarkation', 'false')
INSERT INTO registration_status VALUES (17, 'D8', 'New HA/HB - same GP', 'false')
INSERT INTO registration_status VALUES (18, 'D9', 'Adopted child', 'false')
INSERT INTO registration_status VALUES (19, 'R8', 'Services', 'true')
INSERT INTO registration_status VALUES (20, 'D10', 'Deduction at GP''s request', 'false')
INSERT INTO registration_status VALUES (21, 'D11', 'Registration cancelled', 'false')
INSERT INTO registration_status VALUES (22, 'R9', 'Service dependant', 'true')
INSERT INTO registration_status VALUES (23, 'D12', 'Deduction at patient''s request', 'false')
INSERT INTO registration_status VALUES (24, 'D13', 'Other reason', 'false')
INSERT INTO registration_status VALUES (25, 'D14', 'Returned undelivered', 'false')
INSERT INTO registration_status VALUES (26, 'D15', 'Internal transfer - address change', 'false')
INSERT INTO registration_status VALUES (27, 'D16', 'Internal transfer within partnership', 'false')
INSERT INTO registration_status VALUES (28, 'D17', 'Correspondence states ''gone away''', 'false')
INSERT INTO registration_status VALUES (29, 'D18', 'Practice advise outside of area', 'false')
INSERT INTO registration_status VALUES (30, 'D19', 'Practice advise patient no longer resident', 'false')
INSERT INTO registration_status VALUES (31, 'D20', 'Practice advise removal via screening system', 'false')
INSERT INTO registration_status VALUES (32, 'D21', 'Practice advise removal via vaccination data', 'false')
INSERT INTO registration_status VALUES (33, 'R10', 'Removal from Residential Institute', 'true')
INSERT INTO registration_status VALUES (34, 'D22', 'Records sent back to FHSA', 'false')
INSERT INTO registration_status VALUES (35, 'D23', 'Records received by FHSA', 'false')
INSERT INTO registration_status VALUES (36, 'D24', 'Registration expired', 'false')
GO

CREATE TABLE registration_type
(
  id smallint NOT NULL,
  code character varying(10) NOT NULL,
  description character varying(30) NOT NULL,
  CONSTRAINT pk_registration_type_id PRIMARY KEY (id)
)
GO

INSERT INTO registration_type (id, code, description) VALUES (0, 'E', 'Emergency')
INSERT INTO registration_type (id, code, description) VALUES (1, 'IN', 'Immediately Necessary')
INSERT INTO registration_type (id, code, description) VALUES (2, 'R', 'Regular/GMS')
INSERT INTO registration_type (id, code, description) VALUES (3, 'T', 'Temporary')
INSERT INTO registration_type (id, code, description) VALUES (4, 'P', 'Private')
INSERT INTO registration_type (id, code, description) VALUES (5, 'O', 'Other')
INSERT INTO registration_type (id, code, description) VALUES (6, 'D', 'Dummy/Synthetic')
INSERT INTO registration_type (id, code, description) VALUES (7, 'C', 'Community')
INSERT INTO registration_type (id, code, description) VALUES (8, 'W', 'Walk-In')
INSERT INTO registration_type (id, code, description) VALUES (9, 'MS', 'Minor Surgery')
INSERT INTO registration_type (id, code, description) VALUES (10, 'CHS', 'Child Health Services')
INSERT INTO registration_type (id, code, description) VALUES (11, 'N', 'Contraceptive Services')
INSERT INTO registration_type (id, code, description) VALUES (12, 'Y', 'Yellow Fever')
INSERT INTO registration_type (id, code, description) VALUES (13, 'M', 'Maternity Services')
INSERT INTO registration_type (id, code, description) VALUES (14, 'PR', 'Pre-Registration')
INSERT INTO registration_type (id, code, description) VALUES (15, 'SH', 'Sexual Health')
INSERT INTO registration_type (id, code, description) VALUES (16, 'V', 'Vasectomy')
INSERT INTO registration_type (id, code, description) VALUES (17, 'OH', 'Out of Hours')
GO


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
)
GO

CREATE UNIQUE INDEX organization_id ON organization(id)
GO

CREATE INDEX fki_organization_parent_organization_id ON organization(parent_organization_id)
GO

CREATE TABLE location (
  id bigint NOT NULL,
  name character varying(255),
  type_code character varying(50),
  type_desc character varying(255),
  postcode character varying(10),
  managing_organization_id bigint,
  CONSTRAINT pk_location_id PRIMARY KEY (id)
)
GO

CREATE UNIQUE INDEX location_id ON location(id)
GO

CREATE INDEX fk_location_managing_organisation_id ON location(managing_organization_id)
GO

CREATE TABLE practitioner
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  name character varying(1024),
  role_code character varying(50),
  role_desc character varying(255),
  gmc_code character varying(50),
  CONSTRAINT pk_practitioner_id PRIMARY KEY (id)
)
GO

CREATE UNIQUE INDEX practitioner_id ON practitioner(id)
GO

CREATE TABLE schedule
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  practitioner_id bigint,
  start_date date,
  type character varying(255),
  location character varying(255),
  CONSTRAINT pk_schedule_id PRIMARY KEY (organization_id, id)
)
GO

CREATE UNIQUE INDEX schedule_id ON schedule(id)
GO

CREATE TABLE person
(
  id bigint NOT NULL,
  patient_gender_id smallint NOT NULL,
  pseudo_id character varying(255),
  age_years integer,
  age_months integer,
  age_weeks integer,
  date_of_death date,
  postcode_prefix character varying(20),
  lsoa_code character varying(50),
  msoa_code character varying(50),
  ethnic_code character(1),
  ward_code varchar(50),
  local_authority_code varchar(50),
  registered_practice_organization_id bigint,
  title character varying(50),
  first_names character varying(255),
  last_names character varying(255),
  current_address_id bigint,
  CONSTRAINT pk_person_id PRIMARY KEY (id)
)
GO

CREATE UNIQUE INDEX person_id ON person(id)
GO

CREATE TABLE patient
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  person_id bigint NOT NULL,
  patient_gender_id smallint NOT NULL,
  pseudo_id character varying(255),
  age_years integer,
  age_months integer,
  age_weeks integer,
  date_of_death date,
  postcode_prefix character varying(20),
  lsoa_code character varying(50),
  msoa_code character varying(50),
  ethnic_code character(1),
  ward_code varchar(50),
  local_authority_code varchar(50),
  registered_practice_organization_id bigint,
  title character varying(50),
  first_names character varying(255),
  last_names character varying(255),
  current_address_id bigint,  
  CONSTRAINT pk_patient_id_organization_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX patient_id ON patient(id)
GO

CREATE INDEX patient_person_id ON patient(person_id)
GO

CREATE TABLE episode_of_care
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  registration_type_id smallint,
  registration_status_id smallint,
  date_registered date,
  date_registered_end date,
  usual_gp_practitioner_id bigint,
  CONSTRAINT pk_episode_of_care_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX episode_of_care_id ON episode_of_care(id)
GO

CREATE INDEX episode_of_care_patient_id ON episode_of_care(patient_id)
GO

CREATE INDEX episode_of_care_registration_type_id ON episode_of_care(registration_type_id)
GO

CREATE INDEX episode_of_care_date_registered ON episode_of_care(date_registered)
GO

CREATE INDEX episode_of_care_date_registered_end ON episode_of_care(date_registered_end)
GO

CREATE INDEX episode_of_care_person_id ON episode_of_care(person_id)
GO

CREATE INDEX episode_of_care_organization_id ON episode_of_care(organization_id)
GO

CREATE TABLE appointment
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  practitioner_id bigint,
  schedule_id bigint,
  start_date datetime2(0),
  planned_duration integer,
  actual_duration integer,
  appointment_status_id smallint NOT NULL,
  patient_wait integer,
  patient_delay integer,
  sent_in datetime2(0),
  [left] datetime2(0),
  CONSTRAINT pk_appointment_id PRIMARY KEY (organization_id,person_id,id)
)
GO

CREATE UNIQUE INDEX appointment_id ON appointment(id)
GO

CREATE INDEX appointment_patient_id ON appointment(patient_id)
GO

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
  snomed_concept_id bigint,
  original_code character varying(100) collate Latin1_General_100_BIN2,
  original_term character varying(1000),
  episode_of_care_id bigint,
  service_provider_organization_id bigint,
  date_recorded datetime,
  CONSTRAINT pk_encounter_id PRIMARY KEY  (organization_id,person_id,id)
)
GO

CREATE UNIQUE INDEX encounter_id ON encounter(id)
GO

CREATE INDEX encounter_patient_id ON encounter(patient_id)
GO

CREATE INDEX fki_encounter_appointment_id ON encounter(appointment_id)
GO

CREATE INDEX fki_encounter_patient_id_organization_id ON encounter(patient_id, organization_id)
GO

CREATE INDEX encounter_snomed_concept_id_clinical_effective_date ON encounter(snomed_concept_id, clinical_effective_date)
GO




CREATE TABLE encounter_event
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  encounter_id bigint NOT NULL,
  practitioner_id bigint,
  appointment_id bigint,
  clinical_effective_date datetime,
  date_precision_id smallint,
  snomed_concept_id bigint,
  original_code character varying(100) collate Latin1_General_100_BIN2,
  original_term character varying(1000),
  episode_of_care_id bigint,
  service_provider_organization_id bigint,
  date_recorded datetime,
  location_id bigint,
  finished bit NOT NULL,
  CONSTRAINT pk_encounter_event_id PRIMARY KEY  (organization_id,person_id,id)
)
GO

CREATE UNIQUE INDEX encounter_event_id ON encounter_event(id)
GO

CREATE INDEX encounter_event_patient_id ON encounter_event(patient_id)
GO

CREATE INDEX fki_encounter_event_appointment_id ON encounter_event(appointment_id)
GO

CREATE INDEX fki_encounter_event_patient_id_organization_id ON encounter_event(patient_id, organization_id)
GO

CREATE INDEX encounter_event_snomed_concept_id_clinical_effective_date ON encounter_event(snomed_concept_id, clinical_effective_date)
GO




CREATE TABLE encounter_detail (
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  practitioner_id bigint,
  episode_of_care_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  recording_practitioner_id bigint,
  recording_date date,
  appointment_id bigint,
  service_provider_organization_id bigint,
  location_id bigint,
  end_date date,
  duration_minutes int,
  completion_status_concept_id bigint,
  healthcare_service_type_concept_id bigint,
  interaction_mode_concept_id bigint,
  administrative_action_concept_id bigint,
  purpose_concept_id bigint,
  disposition_concept_id bigint,
  site_of_care_type_concept_id bigint,
  patient_status_concept_id bigint,
  CONSTRAINT pk_encounter_detail_id PRIMARY KEY (organization_id, person_id, id)
)
GO

CREATE UNIQUE INDEX ix_encounter_detail_id ON encounter_detail(id)
GO

CREATE INDEX ix_encounter_detail_patient_id ON encounter_detail(patient_id)
GO

CREATE INDEX ix_encounter_detail_appointment_id ON encounter_detail(appointment_id)
GO

CREATE INDEX ix_encounter_detail_patient_id_organization_id ON encounter_detail(patient_id, organization_id)
GO

CREATE TABLE encounter_raw (
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  practitioner_id bigint,
  episode_of_care_id bigint,
  clinical_effective_date date,
  date_precision_id smallint,
  recording_practitioner_id bigint,
  recording_date date,
  appointment_id bigint,
  service_provider_organization_id bigint,
  location_id bigint,
  end_date date,
  duration_minutes int,
  fhir_adt_message_code varchar(50),
  fhir_class varchar(50),
  fhir_type varchar(50),
  fhir_status varchar(50),
  fhir_snomed_concept_id bigint,
  fhir_original_code character varying(100),
  fhir_original_term character varying(1000),
  CONSTRAINT pk_encounter_raw_id PRIMARY KEY (organization_id, person_id, id)
)
GO

CREATE UNIQUE INDEX ix_raw_detail_id ON encounter_raw(id)
GO

CREATE INDEX ix_encounter_raw_patient_id ON encounter_raw(patient_id)
GO

CREATE INDEX ix_encounter_raw_appointment_id ON encounter_raw(appointment_id)
GO

CREATE INDEX ix_encounter_raw_patient_id_organization_id ON encounter_raw(patient_id, organization_id)
GO

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
  snomed_concept_id bigint,
  original_code character varying(100) collate Latin1_General_100_BIN2,
  original_term character varying(1000),
  is_review bit NOT NULL,
  date_recorded datetime,
  CONSTRAINT pk_allergy_intolerance_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX allergy_intolerance_id ON allergy_intolerance(id)
GO

CREATE INDEX allergy_intolerance_patient_id ON allergy_intolerance(patient_id)
GO

CREATE INDEX allergy_intolerance_snomed_concept_id ON allergy_intolerance(snomed_concept_id)
GO

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
  dmd_id bigint,
  is_active bit NULL,
  cancellation_date date,
  dose character varying(1000),
  quantity_value double precision,
  quantity_unit character varying(255),
  medication_statement_authorisation_type_id smallint NOT NULL,
  original_term character varying(1000),
  CONSTRAINT pk_medication_statement_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX medication_statement_id ON medication_statement(id)
GO

CREATE INDEX medication_statement_patient_id ON medication_statement(patient_id)
GO

CREATE INDEX medication_statement_dmd_id ON medication_statement (patient_id)
GO

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
  dmd_id bigint,
  dose character varying(1000),
  quantity_value double precision,
  quantity_unit character varying(255),
  duration_days integer NULL,
  estimated_cost double precision,
  medication_statement_id bigint,
  original_term character varying(1000),
  CONSTRAINT pk_medication_order_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX medication_order_id ON medication_order(id)
GO

CREATE INDEX medication_order_patient_id ON medication_order(patient_id)
GO

CREATE INDEX medication_order_dmd_id ON medication_order(dmd_id)
GO

CREATE TABLE flag
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  effective_date date,
  date_precision_id smallint,
  is_active bit NOT NULL,
  flag_text varchar(max),
  CONSTRAINT pk_flag_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX flag_id ON flag(id)
GO

CREATE INDEX flag_patient_id ON flag(patient_id)
GO

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
  snomed_concept_id bigint,
  result_value double precision,
  result_value_units character varying(50),
  result_date date,
  result_text varchar(max),
  result_concept_id bigint,
  original_code character varying(100) collate Latin1_General_100_BIN2,
  is_problem bit NOT NULL,
  original_term character varying(1000),
  is_review bit NOT NULL,
  problem_end_date date,
  parent_observation_id bigint,
  date_recorded datetime,
  CONSTRAINT pk_observation_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX observation_id ON observation(id)
GO

CREATE INDEX observation_patient_id ON observation(patient_id)
GO

CREATE INDEX observation_snomed_concept_id ON observation(snomed_concept_id)
GO

CREATE INDEX observation_snomed_concept_id_is_problem ON observation([snomed_concept_id],[is_problem])
GO

CREATE INDEX observation_snomed_concept_id_value ON observation([snomed_concept_id],[result_value])
GO

CREATE INDEX observation_original_code ON observation(original_code)
GO

CREATE INDEX ix_observation_organization_id ON observation(organization_id)
GO

CREATE INDEX ix_observation_clinical_effective_date ON observation (clinical_effective_date)
GO

CREATE INDEX ix_observation_person_id ON observation(person_id)
GO

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
  snomed_concept_id bigint,
  procedure_request_status_id smallint,
  original_code character varying(100) collate Latin1_General_100_BIN2,
  original_term character varying(1000),
  date_recorded datetime,
  CONSTRAINT pk_procedure_request_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX procedure_request_id ON procedure_request(id)
GO

CREATE INDEX procedure_request_patient_id ON procedure_request(patient_id)
GO

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
  snomed_concept_id bigint,
  requester_organization_id bigint,
  recipient_organization_id bigint,
  priority_id smallint,
  type_id smallint,
  mode character varying(50),
  outgoing_referral bit,
  original_code character varying(100) collate Latin1_General_100_BIN2,
  original_term character varying(1000),
  is_review bit NOT NULL,
  specialty varchar(50),
  ubrn varchar(50),
  date_recorded datetime,
  CONSTRAINT pk_referral_request_id PRIMARY KEY ([organization_id],[person_id],[id])
)
GO

CREATE UNIQUE INDEX referral_request_id ON referral_request(id)
GO

CREATE INDEX referral_request_patient_id ON referral_request(patient_id)
GO

CREATE INDEX referral_request_snomed_concept_id ON referral_request(snomed_concept_id)
GO

CREATE TABLE link_distributor
(
  source_skid character varying(255) NOT NULL,
  target_salt_key_name varchar(50) NOT NULL,
  target_skid character varying(255) NULL,
  CONSTRAINT pk_link_distributor PRIMARY KEY ([source_skid], [target_salt_key_name])
)
GO

CREATE INDEX link_distributor_target_skid ON link_distributor(target_skid)
GO

create table patient_uprn (
	patient_id bigint,
    organization_id bigint,
    person_id bigint,
    lsoa_code varchar(50),
    pseudo_uprn varchar(255),
    qualifier varchar(50),
    [algorithm] varchar(255),
    [match] varchar(255),
    no_address bit,
    invalid_address bit,
    missing_postcode bit,
    invalid_postcode bit,
	property_class varchar(10),
    CONSTRAINT pk_patient_uprn_id_organization_id PRIMARY KEY ([organization_id],[person_id],[patient_id])
)
GO

CREATE UNIQUE INDEX patient_uprn_id ON patient_uprn(patient_id)
GO

create index ix_patient_uprn_patient_org_uprn on patient_uprn (patient_id, organization_id, pseudo_uprn)
GO

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
  use_concept_id          int	NOT NULL ,
  start_date date,
  end_date				   date,
  lsoa_2001_code           varchar(9),
  lsoa_2011_code           varchar(9),
  msoa_2001_code           varchar(9),
  msoa_2011_code           varchar(9),
  ward_code                varchar(9),
  local_authority_code     varchar(9),
  CONSTRAINT pk_organization_id_id_patient_id_person_id PRIMARY KEY ([organization_id],[id],[patient_id],[person_id])
)
GO

create unique index ux_patient_address_id on patient_address (id)
GO


CREATE TABLE patient_contact
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint,
  use_concept_id int ,
  type_concept_id int ,
  start_date date,
  end_date date,
  value varchar(255) ,
  CONSTRAINT pk_patient_contact PRIMARY KEY ([organization_id],[id],[patient_id],[person_id])
);

create unique index ux_patient_contact_id on patient_contact (id);
GO


CREATE TABLE [registration_status_history] (
    [id] bigint NOT NULL,
    [organization_id] bigint NOT NULL,
    [patient_id] bigint NOT NULL,
    [person_id] bigint NOT NULL,
    [episode_of_care_id] bigint DEFAULT NULL,
    [registration_status_id] int DEFAULT NULL,
    [start_date] datetime DEFAULT NULL,
    [end_date] datetime DEFAULT NULL,
    PRIMARY KEY ([organization_id],[id],[patient_id],[person_id])
);

CREATE UNIQUE INDEX [ux_registration_status_history_id] ON [registration_status_history] ([id]);


CREATE PROCEDURE update_person_record_2(@_new_person_id bigint)
AS
BEGIN
SET NOCOUNT ON;

		DECLARE @best_patient_id bigint;

		SET @best_patient_id = (
		SELECT id
        FROM
		(SELECT TOP 1
			p.id as [id],
			CASE WHEN  rt.code = 'R' THEN  1 ELSE  0 END as [registration_type_rank], -- if reg type = GMS then up-rank
			CASE WHEN  e.registration_status_id is null or rs.code not in ('PR1', 'PR2', 'PR3') THEN  1 ELSE  0 END as [registration_status_rank], -- if pre-registered status, then down-rank
			CASE WHEN  p.date_of_death is not null THEN  1 ELSE  0 END as [death_rank], --  records is a date of death more likely to be actively used, so up-vote
			CASE WHEN  e.date_registered_end is null THEN  '9999-12-31' ELSE  e.date_registered_end END as [date_registered_end_sortable] -- up-vote non-ended ones
		FROM patient p
		LEFT OUTER JOIN episode_of_care e
			ON e.organization_id = p.organization_id
			AND e.patient_id = p.id
		LEFT OUTER JOIN registration_type rt
			ON rt.id = e.registration_type_id
		LEFT OUTER JOIN registration_status rs
			ON rs.id = e.registration_status_id
		WHERE
			p.person_id = @_new_person_id
		ORDER BY
			registration_status_rank desc, -- avoid pre-registered records if possible
			death_rank desc, -- records marked as deceased are more likely to be used than ones not
			registration_type_rank desc, -- prefer GMS registrations over others
			date_registered desc, -- want the most recent registration
			date_registered_end_sortable desc
		) as [tmp]
		);

		MERGE person e
		USING (
			SELECT person_id, patient_gender_id, pseudo_id, age_years, age_months, age_weeks, date_of_death, postcode_prefix, lsoa_code, msoa_code, ethnic_code, ward_code, local_authority_code, registered_practice_organization_id, title, first_names, last_names
			FROM patient
			WHERE id = @best_patient_id
		) as a
		ON (a.person_id = e.id)
		WHEN MATCHED
			THEN UPDATE SET
				-- e.id = a.person_id,
				e.patient_gender_id = a.patient_gender_id,
				e.pseudo_id = a.pseudo_id,
				e.age_years = a.age_years,
				e.age_months = a.age_months,
				e.age_weeks = a.age_weeks,
				e.date_of_death = a.date_of_death,
				e.postcode_prefix = a.postcode_prefix,
				e.lsoa_code = a.lsoa_code,
				e.msoa_code = a.msoa_code,
				e.ethnic_code = a.ethnic_code,
				e.ward_code = a.ward_code,
				e.local_authority_code = a.local_authority_code,
				e.registered_practice_organization_id = a.registered_practice_organization_id,
				e.title = a.title,
				e.first_names = a.first_names,
				e.last_names = a.last_names
		WHEN NOT MATCHED BY TARGET
			THEN INSERT (id, patient_gender_id, pseudo_id, age_years, age_months, age_weeks, date_of_death, postcode_prefix, lsoa_code, msoa_code, ethnic_code, ward_code, local_authority_code, registered_practice_organization_id, title, first_names, last_names)
			VALUES (a.person_id, a.patient_gender_id, a.pseudo_id, a.age_years, a.age_months, a.age_weeks, a.date_of_death, a.postcode_prefix, a.lsoa_code, a.msoa_code, a.ethnic_code, a.ward_code, a.local_authority_code, a.registered_practice_organization_id, a.title, a.first_names, a.last_names);

END
GO

CREATE PROCEDURE update_person_record(@_new_person_id bigint, @_old_person_id bigint)
AS
BEGIN
SET NOCOUNT ON;

	DECLARE @_best_patient_id bigint = -1;
    DECLARE @_patients_remaning INT = 1;

	IF (@_new_person_id IS NOT NULL) BEGIN
		EXEC update_person_record_2 @_new_person_id;
	END 

    IF (@_old_person_id IS NOT NULL) BEGIN

	 select @_patients_remaning = COUNT(1) from patient where person_id = @_old_person_id;

        IF (@_patients_remaning = 0) BEGIN
			DELETE FROM person
            WHERE id = @_old_person_id;
        END
        ELSE BEGIN
			EXEC update_person_record_2 @_old_person_id;
        END 

    END 
END 
GO

CREATE TRIGGER [after_patient_insert]
ON [patient]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
	DECLARE @new_person_id BIGINT;
	SET @new_person_id = (SELECT person_id FROM inserted);
	EXEC update_person_record @new_person_id, null;

  END
GO

ALTER TABLE [patient] ENABLE TRIGGER [after_patient_insert]
GO

CREATE TRIGGER [after_patient_update]
ON [patient]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
	DECLARE @new_person_id BIGINT;
	SET @new_person_id = (SELECT person_id FROM inserted);
	DECLARE @old_person_id BIGINT;
	SET @old_person_id = (SELECT person_id FROM deleted);
	EXEC update_person_record @new_person_id, @old_person_id;

  END
GO

ALTER TABLE [patient] ENABLE TRIGGER [after_patient_update]
GO

CREATE TRIGGER [after_patient_delete]
ON [patient]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
	DECLARE @old_person_id BIGINT;
	SET @old_person_id = (SELECT person_id FROM deleted);
	EXEC update_person_record null, @old_person_id;

  END
GO

ALTER TABLE [patient] ENABLE TRIGGER [after_patient_delete]
GO