
drop procedure if exists OrganisationManager.deleteAllMappings;
DELIMITER //
CREATE PROCEDURE OrganisationManager.deleteAllMappings (
	in UUID char(36)
)
BEGIN
  delete from OrganisationManager.MasterMapping where ChildUuid = UUID;
  
  delete from OrganisationManager.MasterMapping where ParentUUid = UUID;
END //
DELIMITER ;
