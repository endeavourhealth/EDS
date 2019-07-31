use pcr_patient;

-- note this only contains an example history table and triggers for populating it for a single table

-- create example history table by using the main table
create table zz_history_allergy_intolerance
as select *
from allergy_intolerance;

alter table zz_history_allergy_intolerance
add inserted_at datetime(3);

alter table zz_history_allergy_intolerance
add primary key(id, inserted_at);

-- create triggers for update and delete
drop trigger if exists after_allergy_intolerance_update_history;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_update_history
  AFTER UPDATE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO zz_history_allergy_intolerance (
        inserted_at,
        id,
        patient_id,
        recording_practitioner_id,
        recording_date,
        effective_practitioner_id,
        effective_date,
        effective_date_precision_id,
        concept_id,
        is_confidential,
        encounter_id,
        problem_observation_id,
        additional_data
    ) VALUES (
        now(3), -- current time inc ms
        OLD.id,
        OLD.patient_id,
        OLD.recording_practitioner_id,
        OLD.recording_date,
        OLD.effective_practitioner_id,
        OLD.effective_date,
        OLD.effective_date_precision_id,
        OLD.concept_id,
        OLD.is_confidential,
        OLD.encounter_id,
        OLD.problem_observation_id,
        OLD.additional_data
    );
  END$$
DELIMITER ;



drop trigger if exists after_allergy_intolerance_delete_history;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_delete_history
  AFTER UPDATE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO zz_history_allergy_intolerance (
        inserted_at,
        id,
        patient_id,
        recording_practitioner_id,
        recording_date,
        effective_practitioner_id,
        effective_date,
        effective_date_precision_id,
        concept_id,
        is_confidential,
        encounter_id,
        problem_observation_id,
        additional_data
    ) VALUES (
        now(3), -- current time inc ms
        OLD.id,
        OLD.patient_id,
        OLD.recording_practitioner_id,
        OLD.recording_date,
        OLD.effective_practitioner_id,
        OLD.effective_date,
        OLD.effective_date_precision_id,
        OLD.concept_id,
        OLD.is_confidential,
        OLD.encounter_id,
        OLD.problem_observation_id,
        OLD.additional_data
    );
  END$$
DELIMITER ;

