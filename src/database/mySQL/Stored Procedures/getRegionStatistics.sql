
drop procedure if exists OrganisationManager.getRegionStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getRegionStatistics (
)
BEGIN

  create temporary table OrganisationManager.RegionStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.RegionStatistics (label, value)
    select 'Total Number of Regions', count(*) 
    from OrganisationManager.Region;
    
    insert into OrganisationManager.RegionStatistics (label, value)
    select 'Regions containing a data sharing agreement', count(distinct r.uuid) 
    from OrganisationManager.Region r
    join OrganisationManager.MasterMapping mm on mm.parentUuid = r.Uuid and mm.ParentMapTypeId = 2
    join OrganisationManager.DataSharingAgreement o on o.Uuid = mm.ChildUuid and mm.childMapTypeId = 3;
    
    insert into OrganisationManager.RegionStatistics (label, value)
    select 'Regions containing an organisation', count(distinct r.uuid) 
    from OrganisationManager.Region r
    join OrganisationManager.MasterMapping mm on mm.parentUuid = r.Uuid and mm.ParentMapTypeId = 2
    join OrganisationManager.Organisation o on o.Uuid = mm.ChildUuid and mm.childMapTypeId = 1;
    
    insert into OrganisationManager.RegionStatistics (label, value)
    select 'Regions containing a region', count(distinct r.uuid) 
    from OrganisationManager.Region r
    join OrganisationManager.MasterMapping mm on mm.parentUuid = r.Uuid and mm.ParentMapTypeId = 2
    join OrganisationManager.Region cr on cr.Uuid = mm.ChildUuid and mm.childMapTypeId = 2;
    
    insert into OrganisationManager.RegionStatistics (label, value)
    select 'Regions belonging to a region', count(distinct cr.uuid) 
    from OrganisationManager.Region r
    join OrganisationManager.MasterMapping mm on mm.parentUuid = r.Uuid and mm.ParentMapTypeId = 2
    join OrganisationManager.Region cr on cr.Uuid = mm.ChildUuid and mm.childMapTypeId = 2;
    
    insert into OrganisationManager.RegionStatistics (label, value)
    select 'Orphaned regions', count(distinct r.uuid) 
    from OrganisationManager.Region r
    left outer join OrganisationManager.MasterMapping mmp on mmp.parentUuid = r.Uuid and mmp.ParentMapTypeId = 2
    left outer join OrganisationManager.MasterMapping mmc on mmc.childUuid = r.Uuid and mmc.childMapTypeId = 2
    where mmp.parentUuid is null
	and mmc.parentUuid is null;
    
    select label, value from OrganisationManager.RegionStatistics;
    
    drop table OrganisationManager.RegionStatistics;
    
  
END //
DELIMITER ;
