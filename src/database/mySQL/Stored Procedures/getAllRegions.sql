
drop procedure if exists OrganisationManager.getAllRegions;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getAllRegions ()
BEGIN
  select 
	r.name, 
    r.description, 
    r.uuid, 
    count(rom.regionUuid) 
  from OrganisationManager.Region r
  left outer join organisationmanager.regionorganisationmap rom on rom.RegionUuid = r.uuid
  group by r.name, r.description, r.uuid;
END //
DELIMITER ;