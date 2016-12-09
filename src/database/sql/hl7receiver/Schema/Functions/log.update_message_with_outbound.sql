
create or replace function log.update_message_with_outbound
(
	_message_id integer,
	_outbound_payload text
)
returns void
as $$

	update log.message
	set outbound_payload = _outbound_payload
	where message_id = _message_id;

$$ language sql;
