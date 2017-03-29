
drop procedure if exists OrganisationManager.getDataSharingSummaryStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataSharingSummaryStatistics (
)
BEGIN
  create temporary table DataSharingSummaryStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into DataSharingSummaryStatistics (label, value)
    select 'Total Number of Sharing Summaries', count(*) 
    from OrganisationManager.DataSharingSummary;
    
    select label, value from DataSharingSummaryStatistics;
    
    drop table DataSharingSummaryStatistics;
    
  
END //
DELIMITER ;
