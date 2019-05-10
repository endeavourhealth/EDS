

drop trigger if exists after_patient_insert;

DELIMITER $$
CREATE TRIGGER after_patient_insert
  AFTER INSERT ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        2, -- patient
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_patient_update;

DELIMITER $$
CREATE TRIGGER after_patient_update
  AFTER UPDATE ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        2, -- patient
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_patient_delete;

DELIMITER $$
CREATE TRIGGER after_patient_delete
  AFTER DELETE ON patient
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        2, -- patient
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_person_insert;

DELIMITER $$
CREATE TRIGGER after_person_insert
  AFTER INSERT ON person
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        3, -- person
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_person_update;

DELIMITER $$
CREATE TRIGGER after_person_update
  AFTER UPDATE ON person
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        3, -- person
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_person_delete;

DELIMITER $$
CREATE TRIGGER after_person_delete
  AFTER DELETE ON person
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        3, -- person
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_allergy_intolerance_insert;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_insert
  AFTER INSERT ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        4, -- allergy_intolerance
        NEW.id
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
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        4, -- allergy_intolerance
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_allergy_intolerance_delete;

DELIMITER $$
CREATE TRIGGER after_allergy_intolerance_delete
  AFTER DELETE ON allergy_intolerance
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        4, -- allergy_intolerance
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_encounter_insert;

DELIMITER $$
CREATE TRIGGER after_encounter_insert
  AFTER INSERT ON encounter
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        5, -- encounter
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_encounter_update;

DELIMITER $$
CREATE TRIGGER after_encounter_update
  AFTER UPDATE ON encounter
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        5, -- encounter
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_encounter_delete;

DELIMITER $$
CREATE TRIGGER after_encounter_delete
  AFTER DELETE ON encounter
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        5, -- encounter
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_episode_of_care_insert;

DELIMITER $$
CREATE TRIGGER after_episode_of_care_insert
  AFTER INSERT ON episode_of_care
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        6, -- episode_of_care
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_episode_of_care_update;

DELIMITER $$
CREATE TRIGGER after_episode_of_care_update
  AFTER UPDATE ON episode_of_care
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        6, -- episode_of_care
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_episode_of_care_delete;

DELIMITER $$
CREATE TRIGGER after_episode_of_care_delete
  AFTER DELETE ON episode_of_care
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        6, -- episode_of_care
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_flag_insert;

DELIMITER $$
CREATE TRIGGER after_flag_insert
  AFTER INSERT ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        7, -- flag
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_flag_update;

DELIMITER $$
CREATE TRIGGER after_flag_update
  AFTER UPDATE ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        7, -- flag
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_flag_delete;

DELIMITER $$
CREATE TRIGGER after_flag_delete
  AFTER DELETE ON flag
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        7, -- flag
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_location_insert;

DELIMITER $$
CREATE TRIGGER after_location_insert
  AFTER INSERT ON location
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        8, -- location
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_location_update;

DELIMITER $$
CREATE TRIGGER after_location_update
  AFTER UPDATE ON location
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        8, -- location
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_location_delete;

DELIMITER $$
CREATE TRIGGER after_location_delete
  AFTER DELETE ON location
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        8, -- location
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_medication_order_insert;

DELIMITER $$
CREATE TRIGGER after_medication_order_insert
  AFTER INSERT ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        9, -- medication_order
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_medication_order_update;

DELIMITER $$
CREATE TRIGGER after_medication_order_update
  AFTER UPDATE ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        9, -- medication_order
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_medication_order_delete;

DELIMITER $$
CREATE TRIGGER after_medication_order_delete
  AFTER DELETE ON medication_order
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        9, -- medication_order
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_medication_statement_insert;

DELIMITER $$
CREATE TRIGGER after_medication_statement_insert
  AFTER INSERT ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        10, -- medication_statement
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_medication_statement_update;

DELIMITER $$
CREATE TRIGGER after_medication_statement_update
  AFTER UPDATE ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        10, -- medication_statement
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_medication_statement_delete;

DELIMITER $$
CREATE TRIGGER after_medication_statement_delete
  AFTER DELETE ON medication_statement
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        10, -- medication_statement
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_observation_insert;

DELIMITER $$
CREATE TRIGGER after_observation_insert
  AFTER INSERT ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        11, -- observation
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_observation_update;

DELIMITER $$
CREATE TRIGGER after_observation_update
  AFTER UPDATE ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        11, -- observation
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_observation_delete;

DELIMITER $$
CREATE TRIGGER after_observation_delete
  AFTER DELETE ON observation
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        11, -- observation
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_organization_insert;

DELIMITER $$
CREATE TRIGGER after_organization_insert
  AFTER INSERT ON organization
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        12, -- organization
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_organization_update;

DELIMITER $$
CREATE TRIGGER after_organization_update
  AFTER UPDATE ON organization
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        12, -- organization
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_organization_delete;

DELIMITER $$
CREATE TRIGGER after_organization_delete
  AFTER DELETE ON organization
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        12, -- organization
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_practitioner_insert;

DELIMITER $$
CREATE TRIGGER after_practitioner_insert
  AFTER INSERT ON practitioner
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        13, -- practitioner
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_practitioner_update;

DELIMITER $$
CREATE TRIGGER after_practitioner_update
  AFTER UPDATE ON practitioner
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        13, -- practitioner
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_practitioner_delete;

DELIMITER $$
CREATE TRIGGER after_practitioner_delete
  AFTER DELETE ON practitioner
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        13, -- practitioner
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_procedure_request_insert;

DELIMITER $$
CREATE TRIGGER after_procedure_request_insert
  AFTER INSERT ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        14, -- procedure_request
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_procedure_request_update;

DELIMITER $$
CREATE TRIGGER after_procedure_request_update
  AFTER UPDATE ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        14, -- procedure_request
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_procedure_request_delete;

DELIMITER $$
CREATE TRIGGER after_procedure_request_delete
  AFTER DELETE ON procedure_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        14, -- procedure_request
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_pseudo_id_insert;

DELIMITER $$
CREATE TRIGGER after_pseudo_id_insert
  AFTER INSERT ON pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        15, -- pseudo_id
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_pseudo_id_update;

DELIMITER $$
CREATE TRIGGER after_pseudo_id_update
  AFTER UPDATE ON pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        15, -- pseudo_id
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_pseudo_id_delete;

DELIMITER $$
CREATE TRIGGER after_pseudo_id_delete
  AFTER DELETE ON pseudo_id
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        15, -- pseudo_id
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_referral_request_insert;

DELIMITER $$
CREATE TRIGGER after_referral_request_insert
  AFTER INSERT ON referral_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        16, -- referral_request
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_referral_request_update;

DELIMITER $$
CREATE TRIGGER after_referral_request_update
  AFTER UPDATE ON referral_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        16, -- referral_request
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_referral_request_delete;

DELIMITER $$
CREATE TRIGGER after_referral_request_delete
  AFTER DELETE ON referral_request
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        16, -- referral_request
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_schedule_insert;

DELIMITER $$
CREATE TRIGGER after_schedule_insert
  AFTER INSERT ON schedule
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        17, -- schedule
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_schedule_update;

DELIMITER $$
CREATE TRIGGER after_schedule_update
  AFTER UPDATE ON schedule
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        17, -- schedule
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_schedule_delete;

DELIMITER $$
CREATE TRIGGER after_schedule_delete
  AFTER DELETE ON schedule
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        17, -- schedule
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_appointment_insert;

DELIMITER $$
CREATE TRIGGER after_appointment_insert
  AFTER INSERT ON appointment
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        18, -- appointment
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_appointment_update;

DELIMITER $$
CREATE TRIGGER after_appointment_update
  AFTER UPDATE ON appointment
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        18, -- appointment
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_appointment_delete;

DELIMITER $$
CREATE TRIGGER after_appointment_delete
  AFTER DELETE ON appointment
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        18, -- appointment
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_patient_contact_insert;

DELIMITER $$
CREATE TRIGGER after_patient_contact_insert
  AFTER INSERT ON patient_contact
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        19, -- patient_contact
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_patient_contact_update;

DELIMITER $$
CREATE TRIGGER after_patient_contact_update
  AFTER UPDATE ON patient_contact
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        19, -- patient_contact
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_patient_contact_delete;

DELIMITER $$
CREATE TRIGGER after_patient_contact_delete
  AFTER DELETE ON patient_contact
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        19, -- patient_contact
        OLD.id
    );
  END$$
DELIMITER ;



drop trigger if exists after_patient_address_insert;

DELIMITER $$
CREATE TRIGGER after_patient_address_insert
  AFTER INSERT ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        0, -- insert
        20, -- patient_address
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_patient_address_update;

DELIMITER $$
CREATE TRIGGER after_patient_address_update
  AFTER UPDATE ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        1, -- update
        20, -- patient_address
        NEW.id
    );
  END$$
DELIMITER ;

drop trigger if exists after_patient_address_delete;

DELIMITER $$
CREATE TRIGGER after_patient_address_delete
  AFTER DELETE ON patient_address
  FOR EACH ROW
  BEGIN
    INSERT INTO event_log (
		dt_change,
        change_type,
        table_id,
        record_id
    ) VALUES (
		now(3), -- current time inc ms
        2, -- delete
        20, -- patient_address
        OLD.id
    );
  END$$
DELIMITER ;