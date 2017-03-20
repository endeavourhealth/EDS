
drop procedure if exists OrganisationManager.getParentRegionsFromMappings;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getParentRegionsFromMappings (
	in Child char(36),
    in MappingType smallint
)
BEGIN
 select 
	r.name,
	r.description, 
    r.uuid
  from OrganisationManager.region r
  join OrganisationManager.MasterMapping mm on mm.ParentUuid = r.Uuid
  where mm.ChildUuid = Child
  and mm.MapTypeId = MappingType;  
END //
DELIMITER ;