
drop procedure if exists OrganisationManager.getDataFlowStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataFlowStatistics (
)
BEGIN
  create temporary table OrganisationManager.DataFlowStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into OrganisationManager.DataFlowStatistics (label, value)
    select 'Total Number of Data Flows', count(*) 
    from OrganisationManager.DataFlow;
    
    insert into OrganisationManager.DataFlowStatistics (label, value)
    select 'Total Volume for All Data Flows', sum(ApproximateVolume) 
    from OrganisationManager.DataFlow;
    
    insert into OrganisationManager.DataFlowStatistics (label, value)
    select 'Average Volume for Data Flows', round(avg(ApproximateVolume))
    from OrganisationManager.DataFlow;
    
    select label, value from OrganisationManager.DataFlowStatistics;
    
    drop table OrganisationManager.DataFlowStatistics;
    
  
END //
DELIMITER ;
