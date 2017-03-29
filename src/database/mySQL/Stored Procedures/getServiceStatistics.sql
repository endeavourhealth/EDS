
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
    
    insert into ServiceStatistics (label, value)
    select 'Services linked to an organisation', count(distinct o.uuid) 
    from OrganisationManager.Organisation o
    join organisationmanager.mastermapping mm on mm.childUuid = o.Uuid
    where o.isService = 1
    and mm.mapTypeId = 0;
    
    insert into ServiceStatistics (label, value)
    select 'Orphaned Services', count(o.uuid) 
    from OrganisationManager.Organisation o
    left outer join organisationmanager.mastermapping mm on mm.childUuid = o.Uuid
    where o.isService = 1
    and mm.mapTypeId is null;
    
    select label, value from ServiceStatistics;
    
    drop table ServiceStatistics;
    
  
END //
DELIMITER ;
