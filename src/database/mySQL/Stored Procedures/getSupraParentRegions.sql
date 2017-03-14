
drop procedure if exists OrganisationManager.getSupraParentRegions;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getSupraParentRegions (
	in RegionUUID char(36)
)
BEGIN
  select 
	pr.name,
	pr.description, 
    pr.uuid
  from OrganisationManager.region r
  join OrganisationManager.supraregionmap srm on srm.ChildRegionUUid = r.uuid
  join OrganisationManager.region pr on pr.uuid = srm.ParentRegionUuid
  where r.uuid = RegionUUID;
END //
DELIMITER ;
