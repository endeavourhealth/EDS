
drop procedure if exists OrganisationManager.getRegionsForOrganisation;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getRegionsForOrganisation (
	in OrganisationUUID char(36)
)
BEGIN
  select 
	r.name,
	r.description, 
    r.uuid
  from OrganisationManager.region r
  join OrganisationManager.regionorganisationmap rom on rom.regionUuid = r.uuid
  join OrganisationManager.organisation o on o.uuid = rom.OrganisationUuid
  where o.uuid = OrganisationUUID;
END //
DELIMITER ;
