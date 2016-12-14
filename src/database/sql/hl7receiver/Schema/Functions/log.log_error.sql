
create or replace function log.log_error
(
	_exception varchar(1000),
	_method varchar(1000),
	_message text
)
returns table
(
	error_id integer,
	error_uuid uuid
)
as $$

	insert into log.error
	(
		error_uuid,
		error_count,
		exception,
		method,
		message
	)
	values
	(
		uuid_generate_v4(),
		1,
		_exception,
		_method,
		_message
	)
	on conflict (exception, method, message) do 
	update
	set error_count = error.error_count + 1
	where error.exception = excluded.exception
	and error.method = excluded.method
	and error.message = excluded.message
	returning error_id, error_uuid;
		
$$ language sql;
