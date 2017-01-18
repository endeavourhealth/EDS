
create or replace function log.get_next_unnotified_message
(
	_channel_id integer,
	_instance_id integer
)
returns table
(
	message_id integer,
	message_control_id varchar,
	message_sequence_number varchar,
	message_date timestamp,
	inbound_message_type varchar,
	inbound_payload varchar,
	notification_status_id smallint
)
as $$
begin

	if not exists
	(
		select * 
		from log.channel_forwarder_lock
		where channel_id = _channel_id
		and instance_id = _instance_id
	)
	then
		raise exception 'instance_id % does not have channel forwarder lock', _instance_id;
		return;
	end if;
	
	return query
	select 
		m.message_id,
		m.message_control_id,
		m.message_sequence_number,
		m.message_date,
		m.inbound_message_type,
		m.inbound_payload,
		m.notification_status_id
	from log.message m
	where channel_id = _channel_id
	and m.notification_status_id in 
	(
		1, -1
	)
	order by m.message_date desc, m.log_date desc
	limit 1;
	
end;
$$ language plpgsql;
