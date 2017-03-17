
drop procedure if exists OrganisationManager.deleteRegionMapping;
DELIMITER //
CREATE PROCEDURE OrganisationManager.deleteRegionMapping (
	in UUID char(36)
)
BEGIN
  delete from OrganisationManager.regionorganisationmap where regionUuid = UUID;
END //
DELIMITER ;
