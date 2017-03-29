
drop procedure if exists OrganisationManager.getDataFlowStatistics;
DELIMITER //
CREATE PROCEDURE OrganisationManager.getDataFlowStatistics (
)
BEGIN
  create temporary table DataFlowStatistics (
	label varchar(100) not null,
    value varchar(100) not null
    );
    
    insert into DataFlowStatistics (label, value)
    select 'Total Number of Data Flows', count(*) 
    from OrganisationManager.DataFlow;
    
    select label, value from DataFlowStatistics;
    
    drop table DataFlowStatistics;
    
  
END //
DELIMITER ;
