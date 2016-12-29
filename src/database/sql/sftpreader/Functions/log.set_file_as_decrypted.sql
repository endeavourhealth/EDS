
create or replace function log.set_file_as_decrypted
(
	_batch_file_id integer,
	_decrypted_filename varchar,
	_decrypted_size_bytes bigint
)
returns void
as $$
begin

	update log.batch_file
	set
		is_decrypted = true,
		decrypted_filename = _decrypted_filename,
		decrypt_date = date_trunc('second', now()::timestamp),
		decrypted_size_bytes = _decrypted_size_bytes
	where batch_file_id = _batch_file_id;

end;
$$ language plpgsql;
