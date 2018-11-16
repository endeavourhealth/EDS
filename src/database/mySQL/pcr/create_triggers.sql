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

drop trigger if exists after_consultation_insert;

DELIMITER $$
CREATE TRIGGER after_consultation_insert
  AFTER INSERT ON consultation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 26,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_consultation_update;

DELIMITER $$
CREATE TRIGGER after_consultation_update
  AFTER UPDATE ON consultation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 26,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_consultation_delete;

DELIMITER $$
CREATE TRIGGER after_consultation_delete
  AFTER DELETE ON consultation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 26,
      item_id = OLD.id;
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

drop trigger if exists after_flag_insert;

DELIMITER $$
CREATE TRIGGER after_flag_insert
  AFTER INSERT ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 33,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_flag_update;

DELIMITER $$
CREATE TRIGGER after_flag_update
  AFTER UPDATE ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 33,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_flag_delete;

DELIMITER $$
CREATE TRIGGER after_flag_delete
  AFTER DELETE ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 33,
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

drop trigger if exists after_procedure_request_insert;

DELIMITER $$
CREATE TRIGGER after_procedure_request_insert
  AFTER INSERT ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 35,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_procedure_request_update;

DELIMITER $$
CREATE TRIGGER after_procedure_request_update
  AFTER UPDATE ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 35,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_procedure_request_delete;

DELIMITER $$
CREATE TRIGGER after_procedure_request_delete
  AFTER DELETE ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 35,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_procedure_insert;

DELIMITER $$
CREATE TRIGGER after_procedure_insert
  AFTER INSERT ON `procedure`
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 36,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_procedure_update;

DELIMITER $$
CREATE TRIGGER after_procedure_update
  AFTER UPDATE ON `procedure`
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 36,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_procedure_delete;

DELIMITER $$
CREATE TRIGGER after_procedure_delete
  AFTER DELETE ON `procedure`
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 36,
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

drop trigger if exists after_referral_insert;

DELIMITER $$
CREATE TRIGGER after_referral_insert
  AFTER INSERT ON referral
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 42,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_referral_update;

DELIMITER $$
CREATE TRIGGER after_referral_update
  AFTER UPDATE ON referral
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 42,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_referral_delete;

DELIMITER $$
CREATE TRIGGER after_referral_delete
  AFTER DELETE ON referral
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 42,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_medication_amount_insert;

DELIMITER $$
CREATE TRIGGER after_medication_amount_insert
  AFTER INSERT ON medication_amount
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
        43,
        NEW.id
      from patient p
      where p.id = NEW.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_medication_amount_update;

DELIMITER $$
CREATE TRIGGER after_medication_amount_update
  AFTER UPDATE ON medication_amount
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
        43,
        OLD.id
      from patient p
      where p.id = OLD.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_medication_amount_delete;

DELIMITER $$
CREATE TRIGGER after_medication_amount_delete
  AFTER DELETE ON medication_amount
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
        43,
        OLD.id
      from patient p
      where p.id = OLD.patient_id;    -- use to derive the organisation_id for the event_log
  END$$
DELIMITER ;

drop trigger if exists after_medication_statement_insert;

DELIMITER $$
CREATE TRIGGER after_medication_statement_insert
  AFTER INSERT ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 44,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_medication_statement_update;

DELIMITER $$
CREATE TRIGGER after_medication_statement_update
  AFTER UPDATE ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 44,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_medication_statement_delete;

DELIMITER $$
CREATE TRIGGER after_medication_statement_delete
  AFTER DELETE ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 44,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_medication_order_insert;

DELIMITER $$
CREATE TRIGGER after_medication_order_insert
  AFTER INSERT ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = NEW.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = NEW.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 45,
      item_id = NEW.id;
  END$$
DELIMITER ;

drop trigger if exists after_medication_order_update;

DELIMITER $$
CREATE TRIGGER after_medication_order_update
  AFTER UPDATE ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 0,
      table_id = 45,
      item_id = OLD.id;
  END$$
DELIMITER ;

drop trigger if exists after_medication_order_delete;

DELIMITER $$
CREATE TRIGGER after_medication_order_delete
  AFTER DELETE ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log
    set
      organisation_id = OLD.owning_organisation_id,
      entry_date = now(),
      entered_by_practitioner_id  = OLD.entered_by_practitioner_id,
      device_id = 0,
      entry_mode = 1,
      table_id = 45,
      item_id = OLD.id;
  END$$
DELIMITER ;