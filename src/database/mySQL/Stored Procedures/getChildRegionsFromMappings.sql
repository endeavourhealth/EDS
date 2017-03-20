
drop procedure if exists OrganisationManager.getChildRegionsFromMappings;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getChildRegionsFromMappings (
	in Parent char(36),
    in MappingType smallint
)
BEGIN
 select 
	r.name,
	r.description, 
    r.uuid
  from OrganisationManager.region r
  join OrganisationManager.MasterMapping mm on mm.ChildUuid = r.Uuid
  where mm.ParentUuid = Parent
  and mm.MapTypeId = MappingType;  
END //
DELIMITER ;