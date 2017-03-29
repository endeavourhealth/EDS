
drop procedure if exists OrganisationManager.getRegionStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getRegionStatistics (
)
BEGIN
  create temporary table RegionStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into RegionStatistics (label, value)
    select 'Total Number of Regions', count(*) 
    from OrganisationManager.Region;
    
    insert into RegionStatistics (label, value)
    select 'Regions containing an organisation', count(distinct r.uuid) 
    from OrganisationManager.Region r
    join organisationmanager.mastermapping mm on mm.parentUuid = r.Uuid
    join OrganisationManager.Organisation o on o.Uuid = mm.ChildUuid
    and mm.mapTypeId = 1;
    
    insert into RegionStatistics (label, value)
    select 'Regions containing a region', count(distinct r.uuid) 
    from OrganisationManager.Region r
    join organisationmanager.mastermapping mm on mm.parentUuid = r.Uuid
    join OrganisationManager.Region cr on cr.Uuid = mm.ChildUuid
    and mm.mapTypeId = 1;
    
    insert into RegionStatistics (label, value)
    select 'Regions belonging to a region', count(distinct cr.uuid) 
    from OrganisationManager.Region r
    join organisationmanager.mastermapping mm on mm.parentUuid = r.Uuid
    join OrganisationManager.Region cr on cr.Uuid = mm.ChildUuid
    and mm.mapTypeId = 1;
    
    insert into RegionStatistics (label, value)
    select 'Orphaned regions', count(distinct r.uuid) 
    from OrganisationManager.Region r
    left outer join organisationmanager.mastermapping mmp on mmp.parentUuid = r.Uuid
    left outer join organisationmanager.mastermapping mmc on mmc.childUuid = r.Uuid
    where mmp.parentUuid is null
	and mmc.parentUuid is null;
    
    select label, value from RegionStatistics;
    
    drop table RegionStatistics;
    
  
END //
DELIMITER ;
