
drop procedure if exists OrganisationManager.getChildOrganisationsFromMappings;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getChildOrganisationsFromMappings (
	in Parent char(36),
    in MappingType smallint,
    in OrganisationType smallint
)
BEGIN
 select 
	o.name,
	o.alternative_name, 
    o.ods_code,
    o.ico_code,
    o.ig_toolkit_status,
    o.date_of_registration,
    o.registration_person,
    o.evidence_of_registration,
    o.uuid,
    o.isService
  from OrganisationManager.organisation o
  join OrganisationManager.MasterMapping mm on mm.ChildUuid = o.Uuid
  where mm.ParentUuid = Parent
  and mm.MapTypeId = MappingType
  and o.isService = OrganisationType;  
END //
DELIMITER ;