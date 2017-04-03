
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
    
    select label, value from OrganisationManager.DataSharingAgreementStatistics;
    
    drop table OrganisationManager.DataSharingAgreementStatistics;
    
  
END //
DELIMITER ;
