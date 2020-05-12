USE ehr_??; -- we can have multiple EHR databases

drop trigger if exists after_resource_current_insert;
drop trigger if exists after_resource_current_update;
drop trigger if exists after_resource_current_delete;

DROP TABLE IF EXISTS resource_history;
DROP TABLE IF EXISTS resource_current;

CREATE TABLE resource_history (
  service_id char(36),
  system_id char(36),
  resource_type varchar(50),
  resource_id char(36),
  created_at timestamp,
  patient_id varchar(36),
  resource_data mediumtext,
  resource_checksum bigint,
  is_deleted boolean,
  exchange_batch_id char(36),
  version char(36),
  CONSTRAINT pk_resource_history PRIMARY KEY (resource_id, resource_type, created_at DESC, version)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- index used to get data out for subscriber feeds
CREATE INDEX ix_resource_history_batch_id
ON resource_history (exchange_batch_id, resource_type, resource_id, created_at);

CREATE TABLE resource_current (
  service_id char(36),
  system_id char(36),
  resource_type varchar(50),
  resource_id char(36),
  updated_at datetime,
  patient_id varchar(36),
  resource_data mediumtext,
  resource_checksum bigint,
  resource_metadata varchar(1000),
  CONSTRAINT pk_resource_current PRIMARY KEY (service_id, patient_id, resource_type, resource_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- index used to retrieve specific resources for a service
/*CREATE INDEX ix_resource_current_service_type_id
ON resource_current (service_id, resource_type, resource_id);*/

/*CREATE INDEX ix_resource_current_service_type_id_system
ON resource_current (service_id, resource_type, resource_id, system_id);*/

/*CREATE INDEX ix_resource_current_type_id
ON resource_current (resource_type, resource_id);*/

-- index used to retrieve current version of a resource and get its checksum
CREATE INDEX ix_resource_current_id_type_checksum
ON resource_current (resource_id, resource_type, resource_checksum);

-- index used to prevent resources being duplicated if the patient ID changed
CREATE UNIQUE INDEX uix_resource_current_id_type
ON resource_current (resource_id, resource_type);

-- trigger to update resource_history when a new resource_current is inserted
DELIMITER $$
CREATE TRIGGER after_resource_current_insert
  AFTER INSERT ON resource_current
  FOR EACH ROW
  BEGIN
    INSERT INTO resource_history (
		service_id,
        system_id,
        resource_type,
        resource_id,
        created_at,
        patient_id,
        resource_data,
        resource_checksum,
        is_deleted,
        exchange_batch_id,
        version
    ) VALUES (
		NEW.service_id,
        NEW.system_id,
        NEW.resource_type,
        NEW.resource_id,
        NEW.updated_at,
        NEW.patient_id,
        null, -- the JSON field is for the OLD record, hence null
        null,
        IF (NEW.resource_data is null, true, false),
        SUBSTRING(NEW.resource_metadata, 1, 36), -- batch_id is put into this field to make this work
        SUBSTRING(NEW.resource_metadata, 38, 36) -- version is put into this field to make this work
    );
  END$$
DELIMITER ;

-- trigger to update resource_history when resource_current is updated (including logical deletes)
DELIMITER $$
CREATE TRIGGER after_resource_current_update
  AFTER UPDATE ON resource_current
  FOR EACH ROW
  BEGIN
    INSERT INTO resource_history (
		service_id,
        system_id,
        resource_type,
        resource_id,
        created_at,
        patient_id,
        resource_data,
        resource_checksum,
        is_deleted,
        exchange_batch_id,
        version
    ) VALUES (
		NEW.service_id,
        NEW.system_id,
        NEW.resource_type,
        NEW.resource_id,
        NEW.updated_at,
        NEW.patient_id,
        OLD.resource_data,
        OLD.resource_checksum,
        IF (NEW.resource_data is null, true, false),
        SUBSTRING(NEW.resource_metadata, 1, 36), -- batch_id is put into this field to make this work
        SUBSTRING(NEW.resource_metadata, 38, 36) -- version is put into this field to make this work
    );
  END$$
DELIMITER ;

-- trigger to update resource_history when a resource_current record is deleted (physical delete)
DELIMITER $$
CREATE TRIGGER after_resource_current_delete
  AFTER DELETE ON resource_current
  FOR EACH ROW
  BEGIN
	DELETE FROM resource_history
    WHERE resource_id = OLD.resource_id
    AND resource_type = OLD.resource_type;
  END$$
DELIMITER ;

