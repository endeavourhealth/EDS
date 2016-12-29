
create or replace function log.get_batches
(
	_batch_ids integer[]
)
returns setof refcursor
as $$
declare
	batch refcursor;
	batch_file refcursor;
begin

	batch = 'batch';

	open batch for
		select
			b.batch_id,
			b.batch_identifier,
			b.local_relative_path,
			b.sequence_number
		from log.batch b
		where b.batch_id in
		(
			select unnest(_batch_ids)
		);
	return next batch;

	batch_file = 'batch_file';

	open batch_file for
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
		from log.batch b
		inner join log.batch_file bf on b.batch_id = bf.batch_id 
		where b.batch_id in
		(
			select unnest(_batch_ids)
		);
	return next batch_file;

end;
$$ language plpgsql;