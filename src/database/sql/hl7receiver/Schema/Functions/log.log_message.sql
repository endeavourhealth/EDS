
create or replace function log.log_message
(
	_connection_id integer,
	_inbound_payload text,
	_outbound_payload text
)
returns integer
as $$
declare
	_message_id integer;
begin

	insert into log.message
	(
		connection_id,
		log_date,
		inbound_payload,
		outbound_payload
	)
	values
	(
		_connection_id,
		now(),
		_inbound_payload,
		_outbound_payload
	)
	returning message_id into _message_id;
	
	return _message_id;
	
end;
$$ language plpgsql;
