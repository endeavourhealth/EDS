
drop procedure if exists OrganisationManager.getCohortStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getCohortStatistics (
)
BEGIN
  create temporary table CohortStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into CohortStatistics (label, value)
    select 'Total Number of Cohorts', count(*) 
    from OrganisationManager.Cohort;
    
    select label, value from CohortStatistics;
    
    drop table CohortStatistics;
    
  
END //
DELIMITER ;
