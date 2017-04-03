
drop procedure if exists OrganisationManager.getOrganisationMarkers;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getOrganisationMarkers (
	in RegionId char(36)
)
BEGIN
  select 
	o.name,
    a.lat,
    a.lng
  from organisationmanager.organisation o
  join organisationmanager.address a on a.organisationUuid = o.Uuid
  join organisationmanager.MasterMapping mm on mm.ChildUuid = o.Uuid and mm.ChildMapTypeId = 1
  where mm.ParentUuid = RegionId;
END //
DELIMITER ;
