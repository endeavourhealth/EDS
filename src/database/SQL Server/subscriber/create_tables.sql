CREATE DATABASE subscriber_pi
GO

USE subscriber_pi
GO

IF OBJECT_ID('dbo.allergy_intolerance', 'U') IS NOT NULL DROP TABLE dbo.allergy_intolerance
GO
IF OBJECT_ID('dbo.appointment', 'U') IS NOT NULL DROP TABLE dbo.appointment
GO
IF OBJECT_ID('dbo.diagnostic_order', 'U') IS NOT NULL DROP TABLE dbo.diagnostic_order
GO
IF OBJECT_ID('dbo.encounter', 'U') IS NOT NULL DROP TABLE dbo.encounter
GO
IF OBJECT_ID('dbo.episode_of_care', 'U') IS NOT NULL DROP TABLE dbo.episode_of_care
GO
IF OBJECT_ID('dbo.event_log', 'U') IS NOT NULL DROP TABLE dbo.event_log
GO
IF OBJECT_ID('dbo.flag', 'U') IS NOT NULL DROP TABLE dbo.flag
GO
IF OBJECT_ID('dbo.location', 'U') IS NOT NULL DROP TABLE dbo.location
GO
IF OBJECT_ID('dbo.medication_order', 'U') IS NOT NULL DROP TABLE dbo.medication_order
GO
IF OBJECT_ID('dbo.medication_statement', 'U') IS NOT NULL DROP TABLE dbo.medication_statement
GO
IF OBJECT_ID('dbo.observation', 'U') IS NOT NULL DROP TABLE dbo.observation
GO
IF OBJECT_ID('dbo.organization', 'U') IS NOT NULL DROP TABLE dbo.organization
GO
IF OBJECT_ID('dbo.patient', 'U') IS NOT NULL DROP TABLE dbo.patient
GO
IF OBJECT_ID('dbo.patient_address', 'U') IS NOT NULL DROP TABLE dbo.patient_address
GO
IF OBJECT_ID('dbo.patient_contact', 'U') IS NOT NULL DROP TABLE dbo.patient_contact
GO
IF OBJECT_ID('dbo.patient_uprn', 'U') IS NOT NULL DROP TABLE dbo.patient_uprn
GO
IF OBJECT_ID('dbo.person', 'U') IS NOT NULL DROP TABLE dbo.person
GO
IF OBJECT_ID('dbo.practitioner', 'U') IS NOT NULL DROP TABLE dbo.practitioner
GO
IF OBJECT_ID('dbo.procedure_request', 'U') IS NOT NULL DROP TABLE dbo.procedure_request
GO
IF OBJECT_ID('dbo.pseudo_id', 'U') IS NOT NULL DROP TABLE dbo.pseudo_id
GO
IF OBJECT_ID('dbo.referral_request', 'U') IS NOT NULL DROP TABLE dbo.referral_request
GO
IF OBJECT_ID('dbo.schedule', 'U') IS NOT NULL DROP TABLE dbo.schedule
GO

CREATE TABLE [allergy_intolerance] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[is_review] tinyint NOT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO

CREATE UNIQUE INDEX [allergy_intolerance_id] ON [allergy_intolerance] ([id])
GO
CREATE INDEX [fk_allergy_intolerance_encounter_id] ON [allergy_intolerance] ([encounter_id])
GO
CREATE INDEX [fk_allergy_intolerance_patient_id_organization_id] ON [allergy_intolerance] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_allergy_intolerance_practitioner_id] ON [allergy_intolerance] ([practitioner_id])
GO
CREATE INDEX [allergy_intolerance_patient_id] ON [allergy_intolerance] ([patient_id])
GO
CREATE INDEX [allergy_intolerance_core_concept_id] ON [allergy_intolerance] ([core_concept_id])
GO
DBCC CHECKIDENT (N'[allergy_intolerance]', RESEED, 0)
GO

CREATE TABLE [appointment] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[schedule_id] bigint NULL DEFAULT NULL,
[start_date] datetime2(0) NULL DEFAULT NULL,
[planned_duration] int NOT NULL,
[actual_duration] int NULL DEFAULT NULL,
[appointment_status_concept_id] int NULL DEFAULT NULL,
[patient_wait] int NULL DEFAULT NULL,
[patient_delay] int NULL DEFAULT NULL,
[date_time_sent_in] datetime2(0) NULL DEFAULT NULL,
[date_time_left] datetime2(0) NULL DEFAULT NULL,
[source_id] varchar(36) NULL DEFAULT NULL,
[cancelled_date] datetime2(0) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [appointment_id] ON [appointment] ([id])
GO
CREATE INDEX [fk_appointment_practitioner_id] ON [appointment] ([practitioner_id])
GO
CREATE INDEX [appointment_patient_id] ON [appointment] ([patient_id])
GO
DBCC CHECKIDENT (N'[appointment]', RESEED, 0)
GO

CREATE TABLE [diagnostic_order] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[result_value] float NULL DEFAULT NULL,
[result_value_units] varchar(50) NULL DEFAULT NULL,
[result_date] date NULL DEFAULT NULL,
[result_text] varchar(MAX) NULL DEFAULT NULL,
[result_concept_id] int NULL DEFAULT NULL,
[is_problem] tinyint NOT NULL,
[is_review] tinyint NOT NULL,
[problem_end_date] date NULL DEFAULT NULL,
[parent_observation_id] bigint NULL DEFAULT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
[episodicity_concept_id] int NULL DEFAULT NULL,
[is_primary] tinyint NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [diagnostic_order_id] ON [diagnostic_order] ([id])
GO
CREATE INDEX [fk_diagnostic_order_encounter_id] ON [diagnostic_order] ([encounter_id])
GO
CREATE INDEX [fk_diagnostic_order_patient_id_organization_id] ON [diagnostic_order] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_diagnostic_order_practitioner_id] ON [diagnostic_order] ([practitioner_id])
GO
CREATE INDEX [diagnostic_order_patient_id] ON [diagnostic_order] ([patient_id])
GO
CREATE INDEX [diagnostic_order_core_concept_id] ON [diagnostic_order] ([core_concept_id])
GO
CREATE INDEX [diagnostic_order_core_concept_id_is_problem] ON [diagnostic_order] ([core_concept_id], [is_problem])
GO
CREATE INDEX [diagnostic_order_core_concept_id_result_value] ON [diagnostic_order] ([core_concept_id], [result_value])
GO
CREATE INDEX [diagnostic_order_non_core_concept_id] ON [diagnostic_order] ([non_core_concept_id])
GO
CREATE INDEX [ix_diagnostic_order_organization_id] ON [diagnostic_order] ([organization_id])
GO
CREATE INDEX [ix_diagnostic_order_clinical_effective_date] ON [diagnostic_order] ([clinical_effective_date])
GO
CREATE INDEX [ix_diagnostic_order_person_id] ON [diagnostic_order] ([person_id])
GO
DBCC CHECKIDENT (N'[diagnostic_order]', RESEED, 0)
GO

CREATE TABLE [encounter] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[appointment_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[episode_of_care_id] bigint NULL DEFAULT NULL,
[service_provider_organization_id] bigint NULL DEFAULT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
[type] varchar(MAX) NULL DEFAULT NULL,
[sub_type] varchar(MAX) NULL DEFAULT NULL,
[admission_method] varchar(40) NULL DEFAULT NULL,
[end_date] date NULL DEFAULT NULL,
[institution_location_id] varchar(MAX) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [encounter_id] ON [encounter] ([id])
GO
CREATE INDEX [fk_encounter_practitioner_id] ON [encounter] ([practitioner_id])
GO
CREATE INDEX [fk_encounter_episode_of_care_id] ON [encounter] ([episode_of_care_id])
GO
CREATE INDEX [fk_encounter_service_provider_organization_id] ON [encounter] ([service_provider_organization_id])
GO
CREATE INDEX [encounter_patient_id] ON [encounter] ([patient_id])
GO
CREATE INDEX [fki_encounter_appointment_id] ON [encounter] ([appointment_id])
GO
CREATE INDEX [fki_encounter_patient_id_organization_id] ON [encounter] ([patient_id], [organization_id])
GO
CREATE INDEX [encounter_core_concept_id_clinical_effective_date] ON [encounter] ([core_concept_id], [clinical_effective_date])
GO
DBCC CHECKIDENT (N'[encounter]', RESEED, 0)
GO

CREATE TABLE [episode_of_care] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[registration_type_concept_id] int NULL DEFAULT NULL,
[registration_status_concept_id] int NULL DEFAULT NULL,
[date_registered] date NULL DEFAULT NULL,
[date_registered_end] date NULL DEFAULT NULL,
[usual_gp_practitioner_id] bigint NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [episode_of_care_id] ON [episode_of_care] ([id])
GO
CREATE INDEX [fk_episode_of_care_patient_id_organisation_id] ON [episode_of_care] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_episode_of_care_practitioner_id] ON [episode_of_care] ([usual_gp_practitioner_id])
GO
CREATE INDEX [episode_of_care_patient_id] ON [episode_of_care] ([patient_id])
GO
CREATE INDEX [episode_of_care_registration_type_concept_id] ON [episode_of_care] ([registration_type_concept_id])
GO
CREATE INDEX [episode_of_care_date_registered] ON [episode_of_care] ([date_registered])
GO
CREATE INDEX [episode_of_care_date_registered_end] ON [episode_of_care] ([date_registered_end])
GO
CREATE INDEX [episode_of_care_person_id] ON [episode_of_care] ([person_id])
GO
CREATE INDEX [episode_of_care_organization_id] ON [episode_of_care] ([organization_id])
GO
DBCC CHECKIDENT (N'[episode_of_care]', RESEED, 0)
GO

CREATE TABLE [event_log] (
[dt_change] datetime2(3) NOT NULL,
[change_type] tinyint NOT NULL,
[table_id] tinyint NOT NULL,
[record_id] bigint NOT NULL
)
GO

CREATE TABLE [flag] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[is_active] tinyint NOT NULL,
[flag_text] varchar(MAX) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [flag_id] ON [flag] ([id])
GO
CREATE INDEX [fk_flag_patient_id_organization_id] ON [flag] ([patient_id], [organization_id])
GO
CREATE INDEX [flag_patient_id] ON [flag] ([patient_id])
GO
DBCC CHECKIDENT (N'[flag]', RESEED, 0)
GO

CREATE TABLE [location] (
[id] bigint IDENTITY(1,1),
[name] varchar(255) NULL DEFAULT NULL,
[type_code] varchar(50) NULL DEFAULT NULL,
[type_desc] varchar(255) NULL DEFAULT NULL,
[postcode] varchar(10) NULL DEFAULT NULL,
[managing_organization_id] bigint NULL DEFAULT NULL,
PRIMARY KEY ([id]) 
)
GO
CREATE UNIQUE INDEX [location_id] ON [location] ([id])
GO
CREATE INDEX [fk_location_managing_organisation_id] ON [location] ([managing_organization_id])
GO
DBCC CHECKIDENT (N'[location]', RESEED, 0)
GO

CREATE TABLE [medication_order] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[dose] varchar(1000) NULL DEFAULT NULL,
[quantity_value] float NULL DEFAULT NULL,
[quantity_unit] varchar(255) NULL DEFAULT NULL,
[duration_days] int NULL DEFAULT NULL,
[estimated_cost] float NULL DEFAULT NULL,
[medication_statement_id] bigint NULL DEFAULT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[bnf_reference] varchar(6) NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
[issue_method] varchar(MAX) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [medication_order_id] ON [medication_order] ([id])
GO
CREATE INDEX [fk_medication_order_encounter_id] ON [medication_order] ([encounter_id])
GO
CREATE INDEX [fk_medication_order_patient_id_organization_id] ON [medication_order] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_medication_order_practitioner_id] ON [medication_order] ([practitioner_id])
GO
CREATE INDEX [medication_order_patient_id] ON [medication_order] ([patient_id])
GO
CREATE INDEX [medication_order_core_concept_id] ON [medication_order] ([core_concept_id])
GO
DBCC CHECKIDENT (N'[medication_order]', RESEED, 0)
GO

CREATE TABLE [medication_statement] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[is_active] tinyint NOT NULL,
[cancellation_date] date NULL DEFAULT NULL,
[dose] varchar(1000) NULL DEFAULT NULL,
[quantity_value] float NULL DEFAULT NULL,
[quantity_unit] varchar(255) NULL DEFAULT NULL,
[authorisation_type_concept_id] int NULL DEFAULT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[bnf_reference] varchar(6) NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
[issue_method] varchar(MAX) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [medication_statement_id] ON [medication_statement] ([id])
GO
CREATE INDEX [fk_medication_statement_encounter_id] ON [medication_statement] ([encounter_id])
GO
CREATE INDEX [fk_medication_statement_patient_id_organization_id] ON [medication_statement] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_medication_statement_practitioner_id] ON [medication_statement] ([practitioner_id])
GO
CREATE INDEX [medication_statement_patient_id] ON [medication_statement] ([patient_id])
GO
CREATE INDEX [medication_statement_dmd_id] ON [medication_statement] ([patient_id])
GO
DBCC CHECKIDENT (N'[medication_statement]', RESEED, 0)
GO

CREATE TABLE [observation] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[result_value] float NULL DEFAULT NULL,
[result_value_units] varchar(50) NULL DEFAULT NULL,
[result_date] date NULL DEFAULT NULL,
[result_text] varchar(MAX) NULL DEFAULT NULL,
[result_concept_id] int NULL DEFAULT NULL,
[is_problem] tinyint NOT NULL,
[is_review] tinyint NOT NULL,
[problem_end_date] date NULL DEFAULT NULL,
[parent_observation_id] bigint NULL DEFAULT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
[episodicity_concept_id] int NULL DEFAULT NULL,
[is_primary] tinyint NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [observation_id] ON [observation] ([id])
GO
CREATE INDEX [fk_observation_encounter_id] ON [observation] ([encounter_id])
GO
CREATE INDEX [fk_observation_patient_id_organization_id] ON [observation] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_observation_practitioner_id] ON [observation] ([practitioner_id])
GO
CREATE INDEX [observation_patient_id] ON [observation] ([patient_id])
GO
CREATE INDEX [observation_core_concept_id] ON [observation] ([core_concept_id])
GO
CREATE INDEX [observation_core_concept_id_is_problem] ON [observation] ([core_concept_id], [is_problem])
GO
CREATE INDEX [observation_core_concept_id_result_value] ON [observation] ([core_concept_id], [result_value])
GO
CREATE INDEX [observation_non_core_concept_id] ON [observation] ([non_core_concept_id])
GO
CREATE INDEX [ix_observation_organization_id] ON [observation] ([organization_id])
GO
CREATE INDEX [ix_observation_clinical_effective_date] ON [observation] ([clinical_effective_date])
GO
CREATE INDEX [ix_observation_person_id] ON [observation] ([person_id])
GO
DBCC CHECKIDENT (N'[observation]', RESEED, 0)
GO

CREATE TABLE [organization] (
[id] bigint IDENTITY(1,1),
[ods_code] varchar(50) NULL DEFAULT NULL,
[name] varchar(255) NULL DEFAULT NULL,
[type_code] varchar(50) NULL DEFAULT NULL,
[type_desc] varchar(255) NULL DEFAULT NULL,
[postcode] varchar(10) NULL DEFAULT NULL,
[parent_organization_id] bigint NULL DEFAULT NULL,
PRIMARY KEY ([id]) 
)
GO
CREATE UNIQUE INDEX [organization_id] ON [organization] ([id])
GO
CREATE INDEX [fki_organization_parent_organization_id] ON [organization] ([parent_organization_id])
GO
DBCC CHECKIDENT (N'[organization]', RESEED, 0)
GO

CREATE TABLE [patient] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[title] varchar(50) NULL DEFAULT NULL,
[first_names] varchar(255) NULL DEFAULT NULL,
[last_name] varchar(255) NULL DEFAULT NULL,
[gender_concept_id] int NULL DEFAULT NULL,
[nhs_number] varchar(255) NULL DEFAULT NULL,
[date_of_birth] date NULL DEFAULT NULL,
[date_of_death] date NULL DEFAULT NULL,
[current_address_id] bigint NULL DEFAULT NULL,
[ethnic_code_concept_id] int NULL DEFAULT NULL,
[registered_practice_organization_id] bigint NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [patient_id] ON [patient] ([id])
GO
CREATE INDEX [patient_person_id] ON [patient] ([person_id])
GO
CREATE INDEX [patient_organization_id] ON [patient] ([organization_id])
GO
DBCC CHECKIDENT (N'[patient]', RESEED, 0)
GO

CREATE TABLE [patient_address] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[address_line_1] varchar(255) NULL DEFAULT NULL,
[address_line_2] varchar(255) NULL DEFAULT NULL,
[address_line_3] varchar(255) NULL DEFAULT NULL,
[address_line_4] varchar(255) NULL DEFAULT NULL,
[city] varchar(255) NULL DEFAULT NULL,
[postcode] varchar(10) NULL DEFAULT NULL,
[use_concept_id] int NOT NULL,
[start_date] date NULL DEFAULT NULL,
[end_date] date NULL DEFAULT NULL,
[lsoa_2001_code] varchar(9) NULL DEFAULT NULL,
[lsoa_2011_code] varchar(9) NULL DEFAULT NULL,
[msoa_2001_code] varchar(9) NULL DEFAULT NULL,
[msoa_2011_code] varchar(9) NULL DEFAULT NULL,
[ward_code] varchar(9) NULL DEFAULT NULL,
[local_authority_code] varchar(9) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [id], [patient_id], [person_id]) 
)
GO
CREATE INDEX [fk_patient_address_patient_id_organization_id] ON [patient_address] ([patient_id], [organization_id])
GO
DBCC CHECKIDENT (N'[patient_address]', RESEED, 0)
GO

CREATE TABLE [patient_contact] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[use_concept_id] int NULL DEFAULT NULL,
[type_concept_id] int NULL DEFAULT NULL,
[start_date] date NULL DEFAULT NULL,
[end_date] date NULL DEFAULT NULL,
[value] varchar(255) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [id], [patient_id], [person_id]) 
)
GO
CREATE INDEX [fk_patient_contact_patient_id_organisation_id] ON [patient_contact] ([patient_id], [organization_id])
GO
DBCC CHECKIDENT (N'[patient_contact]', RESEED, 0)

CREATE TABLE [patient_uprn] (
[patient_id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[uprn] bigint NULL DEFAULT NULL,
[qualifier] varchar(50) NULL DEFAULT NULL,
[algorithm] varchar(255) NULL DEFAULT NULL,
[match] varchar(255) NULL DEFAULT NULL,
[no_address] tinyint NULL DEFAULT NULL,
[invalid_address] tinyint NULL DEFAULT NULL,
[missing_postcode] tinyint NULL DEFAULT NULL,
[invalid_postcode] tinyint NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [patient_id]) 
)
GO
CREATE UNIQUE INDEX [patient_uprn_id] ON [patient_uprn] ([patient_id])
GO
CREATE INDEX [fk_patient_uprn_patient_id_organisation_id] ON [patient_uprn] ([patient_id], [organization_id])
GO
DBCC CHECKIDENT (N'[patient_uprn]', RESEED, 0)
GO

CREATE TABLE [person] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[title] varchar(50) NULL DEFAULT NULL,
[first_names] varchar(255) NULL DEFAULT NULL,
[last_name] varchar(255) NULL DEFAULT NULL,
[gender_concept_id] int NULL DEFAULT NULL,
[nhs_number] varchar(255) NULL DEFAULT NULL,
[date_of_birth] date NULL DEFAULT NULL,
[date_of_death] date NULL DEFAULT NULL,
[current_address_id] bigint NULL DEFAULT NULL,
[ethnic_code_concept_id] int NULL DEFAULT NULL,
[registered_practice_organization_id] bigint NULL DEFAULT NULL,
PRIMARY KEY ([id]) 
)
GO
CREATE UNIQUE INDEX [person_id] ON [person] ([id])
GO
DBCC CHECKIDENT (N'[person]', RESEED, 0)
GO

CREATE TABLE [practitioner] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[name] varchar(1024) NULL DEFAULT NULL,
[role_code] varchar(50) NULL DEFAULT NULL,
[role_desc] varchar(255) NULL DEFAULT NULL,
PRIMARY KEY ([id]) 
)
GO
CREATE UNIQUE INDEX [practitioner_id] ON [practitioner] ([id])
GO
CREATE INDEX [fk_practitioner_organisation_id] ON [practitioner] ([organization_id])
GO
DBCC CHECKIDENT (N'[practitioner]', RESEED, 0)
GO

CREATE TABLE [procedure_request] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[status_concept_id] int NULL DEFAULT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [procedure_request_id] ON [procedure_request] ([id])
GO
CREATE INDEX [fk_procedure_request_encounter_id] ON [procedure_request] ([encounter_id])
GO
CREATE INDEX [fk_procedure_request_patient_id_organization_id] ON [procedure_request] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_procedure_request_practitioner_id] ON [procedure_request] ([practitioner_id])
GO
CREATE INDEX [procedure_request_patient_id] ON [procedure_request] ([patient_id])
GO
DBCC CHECKIDENT (N'[procedure_request]', RESEED, 0)
GO

CREATE TABLE [pseudo_id] (
[id] bigint IDENTITY(1,1),
[patient_id] varchar(255) NOT NULL,
[salt_key_name] varchar(50) NOT NULL,
[pseudo_id] varchar(255) NULL DEFAULT NULL,
PRIMARY KEY ([patient_id], [salt_key_name]) 
)
GO
CREATE UNIQUE INDEX [pseudo_id_id] ON [pseudo_id] ([id])
GO
CREATE INDEX [pseudo_id_pseudo_id] ON [pseudo_id] ([pseudo_id])
GO
DBCC CHECKIDENT (N'[pseudo_id]', RESEED, 0)
GO

CREATE TABLE [referral_request] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[requester_organization_id] bigint NULL DEFAULT NULL,
[recipient_organization_id] bigint NULL DEFAULT NULL,
[referral_request_priority_concept_id] int NULL DEFAULT NULL,
[referral_request_type_concept_id] int NULL DEFAULT NULL,
[mode] varchar(50) NULL DEFAULT NULL,
[outgoing_referral] tinyint NULL DEFAULT NULL,
[is_review] tinyint NOT NULL,
[core_concept_id] int NULL DEFAULT NULL,
[non_core_concept_id] int NULL DEFAULT NULL,
[age_at_event] decimal(5,2) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [person_id], [id]) 
)
GO
CREATE UNIQUE INDEX [referral_request_id] ON [referral_request] ([id])
GO
CREATE INDEX [fk_referral_request_encounter_id] ON [referral_request] ([encounter_id])
GO
CREATE INDEX [fk_referral_request_patient_id_organization_id] ON [referral_request] ([patient_id], [organization_id])
GO
CREATE INDEX [fk_referral_request_practitioner_id] ON [referral_request] ([practitioner_id])
GO
CREATE INDEX [fk_referral_request_recipient_organization_id] ON [referral_request] ([recipient_organization_id])
GO
CREATE INDEX [fk_referral_request_requester_organization_id] ON [referral_request] ([requester_organization_id])
GO
CREATE INDEX [referral_request_patient_id] ON [referral_request] ([patient_id])
GO
CREATE INDEX [referral_request_core_concept_id] ON [referral_request] ([core_concept_id])
GO
DBCC CHECKIDENT (N'[referral_request]', RESEED, 0)
GO

CREATE TABLE [schedule] (
[id] bigint IDENTITY(1,1),
[organization_id] bigint NOT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[start_date] date NULL DEFAULT NULL,
[type] varchar(255) NULL DEFAULT NULL,
[location] varchar(255) NULL DEFAULT NULL,
[name] varchar(150) NULL DEFAULT NULL,
PRIMARY KEY ([organization_id], [id]) 
)
GO
CREATE UNIQUE INDEX [schedule_id] ON [schedule] ([id])
GO
DBCC CHECKIDENT (N'[schedule]', RESEED, 0)
GO

ALTER TABLE [patient] ADD CONSTRAINT [fk_patient_organization_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [allergy_intolerance] ADD CONSTRAINT [fk_allergy_intolerance_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [allergy_intolerance] ADD CONSTRAINT [fk_allergy_intolerance_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [allergy_intolerance] ADD CONSTRAINT [fk_allergy_intolerance_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [appointment] ADD CONSTRAINT [fk_appointment_organization_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [appointment] ADD CONSTRAINT [fk_appointment_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [diagnostic_order] ADD CONSTRAINT [fk_diagnostic_order_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [diagnostic_order] ADD CONSTRAINT [fk_diagnostic_order_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [diagnostic_order] ADD CONSTRAINT [fk_diagnostic_order_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_appointment_id] FOREIGN KEY ([appointment_id]) REFERENCES [appointment] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_episode_of_care_id] FOREIGN KEY ([episode_of_care_id]) REFERENCES [episode_of_care] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_service_provider_organization_id] FOREIGN KEY ([service_provider_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [episode_of_care] ADD CONSTRAINT [fk_episode_of_care_patient_id_organisation_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [episode_of_care] ADD CONSTRAINT [fk_episode_of_care_practitioner_id] FOREIGN KEY ([usual_gp_practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [flag] ADD CONSTRAINT [fk_flag_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [location] ADD CONSTRAINT [fk_location_organisation_id] FOREIGN KEY ([managing_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [medication_order] ADD CONSTRAINT [fk_medication_order_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [medication_order] ADD CONSTRAINT [fk_medication_order_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [medication_order] ADD CONSTRAINT [fk_medication_order_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [medication_statement] ADD CONSTRAINT [fk_medication_statement_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [medication_statement] ADD CONSTRAINT [fk_medication_statement_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [medication_statement] ADD CONSTRAINT [fk_medication_statement_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [observation] ADD CONSTRAINT [fk_observation_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [observation] ADD CONSTRAINT [fk_observation_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [observation] ADD CONSTRAINT [fk_observation_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [patient_address] ADD CONSTRAINT [fk_patient_address_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [patient_contact] ADD CONSTRAINT [fk_patient_contact_patient_id_organisation_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [patient_uprn] ADD CONSTRAINT [fk_patient_uprn_patient_id_organisation_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [practitioner] ADD CONSTRAINT [fk_practitioner_organisation_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [procedure_request] ADD CONSTRAINT [fk_procedure_request_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [procedure_request] ADD CONSTRAINT [fk_procedure_request_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [procedure_request] ADD CONSTRAINT [fk_procedure_request_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_patient_id_organization_id] FOREIGN KEY ([patient_id], [organization_id]) REFERENCES [patient] ([id], [organization_id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_recipient_organization_id] FOREIGN KEY ([recipient_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_requester_organization_id] FOREIGN KEY ([requester_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [schedule] ADD CONSTRAINT [fk_schedule_organization_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO

