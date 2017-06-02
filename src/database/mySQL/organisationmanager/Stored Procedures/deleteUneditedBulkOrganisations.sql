
drop procedure if exists OrganisationManager.deleteUneditedBulkOrganisations;
DELIMITER //
CREATE PROCEDURE OrganisationManager.deleteUneditedBulkOrganisations (
)
BEGIN
  delete from OrganisationManager.Organisation where BulkImported = 1 and BulkItemUpdated = 0;
END //
DELIMITER ;
