
drop procedure if exists OrganisationManager.getDataSharingAgreementStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataSharingAgreementStatistics (
)
BEGIN
  create temporary table DataSharingAgreementStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into DataSharingAgreementStatistics (label, value)
    select 'Total Number of Data Sharing Agreements', count(*) 
    from OrganisationManager.DataSharingAgreement;
    
    select label, value from DataSharingAgreementStatistics;
    
    drop table DataSharingAgreementStatistics;
    
  
END //
DELIMITER ;
