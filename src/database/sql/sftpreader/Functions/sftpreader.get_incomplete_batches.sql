
create or replace function sftpreader.get_incomplete_batches
(
	_instance_id varchar
)
returns setof refcursor
as $$
declare
	incomplete_batch refcursor;
	incomplete_batch_file refcursor;
begin

	incomplete_batch = 'incomplete_batch';

	open incomplete_batch for
		select
			b.batch_id,
			b.batch_identifier,
			b.local_relative_path
		from sftpreader.batch b
		where b.instance_id = _instance_id
		and b.is_complete = false;
	return next incomplete_batch;

	incomplete_batch_file = 'incomplete_batch_file';

	open incomplete_batch_file for
		select
			b.batch_id,
			b.batch_identifier,
			b.local_relative_path,
			bf.batch_file_id,
			bf.file_type_identifier,
			bf.filename,
			bf.remote_size_bytes,
			bf.is_downloaded,
			bf.local_size_bytes,
			bf.requires_decryption,
			bf.is_decrypted,
			bf.decrypted_filename,
			bf.decrypted_size_bytes
		from sftpreader.batch b
		inner join sftpreader.batch_file bf on b.batch_id = bf.batch_id 
		where b.instance_id = _instance_id
		and b.is_complete = false;
	return next incomplete_batch_file;

end;
$$ language plpgsql;