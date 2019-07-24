use audit;

DELIMITER //
CREATE PROCEDURE get_monthly_frailty_stats()
BEGIN

	-- export all data
	drop table if exists tmp.frailty_data;

	create table tmp.frailty_data as
	select
		date(timestmp) as `date`,
		time(timestmp) as `time`,
		CASE
			WHEN response_code = 200 AND response_body like '%potentially%' THEN 1
			ELSE 0
		END AS `matched_and_potentially_frail`,
		CASE
			WHEN response_code = 200 AND response_body not like '%potentially%' THEN 1
			ELSE 0
		END AS `matched_and_potentially_not_frail`,
		CASE
			WHEN response_code != 200 AND response_body like '%No patient record could be found%' THEN 1
			ELSE 0
		END AS `error_patient_not_matched`,
		CASE
			WHEN response_code != 200 AND response_body not like '%No patient record could be found%' THEN 1
			ELSE 0
		END AS `error_other`,
		duration_ms
	from audit.subscriber_api_audit
	where user_uuid = '4d4ebdd9-7b83-4736-b558-b5cc97147cd4' -- Redwood user
	and request_path not like '%=999999%'
	-- and date(timestmp) >= '2018-09-26'
	and date(timestmp) >= '2018-11-01'
	order by `date`;

	-- create rolling montly counts
	drop table if exists tmp.frailty_monthly_counts;

	create table tmp.frailty_monthly_counts as
	select
		MIN(`date`) as `month_starting`,
		SUM(matched_and_potentially_frail) as `matched_and_potentially_frail`,
		SUM(matched_and_potentially_not_frail) as `matched_and_potentially_not_frail`,
		SUM(error_patient_not_matched) as `error_patient_not_matched`,
		SUM(error_other) as `error_other`,
		SUM(matched_and_potentially_frail + matched_and_potentially_not_frail + error_patient_not_matched + error_other) as `total_api_requests`
	from tmp.frailty_data
	group by DATE_FORMAT(`date`, "%Y/%m")
	order by MIN(`date`);

	-- create table of rolling AND cumulative counts
	drop table if exists tmp.frailty_total_counts;

	set @frail_sum := 0;
	set @not_frail_sum := 0;
	set @not_matched_sum := 0;
	set @error_sum := 0;
	set @total_sum := 0;

	create table tmp.frailty_total_counts as
	select
		DATE_FORMAT(`month_starting`, "%Y/%m") as `month`,
		matched_and_potentially_frail,
		matched_and_potentially_not_frail,
		error_patient_not_matched,
		error_other,
		total_api_requests,
		(@frail_sum := @frail_sum + matched_and_potentially_frail) as `cumulative_matched_and_potentially_frail`,
		(@not_frail_sum := @not_frail_sum + matched_and_potentially_not_frail) as `cumulative_matched_and_potentially_not_frail`,
		(@not_matched_sum := @not_matched_sum + error_patient_not_matched) as `cumulative_error_patient_not_matched`,
		(@error_sum := @error_sum + error_other) as `cumulative_error_other`,
		(@total_sum := @total_sum + total_api_requests) as `cumulative_total_api_requests`
	from tmp.frailty_monthly_counts
	order by `month_starting`;


	select * from  tmp.frailty_total_counts;


END //
DELIMITER ;

-- call get_monthly_frailty_stats()