
drop procedure if exists OrganisationManager.getDataProcessingAgreementStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataProcessingAgreementStatistics (
)
BEGIN
  create temporary table OrganisationManager.DataProcessingAgreementStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.DataProcessingAgreementStatistics (label, value)
    select 'Total Number of Data Processing Agreements', count(*) 
    from OrganisationManager.DataProcessingAgreement;
    
    select label, value from OrganisationManager.DataProcessingAgreementStatistics;
    
    drop table OrganisationManager.DataProcessingAgreementStatistics;
    
  
END //
DELIMITER ;
