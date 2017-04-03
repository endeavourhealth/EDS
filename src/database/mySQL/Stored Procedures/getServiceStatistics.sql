
drop procedure if exists OrganisationManager.getServiceStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getServiceStatistics (
)
BEGIN
  create temporary table OrganisationManager.ServiceStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.ServiceStatistics (label, value)
    select 'Total Number of Services', count(*) 
    from OrganisationManager.Organisation 
    where isService = 1;
    
    insert into OrganisationManager.ServiceStatistics (label, value)
    select 'Services linked to an organisation', count(distinct o.uuid) 
    from OrganisationManager.Organisation o
    join OrganisationManager.MasterMapping mm on mm.childUuid = o.Uuid and mm.childMapTypeId = 0
    where o.isService = 1;
    
    insert into OrganisationManager.ServiceStatistics (label, value)
    select 'Orphaned Services', count(mm.childUuid) 
    from OrganisationManager.Organisation o
    left outer join OrganisationManager.MasterMapping mm on mm.childUuid = o.Uuid and mm.childMapTypeId = 0
    where o.isService = 1
    and mm.childMapTypeId is null;
    
    select label, value from OrganisationManager.ServiceStatistics;
    
    drop table OrganisationManager.ServiceStatistics;
    
  
END //
DELIMITER ;
