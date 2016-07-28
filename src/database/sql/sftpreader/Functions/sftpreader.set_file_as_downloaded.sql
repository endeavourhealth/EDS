
create or replace function sftpreader.addRemoteFileDetails
(
	_instance_id varchar(100),
	_remote_batch_identifier varchar(500),
	_remote_file_type_identifier varchar(500),
	_filename varchar(1000),
	_remote_file_size_bytes bigint,
	_remote_created_date timestamp
)
returns void
as $$
	declare _batch_id integer;
	declare _batch_type_id integer;
begin

	select 
		batch_id into _batch_id
	from sftpreader.batch
	where instance_id = _instance_id
	and remote_batch_identifier = _remote_batch_identifier;

	select
		batch_type_id into _batch_type_id
	from sftpreader.configuration
	where instance_id = _instance_id;

	if (_batch_id is null)
	then

		insert into sftpreader.batch
		(
			instance_id,
			remote_batch_identifier,
			batch_type_id
		)
		values
		(
			_instance_id,
			_remote_batch_identifier,
			_batch_type_id
		)
		returning batch_id into _batch_id;

	end if;

	insert into sftpreader.batch_file
	(
		batch_id,
		batch_type_id,
		remote_file_type_identifier,
		filename,
		remote_file_size_bytes,
		remote_created_date
	)
	values
	(
		_batch_id,
		_batch_type_id,
		_remote_file_type_identifier,
		_filename,
		_remote_file_size_bytes,
		_remote_created_date
	);

end;
$$ language plpgsql;

