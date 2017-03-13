
drop procedure if exists OrganisationManager.getOrganisationsForRegion;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getOrganisationsForRegion (
	in RegionUUID char(36)
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
    o.organisationUUid
  from OrganisationManager.region r
  join OrganisationManager.regionorganisationmap rom on rom.regionUuid = r.uuid
  join OrganisationManager.organisation o on o.OrganisationUuid = rom.OrganisationUuid
  where r.uuid = RegionUUID;
END //
DELIMITER ;
