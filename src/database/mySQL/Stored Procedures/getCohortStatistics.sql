
drop procedure if exists OrganisationManager.getCohortStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getCohortStatistics (
)
BEGIN
  create temporary table OrganisationManager.CohortStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.CohortStatistics (label, value)
    select 'Total Number of Cohorts', count(*) 
    from OrganisationManager.Cohort;
    
    select label, value from OrganisationManager.CohortStatistics;
    
    drop table OrganisationManager.CohortStatistics;
    
  
END //
DELIMITER ;
