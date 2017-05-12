
drop procedure if exists OrganisationManager.getDatasetStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDatasetStatistics (
)
BEGIN
  create temporary table OrganisationManager.DatasetStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.DatasetStatistics (label, value)
    select 'Total Number of Datasets', count(*)
    from OrganisationManager.Dataset;
    
    select label, value from OrganisationManager.DatasetStatistics;
    
    drop table OrganisationManager.DatasetStatistics;
    
  
END //
DELIMITER ;
