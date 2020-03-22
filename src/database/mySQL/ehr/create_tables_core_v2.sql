-- DDS VERSION 2 SCHEMA DRAFT

-- TODO:
-- TABLES FOR EDITED AND DELETED ROWS (ONE PER TABLE - ALLOWS FOR DELTAS AND AUDIT TRAIL)

use ehr;

CREATE TABLE organization
(
    id int NOT NULL,
    ods_code varchar(50),
    name varchar(255),
    type_id int, -- IM reference (i.e. GP Practices in England and Wales)
    postcode varchar(10),
    parent_organization_id int,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX organization_id ON organization (id);
CREATE INDEX fk_organization_parent_organization_id ON organization (parent_organization_id);
CREATE INDEX organization_ods_code ON organization (ods_code);

CREATE TABLE location (
    id int NOT NULL,
    name varchar(255),
    type_code varchar(50),
    type_desc varchar(255),
    postcode varchar(10),
    managing_organization_id int,
    PRIMARY KEY (id),
    CONSTRAINT fk_location_organisation_id FOREIGN KEY (managing_organization_id) REFERENCES organization (id)
);

CREATE UNIQUE INDEX location_id ON location (id);
CREATE INDEX fk_location_managing_organisation_id ON location (managing_organization_id);

CREATE TABLE practitioner
(
    id int NOT NULL,
    organization_id int NOT NULL,
    name varchar(1024),
    role_code varchar(50),
    role_desc varchar(255),
    type_id int, -- IM reference
    PRIMARY KEY (id),
    CONSTRAINT fk_practitioner_organisation_id FOREIGN KEY (organization_id) REFERENCES organization (id)
);

CREATE UNIQUE INDEX practitioner_id ON practitioner (id);

CREATE TABLE schedule
(
    id int NOT NULL,
    organization_id int NOT NULL,
    practitioner_id int,
    start_date date,
    type varchar(255),
    location varchar(255),
    name varchar(150),
    PRIMARY KEY (organization_id, id),
    CONSTRAINT fk_schedule_organization_id FOREIGN KEY (organization_id) REFERENCES organization (id)
);

CREATE UNIQUE INDEX schedule_id ON schedule (id);

CREATE TABLE person
(
    id int NOT NULL,
    organization_id int NOT NULL,
    title varchar(50),
    first_names varchar(255),
    last_name varchar(255),
    gender_type_id int, -- IM reference
    nhs_number varchar(255),
    date_of_birth date,
    date_of_death date,
    current_address_id int,
    ethnic_code_type_id int, -- IM reference
    registered_practice_organization_id int,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX person_id ON person (id);

CREATE TABLE patient
(
    id int NOT NULL,
    organization_id int NOT NULL,
    person_id int NOT NULL,
    title varchar(50),
    first_names varchar(255),
    last_name varchar(255),
    gender_type_id int, -- IM reference
    nhs_number varchar(255),
    date_of_birth date,
    date_of_death date,
    current_address_id int,
    ethnic_code_type_id int, -- IM reference
    registered_practice_organization_id int,
    mothers_nhs_number varchar(255),
    PRIMARY KEY (organization_id,person_id,id),
    CONSTRAINT fk_patient_organization_id FOREIGN KEY (organization_id) REFERENCES organization (id)
);

CREATE UNIQUE INDEX patient_id ON patient (id);
CREATE INDEX patient_person_id ON patient (person_id);

CREATE TABLE episode_of_care
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    registration_type_type_id int, -- IM reference
    registration_status_type_id int, -- IM reference
    date_registered date,
    date_registered_end date,
    usual_gp_practitioner_id int,
    PRIMARY KEY (organization_id,patient_id,id),
    CONSTRAINT fk_episode_of_care_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id),
    CONSTRAINT fk_episode_of_care_practitioner_id FOREIGN KEY (usual_gp_practitioner_id) REFERENCES practitioner (id)
);

CREATE UNIQUE INDEX episode_of_care_id ON episode_of_care (id);
CREATE INDEX episode_of_care_patient_id ON episode_of_care (patient_id);
CREATE INDEX episode_of_care_registration_type_type_id ON episode_of_care (registration_type_type_id);
CREATE INDEX episode_of_care_date_registered ON episode_of_care (date_registered);
CREATE INDEX episode_of_care_date_registered_end ON episode_of_care (date_registered_end);
CREATE INDEX episode_of_care_organization_id ON episode_of_care (organization_id);

CREATE TABLE appointment
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    practitioner_id int,
    schedule_id int,
    start_date datetime,
    planned_duration integer,
    actual_duration integer,
    appointment_status_type_id int, -- IM reference
    patient_wait integer,
    patient_delay integer,
    date_time_sent_in datetime,
    date_time_left datetime,
    source_id varchar(36),
    cancelled_date datetime,
    PRIMARY KEY (organization_id,patient_id,id),
    CONSTRAINT fk_appointment_organization_id FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_appointment_practitioner_id FOREIGN KEY (practitioner_id) REFERENCES practitioner (id)
);

CREATE UNIQUE INDEX appointment_id ON appointment (id);
CREATE INDEX appointment_patient_id ON appointment (patient_id);

CREATE TABLE encounter (
    id int NOT NULL AUTO_INCREMENT,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    clinical_effective_date datetime,
    type_id int, -- IM reference
    parent_encounter_id int,
    additional_data JSON,
    PRIMARY KEY (id)  -- ,
    -- CONSTRAINT fk_encounter_patient_id_organization_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
);

CREATE TABLE encounter_triple (
    id int NOT NULL AUTO_INCREMENT,
    encounter_id int NOT NULL,
    property_id int NOT NULL, -- IM reference (i.e. Admission method)
    value_id int NOT NULL, -- IM reference (i.e. Emergency admission)
    PRIMARY KEY (id)
);

CREATE TABLE encounter_section (
    id int NOT NULL,
    heading_id int NOT NULL, -- (i.e. history, examination)
    encounter_id int,
    parent_section_id int,
    PRIMARY KEY (id)
);

CREATE INDEX encounter_triple_value_id ON encounter_triple (value_id);


-- additional_data json field will include:
-- {
--   original term
--   original code
--   practitioner_id number
--   duration
--   date_precision_type_id number
--   appointment_id number
--   episode_of_care_id number
--   end_date_time string
-- }

CREATE TABLE medication_statement
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    clinical_effective_date datetime,
    type_id int, -- IM reference
    encounter_id int,
    additional_data JSON,
    PRIMARY KEY (id),
    CONSTRAINT fk_medication_statement_patient_id_organization_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
);


--   additional_data json field will include:
-- {
--   original term
--   original code
--   practitioner_id number
--   date_precision_type_id number
--   is_active boolean
--   cancellation_date string
--   dose string
--   quantity_value number
--   quantity_unit string
--   authorisation_type_type_id number
--   bnf_reference string
--   issue_method string
--   duration_days number
--   estimated_cost number
-- }

CREATE TABLE medication_order
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    clinical_effective_date datetime,
    medication_statement_id int,
    type_id int, -- IM reference
    encounter_id int,
    additional_data JSON,
    PRIMARY KEY (id),
    CONSTRAINT fk_medication_order_patient_id_organization_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
);

CREATE TABLE flag
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    effective_date date,
    date_precision_type_id int, -- IM reference
    is_active boolean NOT NULL,
    flag_text text,
    additional_data JSON,
    PRIMARY KEY (organization_id,patient_id,id),
    CONSTRAINT fk_flag_patient_id_organization_id FOREIGN KEY (patient_id, organization_id)
        REFERENCES patient (id, organization_id)
);

CREATE UNIQUE INDEX flag_id ON flag (id);
CREATE INDEX flag_patient_id ON flag (patient_id);

CREATE TABLE observation
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    clinical_effective_date datetime,
    type_id int, -- IM reference
    result_value double,
    encounter_id int,
    encounter_section_id int, -- maybe JSON
    parent_observation_id int,
    additional_data JSON,
    PRIMARY KEY (id)
);

CREATE INDEX ix_observation_1 ON observation (type_id,organization_id,patient_id,clinical_effective_date);
CREATE INDEX ix_observation_2 ON observation (type_id,organization_id,patient_id,clinical_effective_date,result_value);
CREATE INDEX ix_observation_3 ON observation (type_id,patient_id,clinical_effective_date);

CREATE TABLE observation_triple (
    id int NOT NULL,
    observation_id int NOT NULL,
    property_id int NOT NULL, -- IM reference
    value_id int NOT NULL, -- IM reference
    PRIMARY KEY (id)
);

CREATE INDEX observation_triple_value_id ON observation_triple (value_id);

-- additional_data json field will include:
-- {
--   original term
--   original code
--   original units
--   original value
--   practitioner_id number
--   date_precision_type_id number
--   result_value_units string
--   result_reference_range string
--   result_date string
--   result_text string
--   result_type_id number
--   is_problem boolean
--   problem_end_date string
--   parent_observation_id number
--   episodicity_type_id number
--   is_primary boolean
-- }

CREATE TABLE service_request
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    clinical_effective_date datetime,
    type_id int, -- IM reference
    encounter_id int,
    additional_data JSON,
    PRIMARY KEY (id),
    CONSTRAINT fk_service_request_patient_id_organization_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
);

-- additional_data json field will include:
-- {
--   original term
--   original code
--   practitioner_id number
--   date_precision_type_id number
--   requester_organization_id number
--   recipient_organization_id number
--   priority_type_id number
--   category_type_id number
--   status_type_id number
-- }

CREATE TABLE pseudo_id
(
    id int NOT NULL,
    type_id int, -- IM reference
    patient_id int NOT NULL,
    salt_key_name varchar(50) NOT NULL,
    pseudo_id varchar(255) NULL,
    PRIMARY KEY (patient_id, salt_key_name)
);

CREATE UNIQUE INDEX pseudo_id_id ON pseudo_id (id);
CREATE INDEX pseudo_id_pseudo_id ON pseudo_id (pseudo_id);

CREATE TABLE patient_uprn (
                              patient_id int,
                              organization_id int,
                              uprn int,
                              qualifier varchar(50),
                              algorithm varchar(255),
                              match varchar(255),
                              no_address boolean,
                              invalid_address boolean,
                              missing_postcode boolean,
                              invalid_postcode boolean,
                              PRIMARY KEY (organization_id,patient_id),
                              CONSTRAINT fk_patient_uprn_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
);

CREATE UNIQUE INDEX patient_uprn_id ON patient_uprn (patient_id);

CREATE TABLE patient_contact
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    use_type_id int COMMENT 'use of contact (e.g. mobile, home, work)',
    type_type_id int COMMENT 'type of contact (e.g. phone, email)',
    start_date date,
    end_date date,
    value varchar(255) COMMENT 'the actual phone number or email address',
    PRIMARY KEY (organization_id,id,patient_id),
    CONSTRAINT fk_patient_contact_patient_id_organisation_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
) COMMENT 'stores contact details (e.g. phone) for patients';

CREATE TABLE patient_address
(
    id int NOT NULL,
    organization_id int NOT NULL,
    patient_id int NOT NULL,
    address_line_1 varchar(255),
    address_line_2 varchar(255),
    address_line_3 varchar(255),
    address_line_4 varchar(255),
    city varchar(255),
    postcode varchar(10),
    use_type_id int NOT NULL COMMENT 'use of address (e.g. home, temporary)',
    start_date date,
    end_date date,
    lsoa_2001_code varchar(9),
    lsoa_2011_code varchar(9),
    msoa_2001_code varchar(9),
    msoa_2011_code varchar(9),
    ward_code varchar(9),
    local_authority_code varchar(9),
    PRIMARY KEY (organization_id,id,patient_id),
    CONSTRAINT fk_patient_address_patient_id_organization_id FOREIGN KEY (patient_id, organization_id) REFERENCES patient (id, organization_id)
) COMMENT 'stores address details for patients';

CREATE TABLE event_log (
                           dt_change datetime(3) NOT NULL COMMENT 'date time the change was made to this DB',
                           change_type tinyint NOT NULL COMMENT 'type of transaction 0=insert, 1=update, 2=delete',
                           table_id tinyint NOT NULL COMMENT 'identifier of the table changed',
                           record_id int NOT NULL COMMENT 'id of the record changed'
);

-- TRIGGERS AND STORED PROCEDURES

DELIMITER //
CREATE PROCEDURE update_person_record_2(
    IN _new_person_id int
)
BEGIN

    DECLARE _best_patient_id int DEFAULT -1;

    SET _best_patient_id = (
        SELECT id
        FROM
            (SELECT
                 p.id,
                 IF (e.registration_type_type_id = 1335267, 1, 0) as registration_type_rank, -- if reg type = GMS then up-rank
                 IF (e.registration_status_type_id is null or e.registration_status_type_id not in (1335283, 1335284, 1335285), 1, 0) as registration_status_rank, -- if pre-registered status, then down-rank
                 IF (p.date_of_death is not null, 1, 0) as death_rank, --  records is a date of death more likely to be actively used, so up-vote
                 IF (e.date_registered_end is null, '9999-12-31', e.date_registered_end) as date_registered_end_sortable -- up-vote non-ended ones
             FROM patient p
                      LEFT OUTER JOIN episode_of_care e
                                      ON e.organization_id = p.organization_id
                                          AND e.patient_id = p.id
             WHERE
                     p.person_id = _new_person_id
             ORDER BY
                 registration_status_rank desc, -- avoid pre-registered records if possible
                 death_rank desc, -- records marked as deceased are more likely to be used than ones not
                 registration_type_rank desc, -- prefer GMS registrations over others
                 date_registered desc, -- want the most recent registration
                 date_registered_end_sortable desc
             LIMIT 1) AS tmp
    );

    REPLACE INTO person
    SELECT person_id, organization_id, title, first_names, last_name, gender_type_id, nhs_number, date_of_birth, date_of_death, current_address_id, ethnic_code_type_id, registered_practice_organization_id
    FROM patient
    WHERE id = _best_patient_id;

END //
DELIMITER ;


DELIMITER //
CREATE PROCEDURE update_person_record(
    IN _new_person_id int,
    IN _old_person_id int
)
BEGIN

    DECLARE _patients_remaning INT DEFAULT 1;

    IF (_new_person_id IS NOT NULL) THEN
        CALL update_person_record_2(_new_person_id);
    END IF;

    IF (_old_person_id IS NOT NULL) THEN

        SET _patients_remaning = (select count(1) from patient where person_id = _old_person_id);

        IF (_patients_remaning = 0) THEN
            DELETE FROM person
            WHERE id = _old_person_id;
        ELSE
            CALL update_person_record_2(_old_person_id);
        END IF;

    END IF;


END //
DELIMITER ;

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
DELIMITER ;



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
CREATE TRIGGER after_service_request_insert
    AFTER INSERT ON service_request
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
                 14, -- service_request
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_service_request_update
    AFTER UPDATE ON service_request
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
                 14, -- service_request
                 NEW.id
             );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_service_request_delete
    AFTER DELETE ON service_request
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
                 14, -- service_request
                 OLD.id
             );
END$$
DELIMITER ;




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

-- table to store service level mapping from local Id source to core table int for v2 core publishing db.
CREATE TABLE core_id_map
(
    service_id char(36),
    core_table tinyint NOT NULL COMMENT 'ID of the target table this ID is for',
    core_id int NOT NULL COMMENT 'unique ID allocated for the subscriber DB',
    source_id varchar(250) NOT NULL COMMENT 'Source ID (e.g. The inbound data reference) that this ID is mapped from',
    CONSTRAINT pk_subscriber_id_map PRIMARY KEY (service_id, source_id, core_table)
);
-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_core_id_map_auto_increment
    ON core_id_map (core_id);

ALTER TABLE core_id_map MODIFY COLUMN core_id int auto_increment;
