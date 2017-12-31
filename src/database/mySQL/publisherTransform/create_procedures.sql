use publisher_transform_???;

DROP PROCEDURE IF EXISTS `save_resource_id_map`;

DELIMITER $$
CREATE PROCEDURE `save_resource_id_map`(
	IN _service_id varchar(36),
    IN _system_id varchar(36),
    IN _resource_type varchar(50),
    IN _source_id varchar(500),
    IN _eds_id varchar(36)
)
BEGIN

INSERT INTO resource_id_map (service_id, system_id, resource_type, source_id, eds_id)
VALUES (_service_id, _system_id, _resource_type, _source_id, _eds_id);

END$$
DELIMITER ;
