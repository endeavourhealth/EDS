
drop procedure if exists OrganisationManager.getDataSharingSummaryStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataSharingSummaryStatistics (
)
BEGIN
  create temporary table OrganisationManager.DataSharingSummaryStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.DataSharingSummaryStatistics (label, value)
    select 'Total Number of Sharing Summaries', count(*) 
    from OrganisationManager.DataSharingSummary;
    
    select label, value from OrganisationManager.DataSharingSummaryStatistics;
    
    drop table OrganisationManager.DataSharingSummaryStatistics;
    
  
END //
DELIMITER ;
