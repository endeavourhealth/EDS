
create or replace function sftpreader.add_file
(
	_instance_id varchar(100),
	_file_set_local_identifier varchar(500),
	_file_name varchar(1000),
	_file_path varchar(1000),
	_file_size_bytes bigint
)
returns void
as $$
	declare _file_set_id integer;
begin

	select 
		file_set_id into _file_set_id
	from sftpreader.file_set
	where instance_id = _instance_id
	and local_identifier = _file_set_local_identifier;

	if (_file_set_id is null)
	then

		insert into sftpreader.file_set
		(
			instance_id,
			local_identifier,
			create_date
		)
		values
		(
			_instance_id,
			_file_set_local_identifier,
			now()
		)
		returning file_set_id into _file_set_id;

	end if;

	insert into sftpreader.file
	(
		instance_id,
		file_set_id,
		file_name,
		file_path,
		download_date,
		file_size_bytes
	)
	values
	(
		_instance_id,
		_file_set_id,
		_file_name,
		_file_path,
		now(),
		_file_size_bytes
	);

end;
$$ language plpgsql;

