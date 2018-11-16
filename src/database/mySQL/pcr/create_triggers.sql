use pcr;

drop trigger if exists after_patient_insert;

DELIMITER $$
CREATE TRIGGER after_patient_insert
  AFTER INSERT ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set organisation_id = NEW.organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
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
    set organisation_id = OLD.organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
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
    set organisation_id = OLD.organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 8,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_patient_address_insert;

DELIMITER $$
CREATE TRIGGER after_patient_address_insert
  AFTER INSERT ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        p.organisation_id,
        now(),
        NEW.entered_by_practitioner_id,
        0,
        0,
        9,
        NEW.id
      from patient p
      where p.id = NEW.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_patient_address_update;

DELIMITER $$
CREATE TRIGGER after_patient_address_update
  AFTER UPDATE ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        p.organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        0,
        9,
        OLD.id
      from patient p
      where p.id = OLD.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_patient_address_delete;

DELIMITER $$
CREATE TRIGGER after_patient_address_delete
  AFTER DELETE ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        p.organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        1,
        9,
        OLD.id
      from patient p
      where p.id = OLD.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_patient_identifier_insert;

DELIMITER $$
CREATE TRIGGER after_patient_identifier_insert
  AFTER INSERT ON patient_identifier
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        p.organisation_id,
        now(),
        NEW.entered_by_practitioner_id,
        0,
        0,
        11,
        NEW.id
      from patient p
      where p.id = NEW.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_patient_identifier_update;

DELIMITER $$
CREATE TRIGGER after_patient_identifier_update
  AFTER UPDATE ON patient_identifier
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        p.organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        0,
        11,
        OLD.id
      from patient p
      where p.id = OLD.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_patient_identifier_delete;

DELIMITER $$
CREATE TRIGGER after_patient_identifier_delete
  AFTER DELETE ON patient_identifier
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        p.organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        1,
        11,
        OLD.id
      from patient p
      where p.id = OLD.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_observation_insert;

DELIMITER $$
CREATE TRIGGER after_observation_insert
  AFTER INSERT ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 32,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_observation_update;

DELIMITER $$
CREATE TRIGGER after_observation_update
  AFTER UPDATE ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 32,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_observation_delete;

DELIMITER $$
CREATE TRIGGER after_observation_delete
  AFTER DELETE ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 32,
      item_id = OLD.id;
  END$$
DELIMITER ;


drop trigger if exists after_observation_value_insert;

DELIMITER $$
CREATE TRIGGER after_observation_value_insert
  AFTER INSERT ON observation_value
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        o.owning_organisation_id,
        now(),
        NEW.entered_by_practitioner_id,
        0,
        0,
        39,
        NEW.observation_id			-- observation_id is the unique id for this table
      from observation o
      where o.id = NEW.observation_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_observation_value_update;

DELIMITER $$
CREATE TRIGGER after_observation_value_update
  AFTER UPDATE ON observation_value
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        o.owning_organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        0,
        39,
        OLD.observation_id			-- observation_id is the unique id for this table
      from observation o
      where o.id = OLD.observation_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_observation_value_delete;

DELIMITER $$
CREATE TRIGGER after_observation_value_delete
  AFTER DELETE ON observation_value
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        o.owning_organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        1,
        39,
        OLD.observation_id			-- observation_id is the unique id for this table
      from observation o
      where o.id = OLD.observation_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_allergy_insert;

DELIMITER $$
CREATE TRIGGER after_allergy_insert
  AFTER INSERT ON allergy
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 41,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_allergy_update;

DELIMITER $$
CREATE TRIGGER after_allergy_update
  AFTER UPDATE ON allergy
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 41,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_allergy_delete;

DELIMITER $$
CREATE TRIGGER after_allergy_delete
  AFTER DELETE ON allergy
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 41,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_immunisation_insert;

DELIMITER $$
CREATE TRIGGER after_immunisation_insert
  AFTER INSERT ON immunisation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 40,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_immunisation_update;

DELIMITER $$
CREATE TRIGGER after_immunisation_update
  AFTER UPDATE ON immunisation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 40,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_immunisation_delete;

DELIMITER $$
CREATE TRIGGER after_immunisation_delete
  AFTER DELETE ON immunisation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 40,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_problem_insert;

DELIMITER $$
CREATE TRIGGER after_problem_insert
  AFTER INSERT ON problem
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        o.owning_organisation_id,
        now(),
        NEW.entered_by_practitioner_id,
        0,
        0,
        34,
        NEW.id
      from observation o
      where o.id = NEW.observation_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_problem_update;

DELIMITER $$
CREATE TRIGGER after_problem_update
  AFTER UPDATE ON problem
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        o.owning_organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        0,
        34,
        OLD.id
      from observation o
      where o.id = OLD.observation_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_problem_delete;

DELIMITER $$
CREATE TRIGGER after_problem_delete
  AFTER DELETE ON problem
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
      select
        null,
        o.owning_organisation_id,
        now(),
        OLD.entered_by_practitioner_id,
        0,
        1,
        34,
        OLD.id
      from observation o
      where o.id = OLD.observation_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;