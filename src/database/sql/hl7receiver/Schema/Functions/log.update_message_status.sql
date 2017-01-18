
create or replace function log.update_message_status
(
	_message_id integer,
	_notification_status_id integer
)
returns void
as $$
begin

	update log.message
	set notification_status_id = _notification_status_id
	where message_id = _message_id;
	
end;
$$ language plpgsql;
