USE admin;

DROP TABLE IF EXISTS dependency_type;
DROP TABLE IF EXISTS item_type;
DROP TABLE IF EXISTS audit;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS item_dependency;
DROP TABLE IF EXISTS active_item;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS organisation;
DROP TABLE IF EXISTS patient_cohort;


CREATE TABLE dependency_type
(
    id int,
    description varchar(50),
    CONSTRAINT pk_dependency_type PRIMARY KEY (id)
);

CREATE TABLE item_type
(
    id int,
    description varchar(50),
    CONSTRAINT pk_item_type PRIMARY KEY (id)
);


CREATE TABLE audit
(
    id varchar(36),
    organisation_id varchar(36),
    timestamp datetime,
    end_user_id varchar(36),
    CONSTRAINT pk_item_type PRIMARY KEY (id, organisation_id, timestamp)
);

CREATE INDEX ix_audit_organisation_timestamp_id
ON audit (organisation_id, timestamp, id);


CREATE TABLE item
(
    id varchar(36),
    audit_id varchar(36),
    xml_content text,
    title varchar(250),
    description varchar(500),
    is_deleted boolean,
    CONSTRAINT pk_item PRIMARY KEY (id, audit_id)
);


CREATE TABLE item_dependency
(
    item_id varchar(36),
    audit_id varchar(36),
    dependent_item_id varchar(36),
    dependency_type_id int,
    CONSTRAINT pk_item_dependency PRIMARY KEY (item_id, audit_id, dependent_item_id, dependency_type_id)
);

CREATE INDEX ix_item_dependency_item_audit_type_dependent
ON item_dependency (item_id, audit_id, dependency_type_id, dependent_item_id);

CREATE INDEX ix_item_dependency_dependent_type_item_audit
ON item_dependency (dependent_item_id, dependency_type_id, item_id, audit_id);


CREATE TABLE active_item
(
    item_id varchar(36),
    audit_id varchar(36),
    item_type_id int,
    is_deleted boolean,
    organisation_id varchar(36),
    CONSTRAINT pk_active_item PRIMARY KEY (item_id)
);

CREATE INDEX ix_active_item_organisation_type_deleted
ON active_item (organisation_id, item_type_id, is_deleted);

CREATE INDEX ix_active_item_type_deleted_organisation
ON active_item (item_type_id, is_deleted, organisation_id);

CREATE INDEX ix_active_item_organisation_deleted_type
ON active_item (organisation_id, is_deleted, item_type_id);

CREATE INDEX ix_active_item_item_organisation_type
ON active_item (item_id, organisation_id, item_type_id);

CREATE INDEX ix_active_item_audit_organisation_type
ON active_item (audit_id, organisation_id, item_type_id);


CREATE TABLE service
(
	id varchar(36),
	name varchar(250),
	local_id varchar(50),
	endpoints text,
	organisations text,
    CONSTRAINT pk_service PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ix_service_local_id
ON service (local_id, id);


CREATE TABLE organisation
(
    id varchar(36),
    name varchar(250),
    national_id varchar(50),
	services text,
    CONSTRAINT pk_organisation PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ix_organisation_national_identifier
ON organisation (national_id, id);


 CREATE TABLE patient_cohort (
    protocol_id varchar(36),
    service_id varchar(36),
    nhs_number varchar(10),
    inserted datetime,
    in_cohort boolean,
    CONSTRAINT pk_patient_cohort PRIMARY KEY (protocol_id, service_id, nhs_number, inserted DESC)
);



