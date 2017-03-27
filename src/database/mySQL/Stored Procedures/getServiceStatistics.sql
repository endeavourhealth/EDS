
drop procedure if exists OrganisationManager.getServiceStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getServiceStatistics (
)
BEGIN
  create temporary table ServiceStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into ServiceStatistics (label, value)
    select 'Total Number of Services', count(*) 
    from OrganisationManager.Organisation 
    where isService = 1;
    
    select label, value from ServiceStatistics;
    
    drop table ServiceStatistics;
    
  
END //
DELIMITER ;
