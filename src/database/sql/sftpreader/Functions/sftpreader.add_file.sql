
create or replace function sftpreader.add_file
(
	_instance_id varchar,
	_remote_batch_identifier varchar,
	_remote_file_type_identifier varchar,
	_filename varchar,
	_local_relative_path varchar,
	_remote_size_bytes bigint,
	_remote_created_date timestamp,
	_requires_decryption boolean
)
returns table
(
	file_already_processed boolean,
	batch_file_id integer
)
as $$
	declare _batch_id integer;
	declare _batch_type_id integer;
	declare _batch_file_id integer;
	declare _file_already_processed boolean;
begin

	select 
		batch_id 
	into
		_batch_id
	from sftpreader.batch
	where instance_id = _instance_id
	and remote_batch_identifier = _remote_batch_identifier;

	select
		batch_type_id
	into
		_batch_type_id
	from sftpreader.configuration
	where instance_id = _instance_id;

	if (_batch_id is null)
	then

		insert into sftpreader.batch
		(
			instance_id,
			remote_batch_identifier,
			batch_type_id,
			local_relative_path
		)
		values
		(
			_instance_id,
			_remote_batch_identifier,
			_batch_type_id,
			_local_relative_path
		)
		returning batch_id into _batch_id;

	end if;

	select 
		batch_file_id,
		(is_downloaded and ((not requires_decryption) or is_decrypted))
	into
		_batch_file_id,
		_file_already_processed 
	from sftpreader.batch_file
	where batch_id = _batch_id
	and remote_file_type_identifier = _remote_file_type_identifier;

	if (_batch_file_id is not null)
	then
		if (not _file_already_processed)
		then
			delete from sftpreader.batch_file
			where batch_file_id = _batch_file_id;

			_batch_file_id = null;
		end if;
	end if;

	if (_batch_file_id is null)
	then
		insert into sftpreader.batch_file
		(
			batch_id,
			batch_type_id,
			remote_file_type_identifier,
			filename,
			remote_size_bytes,
			remote_created_date,
			requires_decryption,
			is_decrypted
		)
		values
		(
			_batch_id,
			_batch_type_id,
			_remote_file_type_identifier,
			_filename,
			_remote_size_bytes,
			_remote_created_date,
			_requires_decryption,
			case when _requires_decryption then false else null end
		)
		returning batch_file_id into _batch_file_id;
	end if;

	return query
	select
		_file_already_processed as file_already_processed,
		_batch_file_id as batch_file_id;

end;
$$ language plpgsql;

