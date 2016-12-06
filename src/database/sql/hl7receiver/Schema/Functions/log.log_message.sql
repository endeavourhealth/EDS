
create or replace function log.log_message
(
	_channel_id integer,
	_connection_id integer,
	_inbound_payload text
)
returns integer
as $$
declare
	_message_id integer;
begin

	insert into log.message
	(
		channel_id,
		connection_id,
		inbound_date,
		inbound_payload
	)
	values
	(
		_channel_id,
		_connection_id,
		now(),
		_inbound_payload
	)
	returning message_id into _message_id;
	
	return _message_id;
	
end;
$$ language plpgsql;
