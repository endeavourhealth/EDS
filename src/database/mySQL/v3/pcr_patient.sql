
drop database if exists pcr_patient;
create database pcr_patient;

use pcr_patient;

DROP TABLE IF EXISTS event_log;
DROP TABLE IF EXISTS allergy_intolerance;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS diagnostic_order;
DROP TABLE IF EXISTS encounter;
DROP TABLE IF EXISTS encounter_status;
DROP TABLE IF EXISTS encounter_location;
DROP TABLE IF EXISTS episode_of_care;
DROP TABLE IF EXISTS gp_registration;
DROP TABLE IF EXISTS gp_registration_status;
DROP TABLE IF EXISTS flag;
DROP TABLE IF EXISTS medication_order;
DROP TABLE IF EXISTS medication_statement;
DROP TABLE IF EXISTS observation;
DROP TABLE IF EXISTS observation_result;
DROP TABLE IF EXISTS observation_immunisation;
DROP TABLE IF EXISTS observation_procedure;
DROP TABLE IF EXISTS observation_family_history;
DROP TABLE IF EXISTS observation_condition;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS patient_name;
DROP TABLE IF EXISTS patient_address;
DROP TABLE IF EXISTS patient_telecom;
DROP TABLE IF EXISTS patient_relationship;
DROP TABLE IF EXISTS patient_relationship_telecom;
DROP TABLE IF EXISTS procedure_request;
DROP TABLE IF EXISTS questionnaire_response;
DROP TABLE IF EXISTS questionnaire_response_question;
DROP TABLE IF EXISTS referral_request;
DROP TABLE IF EXISTS patient_person_link;

create table event_log (
	id int NOT NULL,
	table_id tinyint NOT NULL,
    record_id int NOT NULL,
    event_timestamp datetime(3) NOT NULL COMMENT 'datetime 3 gives us precision down to the millisecond',
    event_log_transaction_type_id tinyint NOT NULL COMMENT '0 = insert, 1 = update, 2 = delete',
    batch_id char(36) COMMENT 'UUID referring to the audit.exchange_batch table',
    service_id char(36) COMMENT 'UUID referring to the admin.service table',
    patient_id int COMMENT 'duplication of patient_if (if present) on audited table',
    concept_id int COMMENT 'duplication of main concept ID (if present) on audited table',
	CONSTRAINT pk_event_log PRIMARY KEY (id)
);

ALTER TABLE event_log MODIFY COLUMN id INT auto_increment;


-- table for each FHIR resource
create table allergy_intolerance (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    concept_id int COMMENT 'IM concept ID',
    is_confidential boolean,
    encounter_id bigint,
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores last_occurance, severity, certainty, category, status, freetext/note',
	CONSTRAINT pk_allergy_intolerance PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 1';

CREATE UNIQUE INDEX uix_id ON allergy_intolerance (id);



create table appointment (
	id int NOT NULL,
    patient_id int NOT NULL,
    practitioner_id int,
    booking_date datetime,
    start_date datetime,
	end_date datetime,
    minutes_duration smallint,
    appointment_schedule_id int COMMENT 'refers to appointment_schedule table',
    status_concept_id int COMMENT 'IM concept representing the appt status (e.g. booked, finished, DNA)',
    type_concept_id int COMMENT 'IM concept representing the type (e.g. telephone appt)',
    additional_data JSON COMMENT 'stores freetext/comments, patient wait, patient delay, sent in datetime, left datetime, cancelled datetime, dna_reason_concept_id',
	CONSTRAINT pk_date_precision PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 2';

CREATE UNIQUE INDEX uix_id ON appointment (id);


create table diagnostic_order (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    concept_id int COMMENT 'IM concept ID',
    is_confidential boolean,
    encounter_id bigint,
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores freetext/comments',
	CONSTRAINT pk_diagnostic_order PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 3';

CREATE UNIQUE INDEX uix_id ON diagnostic_order (id);


create table encounter (
	id bigint NOT NULL,
    patient_id int NOT NULL,
	recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
	effective_end_date datetime COMMENT 'end date is always exact, so no precision col required',
    practitioner_id int,
    entered_date datetime,
    is_confidential boolean,
    current_status_concept_id int COMMENT 'IM concept for the status (e.g. completed, planned)',
    current_location_id int COMMENT 'refers to location table',
    episode_of_care_id int COMMENT 'refers to episode_of_care table',
    appointment_id int COMMENT 'refers to appointment table',
    service_provider_organisation_id int COMMENT 'refers to organisation table',
    class_concept_id int COMMENT 'IM concept for the encounter class',
    additional_data JSON COMMENT 'stores freetext/comments, incomplete flag, specialty, treatment function, location type, type, reason',
	CONSTRAINT pk_encounter PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 4';

CREATE UNIQUE INDEX uix_id ON encounter (id);

create table encounter_status (
	id bigint NOT NULL,
    patient_id int NOT NULL,
	encounter_id bigint NOT NULL,
    status_concept_id int COMMENT 'IM concept for the encounter status',
    status_date datetime COMMENT 'datetime the status came into effect',
    additional_data JSON COMMENT '',
	CONSTRAINT pk_encounter_status PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 5';

CREATE UNIQUE INDEX uix_id ON encounter_status (id);


create table encounter_location (
	id bigint NOT NULL,
    patient_id int NOT NULL,
	encounter_id bigint NOT NULL,
    location_id int COMMENT 'refers to location table',
    location_date datetime COMMENT 'datetime the location came into effect',
    additional_data JSON COMMENT '',
	CONSTRAINT pk_encounter_location PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 6';

CREATE UNIQUE INDEX uix_id ON encounter_location (id);



create table episode_of_care (
	id int NOT NULL,
    patient_id int NOT NULL,
    managing_organisation_id int COMMENT 'refers to organisation table',
    care_provider_practitioner_id int COMMENT 'refers to practitioner table',
    start_date datetime,
    end_date datetime,
    additional_data JSON COMMENT 'stores outcome, priority, PCC arrival time, ',
	CONSTRAINT pk_episode_of_care PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 7';

CREATE UNIQUE INDEX uix_id ON episode_of_care (id);




create table gp_registration (
	id int NOT NULL,
    patient_id int NOT NULL,
    managing_organisation_id int COMMENT 'refers to organisation table',
    usual_gp_practitioner_id int COMMENT 'refers to practitioner table',
    start_date date,
    end_date date,
    registration_type_concept_id int COMMENT 'IM concept for GP registration type',
    additional_data JSON COMMENT 'stores outcome, priority, PCC arrival time, residential insitute code',
	CONSTRAINT pk_gp_registration PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 8';

CREATE UNIQUE INDEX uix_id ON gp_registration (id);



create table gp_registration_status (
	id int NOT NULL,
    patient_id int NOT NULL,
    gp_registration_id int NOT NULL,
    registration_status_concept_id int COMMENT 'IM concept for registration status',
    status_date date,
    additional_data JSON COMMENT 'stores outcome, priority, PCC arrival time, ',
	CONSTRAINT pk_gp_registration_status PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 9';

CREATE UNIQUE INDEX uix_id ON gp_registration_status (id);



create table flag (
	id int NOT NULL,
    patient_id int NOT NULL,
    category_concept_id int COMMENT 'IM concept for flag type (e.g. clinical)',
    value_concept_id int COMMENT 'IM concept for the flag (e.g. on CPP)',
    recording_practitioner_id int,
    recording_date datetime,
    effective_start_date datetime,
    effective_end_date datetime,
    additional_data JSON COMMENT 'stores freetext/comments',
	CONSTRAINT pk_flag PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 10';

CREATE UNIQUE INDEX uix_id ON flag (id);





create table medication_order (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    is_confidential boolean,
    encounter_id bigint,
    medication_statement_id int COMMENT 'refers to medication_statement table',
    concept_id int COMMENT 'IM concept ID',
    dose varchar(255),
    quantity_value decimal(5, 3),
    quantity_unit_concept_id int COMMENT 'IM concept for units e.g. tablets',
    supply_duration_days int COMMENT 'intended length of the medication',
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores reason, cost',
	CONSTRAINT pk_medication_order PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 13';

CREATE UNIQUE INDEX uix_id ON medication_order (id);


create table medication_statement (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    is_confidential boolean,
    encounter_id bigint,
    status_concept_id int COMMENT 'IM concept for status e.g. active, completed',
    concept_id int COMMENT 'IM concept ID',
    dose varchar(255),
    quantity_value decimal(5, 3),
    quantity_unit_concept_id int COMMENT 'IM concept for units e.g. tablets',
    number_issues_authorised int,
    number_issues_issued int,
    cancellation_date date,
    authorisation_type_concept_id int COMMENT 'IM concept for auth type e.g. acute, repeat',
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores reason',
	CONSTRAINT pk_medication_statement PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 14';

CREATE UNIQUE INDEX uix_id ON medication_statement (id);



create table observation (
	id bigint NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    concept_id int COMMENT 'IM concept ID',
    is_confidential boolean,
    encounter_id bigint,
    parent_observation_id bigint,
    observation_type_concept_id int COMMENT 'IM concept stating if this is a numeric result, procedure etc.',
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores freetext/comments',
	CONSTRAINT pk_observation PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 15, contains data for multiple FHIR resources: observation, condition, procedure, familty history, diagnostic report, imms, specimen';

CREATE UNIQUE INDEX uix_id ON observation (id);



create table observation_result (
	observation_id bigint NOT NULL,
    patient_id int NOT NULL,
    numeric_value decimal(5, 3),
    numeric_unit_concept_id int COMMENT 'IM concept for the units of measure',
    numeric_comparator_concept_id int COMMENT 'IM concept for the comparator e.g. >=, <)',
    numeric_range_low decimal(5, 3),
    numeric_range_high decimal(5, 3),
    result_date datetime,
    -- result_string varchar(255), -- going to state that text results go through IM
    result_concept_id int COMMENT 'IM concept for the result',
    additional_data JSON COMMENT 'stores freetext/comments',
	CONSTRAINT pk_observation_result PRIMARY KEY (patient_id, observation_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 16, provides supplementary information about numeric observations';

CREATE UNIQUE INDEX uix_id ON observation_result (observation_id);


create table observation_immunisation (
	observation_id bigint NOT NULL,
    patient_id int NOT NULL,
    site_concept_id int COMMENT 'IM concept giving the vaccination site e.g. arm',
    route_concept_id int COMMENT 'IM concept giving the vaccination route e.g. intra-muscular',
    additional_data JSON COMMENT 'stores dose quantity, expiration date, batch number, reason',
	CONSTRAINT pk_observation_immunisation PRIMARY KEY (patient_id, observation_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 17, provides supplementary information about immunisation observations';

CREATE UNIQUE INDEX uix_id ON observation_immunisation (observation_id);


create table observation_procedure (
	observation_id bigint NOT NULL,
    patient_id int NOT NULL,
    end_date datetime COMMENT 'when the procedure was finished, if known',
    is_primary boolean COMMENT 'if the primary procedure or not',
    sequence_number int COMMENT 'sequence number in secondary care',
    performed_location_id int COMMENT 'refers to location table',
    additional_data JSON COMMENT '',
	CONSTRAINT pk_observation_procedure PRIMARY KEY (patient_id, observation_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 18, provides supplementary information about procedure observations';

CREATE UNIQUE INDEX uix_id ON observation_procedure (observation_id);


create table observation_family_history (
	observation_id bigint NOT NULL,
    patient_id int NOT NULL,
    relationship_concept_id int COMMENT 'IM concept giving the relationship to the patient',
    additional_data JSON COMMENT 'stored end_date',
	CONSTRAINT pk_observation_family_history PRIMARY KEY (patient_id, observation_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 19, provides supplementary information about family history observations';

CREATE UNIQUE INDEX uix_id ON observation_family_history (observation_id);


create table observation_condition (
	observation_id bigint NOT NULL,
    patient_id int NOT NULL,
    is_primary boolean COMMENT 'if the primary procedure or not',
    sequence_number int COMMENT 'sequence number in secondary care',
    is_problem boolean COMMENT 'if the condition is a problem',
    problem_episodicity_concept_id int COMMENT 'IM concept for the episodicity',
    problem_end_date date,
    problem_significance_concept_id int COMMENT 'IM concept for significance e.g. minor, major)',
    parent_problem_observation_id bigint,
    parent_problem_relationship_concept_id int COMMENT 'IM concept for the relationship to the parent problem e.g. evolved from',
    additional_data JSON COMMENT 'stored expected duration, last review date, last reviewed by, ',
	CONSTRAINT pk_observation_condition PRIMARY KEY (patient_id, observation_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 20, provides supplementary information about family history observations';

CREATE UNIQUE INDEX uix_id ON observation_condition (observation_id);



create table patient (
	id int NOT NULL,
    birth_date date,
    is_deceased boolean COMMENT 'in some cases we only know a patient is deceased, but not the date',
    deceased_date date,
	gender_concept_id int COMMENT 'IM concept for gender',
	is_confidential boolean,
    is_test_patient boolean,
    nhs_number_verification_concept_id INT COMMENT 'IM concept for NHS number status e.g. traced',
    marital_status_concept_id int COMMENT 'IM concept for marital status',
    ethnicity_concept_id int COMMENT 'IM concept for ethnicity',
    religion_concept_id int COMMENT 'IM concept for ethnicity',
    spoken_language_concept_id int COMMENT 'IM concept for main spoken language',
    nhs_number char(10) COMMENT 'patient official NHS number - any other ID or past NHS number goes in patient_idenfier table',
    additional_data JSON COMMENT 'stores time of birth, spine_sensitive, speaks_english',
	CONSTRAINT pk_patient PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 23';

CREATE UNIQUE INDEX uix_id ON patient (id);


create table patient_identifier (
	id int NOT NULL,
    patient_id int NOT NULL,
    type_concept_id int COMMENT 'IM concept for name use e.g. MRN, past NHS numbers',
    identifier_value varchar(255),
    start_date date,
    end_date date,
    additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_identifier PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 37';

CREATE UNIQUE INDEX uix_id ON patient_identifier (id);



create table patient_name (
	id int NOT NULL,
    patient_id int NOT NULL,
    use_concept_id int COMMENT 'IM concept for name use e.g. official, nickname',
    name_prefix varchar(255) COMMENT 'i.e. title',
    given_names varchar(255) COMMENT 'first and middle names',
    family_names varchar(255) COMMENT 'last names',
    name_suffix varchar(255) COMMENT 'honourifics that go after the name',
    start_date date,
    end_date date,
    additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_name PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 24';

CREATE UNIQUE INDEX uix_id ON patient_name (id);


create table patient_address (
	id int NOT NULL,
    patient_id int NOT NULL,
    use_concept_id int COMMENT 'IM concept for name use e.g. home, temporary',
    address_line_1 varchar(255),
    address_line_2 varchar(255),
    address_line_3 varchar(255),
    address_city varchar(255),
    address_county varchar(255),
    address_postcode varchar(255),
    address_mapped_id varchar(255) COMMENT 'derived ID of the "national" record of this address',
    start_date date,
    end_date date,
    additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_address PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 25';

CREATE UNIQUE INDEX uix_id ON patient_address (id);


create table patient_telecom (
	id int NOT NULL,
    patient_id int NOT NULL,
    use_concept_id int COMMENT 'IM concept for name use e.g. home, work, temporary',
    system_concept_id int COMMENT 'IM concept for name system e.g. phone, fax, email',
    telecom_value varchar(255),
    start_date date,
    end_date date,
    additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_telecom PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 26';

CREATE UNIQUE INDEX uix_id ON patient_telecom (id);


create table patient_relationship (
	id int NOT NULL,
    patient_id int NOT NULL,
    is_next_of_kin boolean,
    is_carer boolean,
    relationship_type_concept_id int COMMENT 'IM concept for relationship type',
    birth_date date,
    gender_concept_id int COMMENT 'IM concept for gender',
    name_prefix varchar(255) COMMENT 'i.e. title',
    given_names varchar(255) COMMENT 'first and middle names',
    family_names varchar(255) COMMENT 'last names',
    name_suffix varchar(255) COMMENT 'honourifics that go after the name',
	address_line_1 varchar(255),
    address_line_2 varchar(255),
    address_line_3 varchar(255),
    address_city varchar(255),
    address_county varchar(255),
    address_postcode varchar(255),
    address_mapped_id varchar(255) COMMENT 'derived ID of the "national" record of this address',
    start_date date,
    end_date date,
    additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_relationship PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 27';

CREATE UNIQUE INDEX uix_id ON patient_relationship (id);


create table patient_relationship_telecom (
	id int NOT NULL,
    patient_id int NOT NULL,
	patient_relationship_id int NOT NULL,
    telecom_concept_id int COMMENT 'IM concept for telephone, fax, email etc.',
    telecom_value varchar(255),
    additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_relationship_telecom PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 28';

CREATE UNIQUE INDEX uix_id ON patient_relationship_telecom (id);


create table procedure_request (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    concept_id int COMMENT 'IM concept ID',
    is_confidential boolean,
    encounter_id bigint,
    status_concept_id int COMMENT 'IM concept for the status e.g. completed, requested',
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores location type, notes, schedule text, schedule date',
	CONSTRAINT pk_procedure_request PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 30';

CREATE UNIQUE INDEX uix_id ON procedure_request (id);


create table questionnaire_response (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    effective_practitioner_id int,
    effective_date datetime,
    effective_date_precision_id tinyint COMMENT 'refers to date_precision table',
    encounter_id bigint,
    status_concept_id int COMMENT 'IM concept for the status e.g. completed, requested',
    is_confidential boolean,
    additional_data JSON COMMENT 'stores location type, notes, schedule text, schedule date',
	CONSTRAINT pk_questionnaire_response PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 31';

CREATE UNIQUE INDEX uix_id ON questionnaire_response (id);


create table questionnaire_response_question (
	id int NOT NULL,
    patient_id int NOT NULL,
    questionnaire_response_id int NOT NULL COMMENT 'refers to owning questionnaire_response',
	question_set varchar(255),
    question varchar(255),
    answer mediumtext,
    ordinal tinyint,
	CONSTRAINT pk_questionnaire_response_question PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 32';

CREATE UNIQUE INDEX uix_id ON questionnaire_response_question (id);



create table referral_request (
	id int NOT NULL,
    patient_id int NOT NULL,
    recording_practitioner_id int,
    recording_date datetime,
    sending_practitioner_id int,
    sending_organisation_id int,
    sending_date datetime,
    sending_date_precision_id tinyint COMMENT 'refers to date_precision table',
    recipient_practitioner_id int,
    recipient_organisation_id int,
    concept_id int COMMENT 'IM concept ID',
    is_confidential boolean,
    encounter_id bigint,
    status_concept_id int COMMENT 'IM concept for status e.g. draft, sent',
    priority_concept_id int COMMENT 'IM concept for priority e.g. routine, urgent',
    type_concept_id int COMMENT 'IM concept for type e.g. assessment, investigation, treatment',
    mode_concept_id int COMMENT 'IM concept for send mode e.g. written, ERS',
    problem_observation_id bigint COMMENT 'links to observation problem record',
    additional_data JSON COMMENT 'stores description, UBRN, recipient service type',
	CONSTRAINT pk_referral_request PRIMARY KEY (patient_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 33';

CREATE UNIQUE INDEX uix_id ON referral_request (id);


CREATE TABLE patient_person_link
(
  patient_id int NOT NULL,
  service_id character(36) NOT NULL COMMENT 'refers to admin.service table',
  person_id int NOT NULL COMMENT 'refers to eds.patient_link table',
  CONSTRAINT pk_patient_person_link PRIMARY KEY (patient_id)
) COMMENT 'table linking individual patient records into singular person records';

CREATE INDEX ix_person_id ON patient_person_link (person_id);
