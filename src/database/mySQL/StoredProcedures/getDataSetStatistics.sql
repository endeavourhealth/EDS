
drop procedure if exists OrganisationManager.getDataSetStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataSetStatistics (
)
BEGIN
  create temporary table OrganisationManager.DataSetStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.DataSetStatistics (label, value)
    select 'Total Number of Data Sets', count(*)
    from OrganisationManager.Dataset;
    
    select label, value from OrganisationManager.DataSetStatistics;
    
    drop table OrganisationManager.DataSetStatistics;
    
  
END //
DELIMITER ;
