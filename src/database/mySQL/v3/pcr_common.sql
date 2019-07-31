drop database if exists pcr_common;
create database pcr_common;

use pcr_common;

DROP TABLE IF EXISTS table_id;
DROP TABLE IF EXISTS date_precision;
DROP TABLE IF EXISTS event_log_transaction_type;

create table table_id (
	id tinyint,
    `table_name` varchar(255),
    CONSTRAINT pk_table_id PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'stores numeric ID used for each table';

-- insert into table_id
INSERT INTO table_id VALUES (1, 'allergy_intolerance');
INSERT INTO table_id VALUES (2, 'appointment');
INSERT INTO table_id VALUES (3, 'diagnostic_order');
INSERT INTO table_id VALUES (4, 'encounter');
INSERT INTO table_id VALUES (5, 'encounter_status');
INSERT INTO table_id VALUES (6, 'encounter_location');
INSERT INTO table_id VALUES (7, 'episode_of_care');
INSERT INTO table_id VALUES (8, 'gp_registration');
INSERT INTO table_id VALUES (9, 'gp_registration_status');
INSERT INTO table_id VALUES (10, 'flag');
INSERT INTO table_id VALUES (11, 'location');
INSERT INTO table_id VALUES (12, 'location_telecom');
INSERT INTO table_id VALUES (13, 'medication_order');
INSERT INTO table_id VALUES (14, 'medication_statement');
INSERT INTO table_id VALUES (15, 'observation');
INSERT INTO table_id VALUES (16, 'observation_result');
INSERT INTO table_id VALUES (17, 'observation_immunisation');
INSERT INTO table_id VALUES (18, 'observation_procedure');
INSERT INTO table_id VALUES (19, 'observation_family_history');
INSERT INTO table_id VALUES (20, 'observation_condition');
INSERT INTO table_id VALUES (21, 'organisation');
INSERT INTO table_id VALUES (22, 'organisation_telecom');
INSERT INTO table_id VALUES (23, 'patient');
INSERT INTO table_id VALUES (24, 'patient_name');
INSERT INTO table_id VALUES (25, 'patient_address');
INSERT INTO table_id VALUES (26, 'patient_telecom');
INSERT INTO table_id VALUES (27, 'patient_relationship');
INSERT INTO table_id VALUES (28, 'patient_relationship_telecom');
INSERT INTO table_id VALUES (29, 'practitioner');
INSERT INTO table_id VALUES (30, 'procedure_request');
INSERT INTO table_id VALUES (31, 'questionnaire_response');
INSERT INTO table_id VALUES (32, 'questionnaire_response_question');
INSERT INTO table_id VALUES (33, 'referral_request');
INSERT INTO table_id VALUES (34, 'appointment_schedule');
INSERT INTO table_id VALUES (35, 'practitioner_identifier');
INSERT INTO table_id VALUES (36, 'practitioner_telecom');
INSERT INTO table_id VALUES (37, 'patient_identifier');


create table date_precision (
	id tinyint,
    precision_desc varchar(255),
    CONSTRAINT pk_date_precision PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'lookup for date precisions';

-- date precisions aligned with FHIR
INSERT INTO date_precision VALUES (1, 'minute');
INSERT INTO date_precision VALUES (2, 'day');
INSERT INTO date_precision VALUES (3, 'month');
INSERT INTO date_precision VALUES (4, 'year');
INSERT INTO date_precision VALUES (5, 'unknown');


create table event_log_transaction_type (
	id tinyint,
  transaction_desc varchar(255),
  CONSTRAINT pk_event_log_transaction_type PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'lookup for date precisions';

INSERT INTO event_log_transaction_type VALUES (1, 'insert');
INSERT INTO event_log_transaction_type VALUES (2, 'update');
INSERT INTO event_log_transaction_type VALUES (3, 'delete');

-- procedure to look up table ID from table name (to avoid hard-coding table IDs everywhere)
DROP PROCEDURE IF EXISTS `get_table_id`;

DELIMITER //
CREATE PROCEDURE get_table_id(
	IN _table_name varchar(255)
)
BEGIN

    select id
    from table_id
    where `table_name` = _table_name;

END //
DELIMITER ;

-- example
-- call get_table_id('patient_address');