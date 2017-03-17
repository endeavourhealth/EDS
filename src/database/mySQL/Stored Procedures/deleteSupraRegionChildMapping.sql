
drop procedure if exists OrganisationManager.deleteSupraRegionChildMapping;
DELIMITER //
CREATE PROCEDURE OrganisationManager.deleteSupraRegionChildMapping (
	in UUID char(36)
)
BEGIN
  delete from OrganisationManager.SupraRegionMap where ChildRegionUuid = UUID;
END //
DELIMITER ;
