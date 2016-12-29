
create or replace function log.get_batch_splits
(
	_batch_split_ids integer[]
)
returns setof refcursor
as $$
declare
	batch_split refcursor;
begin

	batch_split = 'batch_split';

	open batch_split for
		select
			b.batch_split_id,
			b.batch_id,
			b.local_relative_path,
			b.organisation_id
		from log.batch_split b
		where b.batch_split_id in
		(
			select unnest(_batch_split_ids)
		);
	return next batch_split;

end;
$$ language plpgsql;