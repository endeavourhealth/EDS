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
        id from inserted
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

CREATE TRIGGER [after_pseudo_id_insert] 
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
GO

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
