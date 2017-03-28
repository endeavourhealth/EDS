
drop procedure if exists OrganisationManager.getOrganisationStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getOrganisationStatistics (
)
BEGIN
  create temporary table OrganisationStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationStatistics (label, value)
    select 'Total Number of Organisations', count(*) 
    from OrganisationManager.Organisation
    where IsService = 0;
    
    insert into OrganisationStatistics (label, value)
    select 'Bulk Imported Organisations', count(*) 
    from OrganisationManager.Organisation
    where BulkImported = 1
    and IsService = 0;
    
    insert into OrganisationStatistics (label, value)
    select 'Edited Bulk Imported Organisations', count(*) 
    from OrganisationManager.Organisation
    where BulkImported = 1
    and BulkItemUpdated = 1
    and IsService = 0;
    
    insert into OrganisationStatistics (label, value)
    select 'Manually Created Organisations', count(*) 
    from OrganisationManager.Organisation
    where BulkImported = 0
    and IsService = 0;
    
    select label, value from OrganisationStatistics;
    
    drop table OrganisationStatistics;
    
  
END //
DELIMITER ;
