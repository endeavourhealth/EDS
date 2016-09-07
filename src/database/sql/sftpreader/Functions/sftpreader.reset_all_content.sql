
create or replace function sftpreader.reset_all_content
()
returns void
as $$

	delete from sftpreader.unknown_file;
	delete from sftpreader.notification_message;
	delete from sftpreader.batch_split;
	delete from sftpreader.batch_file;
	delete from sftpreader.batch;
	
$$ language sql;
