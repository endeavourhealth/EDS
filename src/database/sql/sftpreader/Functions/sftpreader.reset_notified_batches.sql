
create or replace function sftpreader.reset_notified_batches
()
returns void
as $$

	update sftpreader.batch_split
	set
		have_notified = false, 
		notification_date = null;
	
$$ language sql;