use pcr_patient;

-- note this only contains an example trigger for populating the event_log table for a single table

drop trigger if exists after_allergy_intolerance_insert;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_insert
  AFTER INSERT ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		    table_id,
        record_id,
        event_timestamp,
        event_log_transaction_type_id,
        batch_id,
        service_id,
        patient_id,
        concept_id
    ) VALUES (
        1, -- table 1 = allergy_intolerance
        NEW.id,
        now(3), -- current time inc ms
        1, -- 1 = insert
        NEW.batch_id,
        NEW.service_id,
        NEW.patient_id,
        NEW.concept_id
    );
  END$$
DELIMITER ;

drop trigger if exists after_allergy_intolerance_update;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_update
  AFTER UPDATE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		    table_id,
        record_id,
        event_timestamp,
        event_log_transaction_type_id,
        batch_id,
        service_id,
        patient_id,
        concept_id
    ) VALUES (
        1, -- table 1 = allergy_intolerance
        NEW.id,
        now(3), -- current time inc ms
        1, -- 2 = insert
        NEW.batch_id,
        NEW.service_id,
        NEW.patient_id,
        NEW.concept_id
    );
  END$$
DELIMITER ;

drop trigger if exists after_allergy_intolerance_delete;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_delete
  AFTER UPDATE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		    table_id,
        record_id,
        event_timestamp,
        event_log_transaction_type_id,
        batch_id,
        service_id,
        patient_id,
        concept_id
    ) VALUES (
        1, -- table 1 = allergy_intolerance
        NEW.id,
        now(3), -- current time inc ms
        3, -- 3 = delete
        OLD.batch_id,
        OLD.service_id,
        OLD.patient_id,
        OLD.concept_id
    );
  END$$
DELIMITER ;



