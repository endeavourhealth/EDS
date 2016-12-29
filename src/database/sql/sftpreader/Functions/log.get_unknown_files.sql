
create or replace function log.get_unknown_files
(
	_instance_id varchar
)
returns table
(
	unknown_file_id integer,
	insert_date timestamp,
	filename varchar,
	remote_created_date timestamp,
	remote_size_bytes bigint
)
as $$

	select
		unknown_file_id,
		insert_date,
		filename,
		remote_created_date,
		remote_size_bytes
	from log.unknown_file
	where instance_id = _instance_id;

$$ language sql;

