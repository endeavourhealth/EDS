
drop database if exists pcr_admin;
create database pcr_admin;

use pcr_admin;

DROP TABLE IF EXISTS event_log;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS location_telecom;
DROP TABLE IF EXISTS organisation;
DROP TABLE IF EXISTS organisation_telecom;
DROP TABLE IF EXISTS practitioner;
DROP TABLE IF EXISTS practitioner_identifier;
DROP TABLE IF EXISTS practitioner_telecom;
DROP TABLE IF EXISTS appointment_schedule;

create table event_log (
	id int NOT NULL,
	table_id tinyint NOT NULL,
    record_id int NOT NULL,
    event_timestamp datetime(3) NOT NULL COMMENT 'datetime 3 gives us precision down to the millisecond',
    event_log_transaction_type_id tinyint NOT NULL COMMENT '0 = insert, 1 = update, 2 = delete',
    batch_id char(36) COMMENT 'UUID referring to the audit.exchange_batch table',
    service_id char(36) COMMENT 'UUID referring to the admin.service table',
    patient_id int COMMENT 'duplication of patient_if (if present) on audited table',
    concept_id int COMMENT 'duplication of main concept ID (if present) on audited table',
	CONSTRAINT pk_event_log PRIMARY KEY (id)
);

ALTER TABLE event_log MODIFY COLUMN id INT auto_increment;


create table location (
	id int NOT NULL,
    location_name varchar(255),
    address_line_1 varchar(255),
    address_line_2 varchar(255),
    address_line_3 varchar(255),
    address_city varchar(255),
    address_county varchar(255),
    address_postcode varchar(255),
    address_mapped_id varchar(255) COMMENT 'derived ID of the "national" record of this address',
    parent_location_id int,
    managing_organisation_id int,
	additional_data JSON COMMENT 'stores main contact name, is active, location type, physical type, open date, close date',
	CONSTRAINT pk_location PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 11';


CREATE UNIQUE INDEX uix_id ON location (id);



create table location_telecom (
	id int NOT NULL,
    location_id int NOT NULL,
    telecom_concept_id int COMMENT 'IM concept for telephone, fax, email etc.',
    telecom_value varchar(255),
	additional_data JSON COMMENT '',
	CONSTRAINT pk_location PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 12';

CREATE UNIQUE INDEX uix_id ON location_telecom (id);




-- sticking with UK English
create table organisation (
	id int NOT NULL,
    organisation_name varchar(255),
    address_line_1 varchar(255),
    address_line_2 varchar(255),
    address_line_3 varchar(255),
    address_city varchar(255),
    address_county varchar(255),
    address_postcode varchar(255),
    address_mapped_id varchar(255) COMMENT 'derived ID of the "national" record of this address',
    parent_organisation_id int,
    main_location_id int COMMENT 'refers to location table',
    type_concept_id int COMMENT 'IM concept for org type e.g. GP practice',
	additional_data JSON COMMENT 'stores is active, open date, close date',
	CONSTRAINT pk_organisation PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 21';

CREATE UNIQUE INDEX uix_id ON organisation (id);


create table organisation_telecom (
	id int NOT NULL,
    organisation_id int NOT NULL,
    telecom_concept_id int COMMENT 'IM concept for telephone, fax, email etc.',
    telecom_value varchar(255),
	additional_data JSON COMMENT '',
	CONSTRAINT pk_organisation_telecom PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 22';

CREATE UNIQUE INDEX uix_id ON organisation_telecom (id);

create table practitioner (
	id int NOT NULL,
    name_prefix varchar(255) COMMENT 'i.e. title',
    given_names varchar(255) COMMENT 'first and middle names',
    family_names varchar(255) COMMENT 'last names',
    name_suffix varchar(255) COMMENT 'honourifics that go after the name',
    start_date date,
    end_date date,
    role_concept_id int COMMENT 'IM concept for the person role type',
    organisation_id int COMMENT 'refers to organisation table',
	additional_data JSON COMMENT '',
	CONSTRAINT pk_patient_relationship_telecom PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 29';

CREATE UNIQUE INDEX uix_id ON practitioner (id);

create table practitioner_identifier (
	id int NOT NULL,
	practitioner_id int NOT NULL,
	identifier_type_concept_id int NOT NULL COMMENT 'IM concept giving the type of this identifier e.g. GMC number',
	identifier_value varchar(255) NOT NULL,
	additional_data JSON COMMENT '',
	CONSTRAINT pk_practitioner_identifier PRIMARY KEY (practitioner_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 35';

CREATE UNIQUE INDEX uix_id ON practitioner_identifier (id);


create table practitioner_telecom (
	id int NOT NULL,
	practitioner_id int NOT NULL,
  telecom_concept_id int COMMENT 'IM concept for telephone, fax, email etc.',
  telecom_value varchar(255),
	additional_data JSON COMMENT '',
	CONSTRAINT pk_practitioner_telecom PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 36';

CREATE UNIQUE INDEX uix_id ON practitioner_telecom (practitioner_id, id);





-- diverting from FHIR "schedule" to avoid using SQL keywords
create table appointment_schedule (
	id int NOT NULL,
    location_id int COMMENT 'refers to location table',
    schedule_name varchar(255),
    schedule_type_concept_id int COMMENT 'IM concept for schedule type e.g. telephone appts',
    start_date datetime,
    end_date datetime,
    primary_practitioner_id int COMMENT 'refers to practitioner table',
    created_by_practitioner_id int COMMENT 'refers to practitioner table',
    created_date datetime,
	additional_data JSON COMMENT 'stores location type, comments, additional practitioners',
	CONSTRAINT pk_appointment_schedule PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'table ID = 34';

CREATE UNIQUE INDEX uix_id ON appointment_schedule (id);


