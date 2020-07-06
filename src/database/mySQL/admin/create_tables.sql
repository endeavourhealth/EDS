USE admin;

drop trigger if exists after_service_insert;
drop trigger if exists after_service_update;
drop trigger if exists after_service_delete;
DROP TABLE IF EXISTS dependency_type;
DROP TABLE IF EXISTS item_type;
DROP TABLE IF EXISTS audit;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS item_dependency;
DROP TABLE IF EXISTS active_item;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS organisation; -- no longer used, but keep the delete
DROP TABLE IF EXISTS patient_cohort; -- no longer used, but keep the delete
DROP TABLE IF EXISTS link_distributor_populator;
DROP TABLE IF EXISTS link_distributor_task_list;


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
    xml_content longtext,
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
	publisher_config_name text,
	notes text,
	postcode varchar(50),
	ccg_code varchar(50),
	organisation_type varchar(50),
	alias varchar(250) COMMENT 'secondary name',
	tags json COMMENT 'custom tags for filtering and info',
  CONSTRAINT pk_service PRIMARY KEY (id)
);

-- would ideally like this index to be unique, to prevent us inserting the same service
-- more than once, but the UPSERT syntax means that this wouldn't fail the SQL but cause
-- an update on the existing service
CREATE INDEX ix_service_local_id
ON service (local_id);

/*

CREATE TABLE organisation
(
    id varchar(36),
    name varchar(250),
    national_id varchar(50),
	services text,
    CONSTRAINT pk_organisation PRIMARY KEY (id)
);

-- would ideally like this index to be unique, to prevent us inserting the same organization
-- more than once, but the UPSERT syntax means that this wouldn't fail the SQL but cause
-- an update on the existing service
CREATE INDEX ix_organisation_national_identifier
ON organisation (national_id);
*/

/*
 CREATE TABLE patient_cohort (
    protocol_id varchar(36),
    service_id varchar(36),
    nhs_number varchar(10),
    inserted datetime,
    in_cohort boolean,
    CONSTRAINT pk_patient_cohort PRIMARY KEY (protocol_id, service_id, nhs_number, inserted DESC)
);*/

CREATE TABLE link_distributor_populator
(
    patient_id varchar(36),
    nhs_number varchar(10),
    date_of_birth date,
    done boolean,
    CONSTRAINT pk_link_distributor_populator_patient_id PRIMARY KEY (patient_id)
);

CREATE TABLE link_distributor_task_list
(
	config_name varchar(100) not null,
    process_status tinyint not null comment '0 - to do, 1 - in progress, 2 - complete, 3 - error',
    CONSTRAINT pk_link_distributor_task_list_config_name PRIMARY KEY (config_name)
);



CREATE TABLE service_history
(
	id varchar(36),
	name varchar(250),
	local_id varchar(50),
	endpoints text,
	organisations text,
	publisher_config_name text,
	notes text,
	postcode varchar(50),
	ccg_code varchar(50),
	organisation_type varchar(50),
	alias varchar(250) COMMENT 'secondary name',
	tags json COMMENT 'custom tags for filtering and info',
  transaction_type varchar(100) NOT NULL,
  dt_changed datetime(3) NOT NULL
) COMMENT 'stores history of updates to the service table';





DELIMITER $$
CREATE TRIGGER after_service_insert
  AFTER INSERT ON service
  FOR EACH ROW
  BEGIN
    INSERT INTO service_history (
		id,
		name,
		local_id,
		endpoints,
		organisations,
		publisher_config_name,
		notes,
		postcode,
		ccg_code,
		organisation_type,
		alias,
		tags,
        transaction_type,
        dt_changed
    ) VALUES (
		NEW.id,
		NEW.name,
		NEW.local_id,
		NEW.endpoints,
		NEW.organisations,
		NEW.publisher_config_name,
		NEW.notes,
		NEW.postcode,
		NEW.ccg_code,
		NEW.organisation_type,
		NEW.alias,
		NEW.tags,
        'insert',
        now()
    );
  END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_service_update
  AFTER UPDATE ON service
  FOR EACH ROW
  BEGIN
    INSERT INTO service_history (
		id,
		name,
		local_id,
		endpoints,
		organisations,
		publisher_config_name,
		notes,
		postcode,
		ccg_code,
		organisation_type,
		alias,
		tags,
        transaction_type,
        dt_changed
    ) VALUES (
		NEW.id,
		NEW.name,
		NEW.local_id,
		NEW.endpoints,
		NEW.organisations,
		NEW.publisher_config_name,
		NEW.notes,
		NEW.postcode,
		NEW.ccg_code,
		NEW.organisation_type,
		NEW.alias,
		NEW.tags,
        'update',
        now()
    );
  END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER after_service_delete
  AFTER DELETE ON service
  FOR EACH ROW
  BEGIN
    INSERT INTO service_history (
		id,
		name,
		local_id,
		endpoints,
		organisations,
		publisher_config_name,
		notes,
		postcode,
		ccg_code,
		organisation_type,
		alias,
		tags,
        transaction_type,
        dt_changed
    ) VALUES (
		OLD.id,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
        'delete',
        now()
    );
  END$$
DELIMITER ;
