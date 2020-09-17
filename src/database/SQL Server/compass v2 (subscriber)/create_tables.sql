
-- drop SP
IF OBJECT_ID('dbo.update_person_record') IS NOT NULL DROP PROCEDURE update_person_record;
GO

-- drop tables
IF OBJECT_ID('dbo.registration_status_history', 'U') IS NOT NULL DROP TABLE dbo.registration_status_history
GO
IF OBJECT_ID('dbo.patient_pseudo_id', 'U') IS NOT NULL DROP TABLE dbo.patient_pseudo_id
GO
IF OBJECT_ID('dbo.pseudo_id', 'U') IS NOT NULL DROP TABLE dbo.pseudo_id
GO
IF OBJECT_ID('dbo.allergy_intolerance', 'U') IS NOT NULL DROP TABLE dbo.allergy_intolerance
GO
IF OBJECT_ID('dbo.diagnostic_order', 'U') IS NOT NULL DROP TABLE dbo.diagnostic_order
GO
IF OBJECT_ID('dbo.medication_order', 'U') IS NOT NULL DROP TABLE dbo.medication_order
GO
IF OBJECT_ID('dbo.medication_statement', 'U') IS NOT NULL DROP TABLE dbo.medication_statement
GO
IF OBJECT_ID('dbo.flag', 'U') IS NOT NULL DROP TABLE dbo.flag
GO
IF OBJECT_ID('dbo.observation', 'U') IS NOT NULL DROP TABLE dbo.observation
GO
IF OBJECT_ID('dbo.procedure_request', 'U') IS NOT NULL DROP TABLE dbo.procedure_request
GO
IF OBJECT_ID('dbo.referral_request', 'U') IS NOT NULL DROP TABLE dbo.referral_request
GO
IF OBJECT_ID('dbo.patient_contact', 'U') IS NOT NULL DROP TABLE dbo.patient_contact
GO
IF OBJECT_ID('dbo.patient_address', 'U') IS NOT NULL DROP TABLE dbo.patient_address
GO
IF OBJECT_ID('dbo.patient_uprn', 'U') IS NOT NULL DROP TABLE dbo.patient_uprn
GO
IF OBJECT_ID('dbo.patient_additional', 'U') IS NOT NULL DROP TABLE dbo.patient_additional
GO
IF OBJECT_ID('dbo.event_log', 'U') IS NOT NULL DROP TABLE dbo.event_log
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
IF OBJECT_ID('dbo.location', 'U') IS NOT NULL DROP TABLE dbo.location
GO
IF OBJECT_ID('dbo.schedule', 'U') IS NOT NULL DROP TABLE dbo.schedule
GO
IF OBJECT_ID('dbo.practitioner', 'U') IS NOT NULL DROP TABLE dbo.practitioner
GO
IF OBJECT_ID('dbo.organization', 'U') IS NOT NULL DROP TABLE dbo.organization
GO


CREATE TABLE [allergy_intolerance] (
[id] bigint,
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
[date_recorded] datetime NULL DEFAULT NULL,
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


CREATE TABLE [appointment] (
[id] bigint,
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[schedule_id] bigint NULL DEFAULT NULL,
[start_date] datetime2(0) NULL DEFAULT NULL,
[planned_duration] int NULL DEFAULT NULL,
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


CREATE TABLE [diagnostic_order] (
[id] bigint,
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


CREATE TABLE [encounter] (
[id] bigint,
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
[date_recorded] datetime NULL DEFAULT NULL,
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



CREATE TABLE [encounter_event] (
[id] bigint,
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NOT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[appointment_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] datetime NULL DEFAULT NULL,
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
[date_recorded] datetime NULL DEFAULT NULL,
[finished] bit NOT NULL,
PRIMARY KEY ([organization_id], [person_id], [id])
)
GO
CREATE UNIQUE INDEX [encounter_event_id] ON [encounter_event] ([id])
GO
CREATE INDEX [fk_encounter_event_practitioner_id] ON [encounter_event] ([practitioner_id])
GO
CREATE INDEX [fk_encounter_event_episode_of_care_id] ON [encounter_event] ([episode_of_care_id])
GO
CREATE INDEX [fk_encounter_event_service_provider_organization_id] ON [encounter_event] ([service_provider_organization_id])
GO
CREATE INDEX [encounter_event_patient_id] ON [encounter_event] ([patient_id])
GO
CREATE INDEX [fki_encounter_event_appointment_id] ON [encounter_event] ([appointment_id])
GO
CREATE INDEX [fki_encounter_event_patient_id_organization_id] ON [encounter_event] ([patient_id], [organization_id])
GO
CREATE INDEX [encounter_event_core_concept_id_clinical_effective_date] ON [encounter_event] ([core_concept_id], [clinical_effective_date])
GO

CREATE TABLE [episode_of_care] (
[id] bigint,
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


CREATE TABLE [event_log] (
[dt_change] datetime2(3) NOT NULL,
[change_type] tinyint NOT NULL,
[table_id] tinyint NOT NULL,
[record_id] bigint NOT NULL
)
GO

CREATE TABLE [flag] (
[id] bigint,
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


CREATE TABLE [location] (
[id] bigint,
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


CREATE TABLE [medication_order] (
[id] bigint,
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


CREATE TABLE [medication_statement] (
[id] bigint,
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[encounter_id] bigint NULL DEFAULT NULL,
[practitioner_id] bigint NULL DEFAULT NULL,
[clinical_effective_date] date NULL DEFAULT NULL,
[date_precision_concept_id] int NULL DEFAULT NULL,
[is_active] tinyint NULL,
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


CREATE TABLE [observation] (
[id] bigint,
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
[date_recorded] datetime NULL DEFAULT NULL,
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


CREATE TABLE [organization] (
[id] bigint,
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


CREATE TABLE [patient] (
[id] bigint,
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


CREATE TABLE [patient_address] (
[id] bigint,
[organization_id] bigint NOT NULL,
[patient_id] bigint NOT NULL,
[person_id] bigint NOT NULL,
[address_line_1] varchar(255) NULL DEFAULT NULL,
[address_line_2] varchar(255) NULL DEFAULT NULL,
[address_line_3] varchar(255) NULL DEFAULT NULL,
[address_line_4] varchar(255) NULL DEFAULT NULL,
[city] varchar(255) NULL DEFAULT NULL,
[postcode] varchar(255) NULL DEFAULT NULL,
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
create unique index ux_patient_address_id on patient_address (id);
GO

CREATE TABLE [patient_additional] (
  [id] bigint NOT NULL ,
  [property_id] varchar(255)  NOT NULL,
  [value_id] varchar(255) NOT NULL ,
  PRIMARY KEY ([id], [property_id])
)
GO
CREATE INDEX [ix_patient_additional_id]  ON [patient_additional]  (value_id)
GO

CREATE TABLE [patient_contact] (
[id] bigint,
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
create unique index ux_patient_contact_id on patient_contact (id);
GO


CREATE TABLE [patient_uprn] (
[patient_id] bigint,
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


CREATE TABLE [person] (
[id] bigint,
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


CREATE TABLE [practitioner] (
[id] bigint,
[organization_id] bigint NOT NULL,
[name] varchar(1024) NULL DEFAULT NULL,
[role_code] varchar(50) NULL DEFAULT NULL,
[role_desc] varchar(255) NULL DEFAULT NULL,
[gmc_code] varchar(50) NULL DEFAULT NULL,
PRIMARY KEY ([id])
)
GO
CREATE UNIQUE INDEX [practitioner_id] ON [practitioner] ([id])
GO
CREATE INDEX [fk_practitioner_organisation_id] ON [practitioner] ([organization_id])
GO


CREATE TABLE [procedure_request] (
[id] bigint,
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
[date_recorded] datetime NULL DEFAULT NULL,
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

/*

CREATE TABLE [pseudo_id] (
[id] bigint,
[patient_id] bigint NOT NULL,
[salt_key_name] varchar(50) NOT NULL,
[pseudo_id] varchar(255) NULL DEFAULT NULL,
PRIMARY KEY ([patient_id], [salt_key_name])
)
GO
CREATE UNIQUE INDEX [pseudo_id_id] ON [pseudo_id] ([id])
GO
CREATE INDEX [pseudo_id_pseudo_id] ON [pseudo_id] ([pseudo_id])
GO
*/


CREATE TABLE [referral_request] (
[id] bigint,
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
[date_recorded] datetime NULL DEFAULT NULL,
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


CREATE TABLE [schedule] (
[id] bigint,
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


CREATE TABLE patient_pseudo_id
(
  id bigint NOT NULL,
  organization_id bigint NOT NULL,
  patient_id bigint NOT NULL,
  person_id bigint NOT NULL,
  salt_name varchar(50) NOT NULL,
  skid varchar(255) NOT NULL,
  is_nhs_number_valid bit NOT NULL,
  is_nhs_number_verified_by_publisher bit NOT NULL,
  CONSTRAINT pk_patient_pseudo_id PRIMARY KEY (organization_id, person_id, id)
);
GO
CREATE UNIQUE INDEX ux_patient_pseudo_id ON patient_pseudo_id (id);
GO
CREATE INDEX patient_pseudo_id_patient ON patient_pseudo_id (patient_id);
GO

CREATE TABLE registration_status_history (
   id bigint NOT NULL,
   organization_id bigint NOT NULL,
   patient_id bigint NOT NULL,
   person_id bigint NOT NULL,
   episode_of_care_id bigint DEFAULT NULL,
   registration_status_concept_id int DEFAULT NULL,
   start_date datetime DEFAULT NULL,
   end_date datetime DEFAULT NULL,
   PRIMARY KEY (organization_id, id, patient_id, person_id)
);
GO
CREATE UNIQUE INDEX ux_registration_status_history_id ON registration_status_history (id);
GO
CREATE INDEX ix_registration_status_history_patient ON registration_status_history (patient_id);
GO


ALTER TABLE [patient] ADD CONSTRAINT [fk_patient_organization_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [allergy_intolerance] ADD CONSTRAINT [fk_allergy_intolerance_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [allergy_intolerance] ADD CONSTRAINT [fk_allergy_intolerance_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [allergy_intolerance] ADD CONSTRAINT [fk_allergy_intolerance_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [appointment] ADD CONSTRAINT [fk_appointment_organization_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [appointment] ADD CONSTRAINT [fk_appointment_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [diagnostic_order] ADD CONSTRAINT [fk_diagnostic_order_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [diagnostic_order] ADD CONSTRAINT [fk_diagnostic_order_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [diagnostic_order] ADD CONSTRAINT [fk_diagnostic_order_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
-- known examples of Emis consultations referring to unknown appointments, so removed this
-- ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_appointment_id] FOREIGN KEY ([appointment_id]) REFERENCES [appointment] ([id])
-- GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_episode_of_care_id] FOREIGN KEY ([episode_of_care_id]) REFERENCES [episode_of_care] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [encounter] ADD CONSTRAINT [fk_encounter_service_provider_organization_id] FOREIGN KEY ([service_provider_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [episode_of_care] ADD CONSTRAINT [fk_episode_of_care_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [episode_of_care] ADD CONSTRAINT [fk_episode_of_care_practitioner_id] FOREIGN KEY ([usual_gp_practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [flag] ADD CONSTRAINT [fk_flag_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [location] ADD CONSTRAINT [fk_location_organisation_id] FOREIGN KEY ([managing_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [medication_order] ADD CONSTRAINT [fk_medication_order_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [medication_order] ADD CONSTRAINT [fk_medication_order_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [medication_order] ADD CONSTRAINT [fk_medication_order_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [medication_statement] ADD CONSTRAINT [fk_medication_statement_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [medication_statement] ADD CONSTRAINT [fk_medication_statement_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [medication_statement] ADD CONSTRAINT [fk_medication_statement_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [observation] ADD CONSTRAINT [fk_observation_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [observation] ADD CONSTRAINT [fk_observation_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [observation] ADD CONSTRAINT [fk_observation_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [patient_address] ADD CONSTRAINT [fk_patient_address_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [patient_contact] ADD CONSTRAINT [fk_patient_contact_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [patient_uprn] ADD CONSTRAINT [fk_patient_uprn_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [practitioner] ADD CONSTRAINT [fk_practitioner_organisation_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [procedure_request] ADD CONSTRAINT [fk_procedure_request_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [procedure_request] ADD CONSTRAINT [fk_procedure_request_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [procedure_request] ADD CONSTRAINT [fk_procedure_request_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_encounter_id] FOREIGN KEY ([encounter_id]) REFERENCES [encounter] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_patient_id] FOREIGN KEY ([patient_id]) REFERENCES [patient] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_practitioner_id] FOREIGN KEY ([practitioner_id]) REFERENCES [practitioner] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_recipient_organization_id] FOREIGN KEY ([recipient_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [referral_request] ADD CONSTRAINT [fk_referral_request_requester_organization_id] FOREIGN KEY ([requester_organization_id]) REFERENCES [organization] ([id])
GO
ALTER TABLE [schedule] ADD CONSTRAINT [fk_schedule_organization_id] FOREIGN KEY ([organization_id]) REFERENCES [organization] ([id])
GO




CREATE PROCEDURE update_person_record(
	@new_person_id AS bigint,
	@old_person_id AS bigint
)
AS
BEGIN

	DECLARE @best_patient_id bigint;
    DECLARE @patients_remaning INT;
	--print 'running SP';
	--print '@new_person_id = ' + COALESCE(CONVERT(varchar, @new_person_id), 'null');
	--print '@old_person_id = ' + COALESCE(CONVERT(varchar, @old_person_id), 'null');

	IF (@new_person_id IS NOT NULL)
	BEGIN

		SET @best_patient_id = (
			SELECT id
            FROM
			(SELECT TOP 1
				p.id as [id],
				CASE WHEN (e.registration_type_concept_id = 1335267) THEN 1 ELSE 0 END AS [registration_type_rank], -- if reg type = GMS then up-rank
				CASE WHEN (e.registration_status_concept_id is null or e.registration_status_concept_id not in (1335283, 1335284, 1335285)) THEN 1 ELSE 0 END AS [registration_status_rank], -- if pre-registered status, then down-rank
				CASE WHEN (p.date_of_death is not null) THEN 1 ELSE 0 END AS [death_rank], --  records is a date of death more likely to be actively used, so up-vote
				CASE WHEN (e.date_registered_end is null) THEN '9999-12-31' ELSE e.date_registered_end END AS [date_registered_end_sortable] -- up-vote non-ended ones
			FROM patient p
			LEFT OUTER JOIN episode_of_care e
				ON e.organization_id = p.organization_id
				AND e.patient_id = p.id
			WHERE
				p.person_id = @new_person_id
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
			SELECT person_id, organization_id, title, first_names, last_name, gender_concept_id, nhs_number, date_of_birth, date_of_death, current_address_id, ethnic_code_concept_id, registered_practice_organization_id
			FROM patient
			WHERE id = @best_patient_id
		) as a
		ON (a.person_id = e.id)
		WHEN MATCHED
			THEN UPDATE SET
				-- e.id = a.person_id,
				e.organization_id = a.organization_id,
				e.title = a.title,
				e.first_names = a.first_names,
				e.last_name = a.last_name,
				e.gender_concept_id = a.gender_concept_id,
				e.nhs_number = a.nhs_number,
				e.date_of_birth = a.date_of_birth,
				e.date_of_death = a.date_of_death,
				e.current_address_id = a.current_address_id,
				e.ethnic_code_concept_id = a.ethnic_code_concept_id,
				e.registered_practice_organization_id = a.registered_practice_organization_id
		WHEN NOT MATCHED BY TARGET
			THEN INSERT (id, organization_id, title, first_names, last_name, gender_concept_id, nhs_number, date_of_birth, date_of_death, current_address_id, ethnic_code_concept_id, registered_practice_organization_id)
			VALUES (a.person_id, a.organization_id, a.title, a.first_names, a.last_name, a.gender_concept_id, a.nhs_number, a.date_of_birth, a.date_of_death, a.current_address_id, a.ethnic_code_concept_id, a.registered_practice_organization_id);
	END

    IF (@old_person_id IS NOT NULL)
	BEGIN

		SET @patients_remaning = (select COUNT(1) from patient where person_id = @old_person_id);
		--PRINT 'patients remaining = ' + COALESCE(CONVERT(varchar, @patients_remaning), 'null');
        IF (@patients_remaning = 0)
		BEGIN
			--PRINT 'will delete all for old person ID';
			DELETE FROM person
            WHERE id = @old_person_id;
		END
        ELSE
		BEGIN
			--PRINT 'will recurse to re-do for old person ID';
			EXEC update_person_record @old_person_id, null;
        END

    END

END

GO



CREATE TRIGGER [after_allergy_intolerance_insert]
ON [allergy_intolerance]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        4, -- allergy_intolerance
        id from inserted
  END
GO
ALTER TABLE [allergy_intolerance] ENABLE TRIGGER [after_allergy_intolerance_insert]
GO
CREATE TRIGGER [after_allergy_intolerance_update]
ON [allergy_intolerance]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        4, -- allergy_intolerance
        id from deleted
  END
GO
ALTER TABLE [allergy_intolerance] ENABLE TRIGGER [after_allergy_intolerance_update]
GO
CREATE TRIGGER [after_allergy_intolerance_delete]
ON [allergy_intolerance]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        4, -- allergy_intolerance
        id from deleted
  END
GO
ALTER TABLE [allergy_intolerance] ENABLE TRIGGER [after_allergy_intolerance_delete]
GO

CREATE TRIGGER [after_appointment_insert]
ON [appointment]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        18, -- appointment
        id from inserted
  END
GO
ALTER TABLE [appointment] ENABLE TRIGGER [after_appointment_insert]
GO
CREATE TRIGGER [after_appointment_update]
ON [appointment]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        18, -- appointment
        id from deleted
  END
GO
ALTER TABLE [appointment] ENABLE TRIGGER [after_appointment_update]
GO
CREATE TRIGGER [after_appointment_delete]
ON [appointment]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        18, -- appointment
        id from deleted
  END
GO
ALTER TABLE [appointment] ENABLE TRIGGER [after_appointment_delete]
GO


CREATE TRIGGER [after_encounter_event_insert]
ON [encounter_event]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        25, -- encounter_event
        id from inserted
  END
GO
ALTER TABLE [encounter_event] ENABLE TRIGGER [after_encounter_event_insert]
GO
CREATE TRIGGER [after_encounter_event_update]
ON [encounter_event]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        25, -- encounter_event
        id from deleted
  END
GO
ALTER TABLE [encounter_event] ENABLE TRIGGER [after_encounter_event_update]
GO
CREATE TRIGGER [after_encounter_event_delete]
ON [encounter_event]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        25, -- encounter_event
        id from deleted
  END
GO
ALTER TABLE [encounter_event] ENABLE TRIGGER [after_encounter_event_delete]
GO


CREATE TRIGGER [after_encounter_insert]
ON [encounter]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        5, -- encounter
        id from inserted
  END
GO
ALTER TABLE [encounter] ENABLE TRIGGER [after_encounter_insert]
GO
CREATE TRIGGER [after_encounter_update]
ON [encounter]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        5, -- encounter
        id from deleted
  END
GO
ALTER TABLE [encounter] ENABLE TRIGGER [after_encounter_update]
GO
CREATE TRIGGER [after_encounter_delete]
ON [encounter]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        5, -- encounter
        id from deleted
  END
GO
ALTER TABLE [encounter] ENABLE TRIGGER [after_encounter_delete]
GO

CREATE TRIGGER [after_episode_of_care_insert]
ON [episode_of_care]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        6, -- episode_of_care
        id from inserted
  END
GO
ALTER TABLE [episode_of_care] ENABLE TRIGGER [after_episode_of_care_insert]
GO
CREATE TRIGGER [after_episode_of_care_update]
ON [episode_of_care]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        6, -- episode_of_care
        id from deleted
  END
GO
ALTER TABLE [episode_of_care] ENABLE TRIGGER [after_episode_of_care_update]
GO
CREATE TRIGGER [after_episode_of_care_delete]
ON [episode_of_care]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        6, -- episode_of_care
        id from deleted
  END
GO
ALTER TABLE [episode_of_care] ENABLE TRIGGER [after_episode_of_care_delete]
GO

CREATE TRIGGER [after_flag_insert]
ON [flag]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        7, -- flag
        id from inserted
  END
GO
ALTER TABLE [flag] ENABLE TRIGGER [after_flag_insert]
GO
CREATE TRIGGER [after_flag_update]
ON [flag]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        7, -- flag
        id from deleted
  END
GO
ALTER TABLE [flag] ENABLE TRIGGER [after_flag_update]
GO
CREATE TRIGGER [after_flag_delete]
ON [flag]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        7, -- flag
        id from deleted
  END
GO
ALTER TABLE [flag] ENABLE TRIGGER [after_flag_delete]
GO

CREATE TRIGGER [after_location_insert]
ON [location]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        8, -- location
        id from inserted
  END
GO
ALTER TABLE [location] ENABLE TRIGGER [after_location_insert]
GO
CREATE TRIGGER [after_location_update]
ON [location]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        8, -- location
        id from deleted
  END
GO
ALTER TABLE [location] ENABLE TRIGGER [after_location_update]
GO
CREATE TRIGGER [after_location_delete]
ON [location]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        8, -- location
        id from deleted
  END
GO
ALTER TABLE [location] ENABLE TRIGGER [after_location_delete]
GO

CREATE TRIGGER [after_medication_order_insert]
ON [medication_order]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        9, -- medication_order
        id from inserted
  END
GO
ALTER TABLE [medication_order] ENABLE TRIGGER [after_medication_order_insert]
GO
CREATE TRIGGER [after_medication_order_update]
ON [medication_order]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        9, -- medication_order
        id from deleted
  END
GO
ALTER TABLE [medication_order] ENABLE TRIGGER [after_medication_order_update]
GO
CREATE TRIGGER [after_medication_order_delete]
ON [medication_order]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        9, -- medication_order
        id from deleted
  END
GO
ALTER TABLE [medication_order] ENABLE TRIGGER [after_medication_order_delete]
GO

CREATE TRIGGER [after_medication_statement_insert]
ON [medication_statement]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        10, -- medication_statement
        id from inserted
  END
GO
ALTER TABLE [medication_statement] ENABLE TRIGGER [after_medication_statement_insert]
GO
CREATE TRIGGER [after_medication_statement_update]
ON [medication_statement]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        10, -- medication_statement
        id from deleted
  END
GO
ALTER TABLE [medication_statement] ENABLE TRIGGER [after_medication_statement_update]
GO
CREATE TRIGGER [after_medication_statement_delete]
ON [medication_statement]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        10, -- medication_statement
        id from deleted
  END
GO
ALTER TABLE [medication_statement] ENABLE TRIGGER [after_medication_statement_delete]
GO

CREATE TRIGGER [after_observation_insert]
ON [observation]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        11, -- observation
        id from inserted
  END
GO
ALTER TABLE [observation] ENABLE TRIGGER [after_observation_insert]
GO
CREATE TRIGGER [after_observation_update]
ON [observation]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        11, -- observation
        id from deleted
  END
GO
ALTER TABLE [observation] ENABLE TRIGGER [after_observation_update]
GO
CREATE TRIGGER [after_observation_delete]
ON [observation]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        11, -- observation
        id from deleted
  END
GO
ALTER TABLE [observation] ENABLE TRIGGER [after_observation_delete]
GO

CREATE TRIGGER [after_organization_insert]
ON [organization]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        12, -- organization
        id from inserted
  END
GO
ALTER TABLE [organization] ENABLE TRIGGER [after_organization_insert]
GO
CREATE TRIGGER [after_organization_update]
ON [organization]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        12, -- organization
        id from deleted
  END
GO
ALTER TABLE [organization] ENABLE TRIGGER [after_organization_update]
GO
CREATE TRIGGER [after_organization_delete]
ON [organization]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        12, -- organization
        id from deleted
  END
GO
ALTER TABLE [organization] ENABLE TRIGGER [after_organization_delete]
GO

CREATE TRIGGER [after_patient_insert]
ON [patient]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        2, -- patient
        id from inserted;

	-- update the person table too
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
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        2, -- patient
        id from deleted

	-- update the person table too
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
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        2, -- patient
        id from deleted
  END

  	-- update the person table too
	DECLARE @old_person_id BIGINT;
	SET @old_person_id = (SELECT person_id FROM deleted);
	EXEC update_person_record null, @old_person_id;

GO
ALTER TABLE [patient] ENABLE TRIGGER [after_patient_delete]
GO

CREATE TRIGGER [after_patient_address_insert]
ON [patient_address]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        20, -- patient_address
        id from inserted
  END
GO
ALTER TABLE [patient_address] ENABLE TRIGGER [after_patient_address_insert]
GO
CREATE TRIGGER [after_patient_address_update]
ON [patient_address]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        20, -- patient_address
        id from deleted
  END
GO
ALTER TABLE [patient_address] ENABLE TRIGGER [after_patient_address_update]
GO
CREATE TRIGGER [after_patient_address_delete]
ON [patient_address]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        20, -- patient_address
        id from deleted
  END
GO
ALTER TABLE [patient_address] ENABLE TRIGGER [after_patient_address_delete]
GO

CREATE TRIGGER [after_patient_contact_insert]
ON [patient_contact]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        19, -- patient_contact
        id from inserted
  END
GO
ALTER TABLE [patient_contact] ENABLE TRIGGER [after_patient_contact_insert]
GO
CREATE TRIGGER [after_patient_contact_update]
ON [patient_contact]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        19, -- patient_contact
        id from deleted
  END
GO
ALTER TABLE [patient_contact] ENABLE TRIGGER [after_patient_contact_update]
GO
CREATE TRIGGER [after_patient_contact_delete]
ON [patient_contact]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        19, -- patient_contact
        id from deleted
  END
GO
ALTER TABLE [patient_contact] ENABLE TRIGGER [after_patient_contact_delete]
GO

CREATE TRIGGER [after_person_insert]
ON [person]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        3, -- person
        id from inserted
  END
GO
ALTER TABLE [person] ENABLE TRIGGER [after_person_insert]
GO
CREATE TRIGGER [after_person_update]
ON [person]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        3, -- person
        id from deleted
  END
GO
ALTER TABLE [person] ENABLE TRIGGER [after_person_update]
GO
CREATE TRIGGER [after_person_delete]
ON [person]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        3, -- person
        id from deleted
  END
GO
ALTER TABLE [person] ENABLE TRIGGER [after_person_delete]
GO

CREATE TRIGGER [after_practitioner_insert]
ON [practitioner]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        13, -- practitioner
        id from inserted
  END
GO
ALTER TABLE [practitioner] ENABLE TRIGGER [after_practitioner_insert]
GO
CREATE TRIGGER [after_practitioner_update]
ON [practitioner]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        13, -- practitioner
        id from deleted
  END
GO
ALTER TABLE [practitioner] ENABLE TRIGGER [after_practitioner_update]
GO
CREATE TRIGGER [after_practitioner_delete]
ON [practitioner]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        13, -- practitioner
        id from deleted
  END
GO
ALTER TABLE [practitioner] ENABLE TRIGGER [after_practitioner_delete]
GO

CREATE TRIGGER [after_procedure_request_insert]
ON [procedure_request]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        14, -- procedure_request
        id from inserted
  END
GO
ALTER TABLE [procedure_request] ENABLE TRIGGER [after_procedure_request_insert]
GO
CREATE TRIGGER [after_procedure_request_update]
ON [procedure_request]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        14, -- procedure_request
        id from deleted
  END
GO
ALTER TABLE [procedure_request] ENABLE TRIGGER [after_procedure_request_update]
GO
CREATE TRIGGER [after_procedure_request_delete]
ON [procedure_request]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        14, -- procedure_request
        id from deleted
  END
GO
ALTER TABLE [procedure_request] ENABLE TRIGGER [after_procedure_request_delete]
GO

/*CREATE TRIGGER [after_pseudo_id_insert]
ON [pseudo_id]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        15, -- pseudo_id
        id from inserted
  END
GO
ALTER TABLE [pseudo_id] ENABLE TRIGGER [after_pseudo_id_insert]
GO
CREATE TRIGGER [after_pseudo_id_update]
ON [pseudo_id]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        15, -- pseudo_id
        id from deleted
  END
GO
ALTER TABLE [pseudo_id] ENABLE TRIGGER [after_pseudo_id_update]
GO
CREATE TRIGGER [after_pseudo_id_delete]
ON [pseudo_id]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        15, -- pseudo_id
        id from deleted
  END
GO
ALTER TABLE [pseudo_id] ENABLE TRIGGER [after_pseudo_id_delete]
GO*/

CREATE TRIGGER [after_referral_request_insert]
ON [referral_request]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        16, -- referral_request
        id from inserted
  END
GO
ALTER TABLE [referral_request] ENABLE TRIGGER [after_referral_request_insert]
GO
CREATE TRIGGER [after_referral_request_update]
ON [referral_request]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        16, -- referral_request
        id from deleted
  END
GO
ALTER TABLE [referral_request] ENABLE TRIGGER [after_referral_request_update]
GO
CREATE TRIGGER [after_referral_request_delete]
ON [referral_request]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        16, -- referral_request
        id from deleted
  END
GO
ALTER TABLE [referral_request] ENABLE TRIGGER [after_referral_request_delete]
GO

CREATE TRIGGER [after_schedule_insert]
ON [schedule]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        17, -- schedule
        id from inserted
  END
GO
ALTER TABLE [schedule] ENABLE TRIGGER [after_schedule_insert]
GO
CREATE TRIGGER [after_schedule_update]
ON [schedule]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        17, -- schedule
        id from deleted
  END
GO
ALTER TABLE [schedule] ENABLE TRIGGER [after_schedule_update]
GO
CREATE TRIGGER [after_schedule_delete]
ON [schedule]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        17, -- schedule
        id from deleted
  END
GO
ALTER TABLE [schedule] ENABLE TRIGGER [after_schedule_delete]
GO

CREATE TRIGGER [after_diagnostic_order_insert]
ON [diagnostic_order]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        21, -- diagnostic_order
        id from inserted
  END
GO
ALTER TABLE [diagnostic_order] ENABLE TRIGGER [after_diagnostic_order_insert]
GO
CREATE TRIGGER [after_diagnostic_order_update]
ON [diagnostic_order]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        21, -- diagnostic_order
        id from deleted
  END
GO
ALTER TABLE [diagnostic_order] ENABLE TRIGGER [after_diagnostic_order_update]
GO
CREATE TRIGGER [after_diagnostic_order_delete]
ON [diagnostic_order]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        21, -- diagnostic_order
        id from deleted
  END
GO
ALTER TABLE [diagnostic_order] ENABLE TRIGGER [after_diagnostic_order_delete]
GO




CREATE TRIGGER [after_patient_pseudo_id_insert]
ON [patient_pseudo_id]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        27, -- patient_pseudo_id
        id from inserted
  END
GO
ALTER TABLE [patient_pseudo_id] ENABLE TRIGGER [after_patient_pseudo_id_insert]
GO
CREATE TRIGGER [after_patient_pseudo_id_update]
ON [patient_pseudo_id]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        27, -- patient_pseudo_id
        id from deleted
  END
GO
ALTER TABLE [patient_pseudo_id] ENABLE TRIGGER [after_patient_pseudo_id_update]
GO
CREATE TRIGGER [after_patient_pseudo_id_delete]
ON [patient_pseudo_id]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        27, -- patient_pseudo_id
        id from deleted
  END
GO
ALTER TABLE [patient_pseudo_id] ENABLE TRIGGER [after_patient_pseudo_id_delete]
GO



CREATE TRIGGER [after_registration_status_history_insert]
ON [registration_status_history]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        23, -- registration_status_history
        id from inserted
  END
GO
ALTER TABLE [registration_status_history] ENABLE TRIGGER [after_registration_status_history_insert]
GO
CREATE TRIGGER [after_registration_status_history_update]
ON [registration_status_history]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        23, -- registration_status_history
        id from deleted
  END
GO
ALTER TABLE [registration_status_history] ENABLE TRIGGER [after_registration_status_history_update]
GO
CREATE TRIGGER [after_registration_status_history_delete]
ON [registration_status_history]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        23, -- registration_status_history
        id from deleted
  END
GO
ALTER TABLE [registration_status_history] ENABLE TRIGGER [after_registration_status_history_delete]
GO

CREATE TRIGGER [after_patient_additional_insert]
ON [patient_additional]
WITH EXECUTE AS CALLER
After INSERT
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        0, -- insert
        28, -- patient_additional
        id from inserted
  END
GO
ALTER TABLE [patient_additional] ENABLE TRIGGER [after_patient_additional_insert]
GO
CREATE TRIGGER [after_patient_additional_update]
ON [patient_additional]
WITH EXECUTE AS CALLER
After UPDATE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        1, -- update
        28, -- patient_additional
        id from deleted
  END
GO
ALTER TABLE [patient_additional] ENABLE TRIGGER [after_patient_additional_update]
GO
CREATE TRIGGER [after_patient_additional_delete]
ON [patient_additional]
WITH EXECUTE AS CALLER
After DELETE
AS
BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
	) select
		GETDATE(), -- current time inc ms
        2, -- delete
        28, -- patient_additional
        id from deleted
  END
GO
ALTER TABLE [patient_additional] ENABLE TRIGGER [after_patient_additional_delete]
GO

