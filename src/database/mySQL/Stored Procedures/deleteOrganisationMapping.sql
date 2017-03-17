
drop procedure if exists OrganisationManager.deleteOrganisationMapping;
DELIMITER //
CREATE PROCEDURE OrganisationManager.deleteOrganisationMapping (
	in UUID char(36)
)
BEGIN
  delete from OrganisationManager.regionorganisationmap where organisationUUid = UUID;
END //
DELIMITER ;
