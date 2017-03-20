
drop procedure if exists OrganisationManager.getParentOrganisationsFromMappings;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getParentOrganisationsFromMappings (
	in Child char(36),
    in MappingType smallint
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
  join OrganisationManager.MasterMapping mm on mm.ParentUuid = o.Uuid
  where mm.ChildUuid = Child
  and mm.MapTypeId = MappingType;  
END //
DELIMITER ;