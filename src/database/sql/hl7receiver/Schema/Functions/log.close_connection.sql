
create or replace function log.close_connection
(
	_connection_id integer
)
returns void
as $$
begin

	update log.connection
	set disconnected = now()
	where connection_id = _connection_id;
	
end;
$$ language plpgsql;
