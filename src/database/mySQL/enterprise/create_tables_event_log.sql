-- This script contains the event_log creation and table triggers for the Compass v1 upgrade
-- NOTE: run this AFTER the initial update scripts have completed

use enterprise_pseudo;  -- change this as required <-> use enterprise_pi;

drop table if exists event_log;

-- drop these existing triggers and recreate with the event_log in
drop trigger if exists after_patient_insert;
drop trigger if exists after_patient_update;
drop trigger if exists after_patient_delete;

drop trigger if exists after_person_insert;
drop trigger if exists after_person_update;
drop trigger if exists after_person_delete;
drop trigger if exists after_allergy_intolerance_insert;
drop trigger if exists after_allergy_intolerance_update;
drop trigger if exists after_allergy_intolerance_delete;
drop trigger if exists after_encounter_insert;
drop trigger if exists after_encounter_update;
drop trigger if exists after_encounter_delete;
drop trigger if exists after_episode_of_care_insert;
drop trigger if exists after_episode_of_care_update;
drop trigger if exists after_episode_of_care_delete;
drop trigger if exists after_flag_insert;
drop trigger if exists after_flag_update;
drop trigger if exists after_flag_delete;
drop trigger if exists after_location_insert;
drop trigger if exists after_location_update;
drop trigger if exists after_location_delete;
drop trigger if exists after_medication_order_insert;
drop trigger if exists after_medication_order_update;
drop trigger if exists after_medication_order_delete;
drop trigger if exists after_medication_statement_insert;
drop trigger if exists after_medication_statement_update;
drop trigger if exists after_medication_statement_delete;
drop trigger if exists after_observation_insert;
drop trigger if exists after_observation_update;
drop trigger if exists after_observation_delete;
drop trigger if exists after_organization_insert;
drop trigger if exists after_organization_update;
drop trigger if exists after_organization_delete;
drop trigger if exists after_practitioner_insert;
drop trigger if exists after_practitioner_update;
drop trigger if exists after_practitioner_delete;
drop trigger if exists after_procedure_request_insert;
drop trigger if exists after_procedure_request_update;
drop trigger if exists after_procedure_request_delete;
drop trigger if exists after_referral_request_insert;
drop trigger if exists after_referral_request_update;
drop trigger if exists after_referral_request_delete;
drop trigger if exists after_schedule_insert;
drop trigger if exists after_schedule_update;
drop trigger if exists after_schedule_delete;
drop trigger if exists after_appointment_insert;
drop trigger if exists after_appointment_update;
drop trigger if exists after_appointment_delete;
drop trigger if exists after_patient_contact_insert;
drop trigger if exists after_patient_contact_update;
drop trigger if exists after_patient_contact_delete;
drop trigger if exists after_patient_address_insert;
drop trigger if exists after_patient_address_update;
drop trigger if exists after_patient_address_delete;
drop trigger if exists after_registration_status_history_insert;
drop trigger if exists after_registration_status_history_update;
drop trigger if exists after_registration_status_history_delete;

CREATE TABLE event_log (
                           dt_change datetime(3) NOT NULL COMMENT 'date time the change was made to this DB',
                           change_type tinyint NOT NULL COMMENT 'type of transaction 0=insert, 1=update, 2=delete',
                           table_id tinyint NOT NULL COMMENT 'identifier of the table changed',
                           record_id bigint NOT NULL COMMENT 'id of the record changed'
);
-- note: purposefully no primary key or any other constraint

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

    -- and update the person table too
    CALL update_person_record(NEW.person_id, null);
END$$
DELIMITER ;

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

    -- and update the person table too
    CALL update_person_record(NEW.person_id, OLD.person_id);
END$$
DELIMITER ;

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

    -- and update the person table too
    CALL update_person_record(null, OLD.person_id);
END$$
DELIMITER ;


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
DELIMITER;

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
DELIMITER;

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

DELIMITER $$
CREATE TRIGGER after_diagnostic_order_insert
    AFTER INSERT ON diagnostic_order
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
                 21, -- diagnostic_order
                 NEW.id
             );
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_registration_status_history_insert
    AFTER INSERT ON registration_status_history
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
                 23, -- registration_status_history
                 NEW.id
             );
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_registration_status_history_update
    AFTER UPDATE ON registration_status_history
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
                 21, -- registration_status_history
                 NEW.id
             );
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_registration_status_history_delete
    AFTER DELETE ON registration_status_history
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
                 21, -- registration_status_history
                 OLD.id
             );
END$$
DELIMITER ;
