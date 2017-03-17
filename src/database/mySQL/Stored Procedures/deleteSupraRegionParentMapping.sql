
drop procedure if exists OrganisationManager.deleteSupraRegionParentMapping;
DELIMITER //
CREATE PROCEDURE OrganisationManager.deleteSupraRegionParentMapping (
	in UUID char(36)
)
BEGIN
  delete from OrganisationManager.SupraRegionMap where ParentRegionUuid = UUID;
END //
DELIMITER ;
