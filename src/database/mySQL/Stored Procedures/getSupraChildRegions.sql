
drop procedure if exists OrganisationManager.getSupraChildRegions;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getSupraChildRegions (
	in RegionUUID char(36)
)
BEGIN
  select 
	cr.name,
	cr.description, 
    cr.uuid
  from OrganisationManager.region r
  join OrganisationManager.supraregionmap srm on srm.ParentRegionUUid = r.uuid
  join OrganisationManager.region cr on cr.uuid = srm.ChildRegionUuid
  where r.uuid = RegionUUID;
END //
DELIMITER ;
