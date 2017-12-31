USE ehr_??; -- we can have multiple EHR databases

DROP PROCEDURE IF EXISTS `save_resource`;
DROP PROCEDURE IF EXISTS `delete_resource`;
DROP PROCEDURE IF EXISTS `physical_delete_resource`;

DELIMITER $$
CREATE PROCEDURE `save_resource`(
	IN _service_id varchar(36),
    IN _system_id varchar(36),
    IN _resource_type varchar(50),
    IN _resource_id varchar(36),
    IN _timestmp timestamp,
    IN _patient_id varchar(36),
    IN _resource_data mediumtext,
    IN _resource_checksum bigint,
    IN _exchange_batch_id varchar(36),
    IN _version varchar(36),
    IN _resource_metadata varchar(1000)
)
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
	_service_id,
    _system_id,
    _resource_type,
    _resource_id,
    _timestmp,
    _patient_id,
    _resource_data,
    _resource_checksum,
    false,
    _exchange_batch_id,
    _version
);

INSERT INTO resource_current (
	service_id,
    system_id,
    resource_type,
    resource_id,
    updated_at,
    patient_id,
    resource_data,
    resource_checksum,
    resource_metadata
) VALUES (
	_service_id,
    _system_id,
    _resource_type,
    _resource_id,
    _timestmp,
    _patient_id,
    _resource_data,
    _resource_checksum,
    _resource_metadata
) ON DUPLICATE KEY UPDATE
	updated_at = VALUES(updated_at),
    resource_data = VALUES(resource_data),
    resource_checksum = VALUES(resource_checksum),
    resource_metadata = VALUES(resource_metadata);

END$$
DELIMITER ;



DELIMITER $$
CREATE PROCEDURE `delete_resource`(
	IN _service_id varchar(36),
    IN _system_id varchar(36),
    IN _resource_type varchar(50),
    IN _resource_id varchar(36),
    IN _timestmp timestamp,
    IN _patient_id varchar(36),
    IN _exchange_batch_id varchar(36),
    IN _version varchar(36)
)
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
	_service_id,
    _system_id,
    _resource_type,
    _resource_id,
    _timestmp,
    _patient_id,
    null,
    null,
    true,
    _exchange_batch_id,
    _version
);

DELETE FROM resource_current
WHERE
	service_id = _service_id
    AND system_id = _system_id
    AND patient_id = _patient_id
    AND resource_type = _resource_type
    AND resource_id = _resource_id;


END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE `physical_delete_resource`(
	IN _service_id varchar(36),
    IN _system_id varchar(36),
    IN _resource_type varchar(50),
    IN _resource_id varchar(36),
    IN _timestmp timestamp,
    IN _patient_id varchar(36),
    IN _version varchar(36)
)
BEGIN

DELETE FROM resource_history
WHERE
	resource_id = _resource_id
    AND resource_type = _resource_type
    AND created_at = _timestmp
    AND version = _version;


DELETE FROM resource_current
WHERE
	service_id = _service_id
    AND system_id = _system_id
    AND patient_id = _patient_id
    AND resource_type = _resource_type
    AND resource_id = _resource_id;


END$$
DELIMITER ;
