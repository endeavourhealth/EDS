
drop procedure if exists OrganisationManager.getDataSharingAgreementStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataSharingAgreementStatistics (
)
BEGIN
  create temporary table OrganisationManager.DataSharingAgreementStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.DataSharingAgreementStatistics (label, value)
    select 'Total Number of Data Sharing Agreements', count(*) 
    from OrganisationManager.DataSharingAgreement;
    
    insert into OrganisationManager.DataSharingAgreementStatistics (label, value)
    select 'Data sharing agreements belonging to a region', count(distinct o.uuid) 
    from OrganisationManager.DataSharingAgreement o
    join OrganisationManager.MasterMapping mm on mm.ChildUuid = o.Uuid and mm.childMapTypeId= 3
    join OrganisationManager.Region r on r.Uuid = mm.parentUuid and mm.ParentMapTypeId  = 2;
    
    select label, value from OrganisationManager.DataSharingAgreementStatistics;
    
    drop table OrganisationManager.DataSharingAgreementStatistics;
    
  
END //
DELIMITER ;
