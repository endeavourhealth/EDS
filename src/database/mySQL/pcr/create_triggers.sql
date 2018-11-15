use pcr;

use pcr;

drop trigger if exists after_patient_insert;

DELIMITER $$
CREATE TRIGGER after_patient_insert
  AFTER INSERT ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    SET organisation_id = NEW.organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id ,
      device_id = 0,
      entry_mode = 0,
      table_id = 8,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_patient_update;

DELIMITER $$
CREATE TRIGGER after_patient_update
  AFTER UPDATE ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    SET organisation_id = OLD.organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id ,
      device_id = 0,
      entry_mode = 0,
      table_id = 8,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_patient_delete;

DELIMITER $$
CREATE TRIGGER after_patient_delete
  AFTER DELETE ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    SET organisation_id = OLD.organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id ,
      device_id = 0,
      entry_mode = 1,
      table_id = 8,
      item_id = OLD.id;
  END$$
DELIMITER ;

