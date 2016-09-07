--manually delete the old version of this function
DROP FUNCTION IF EXISTS sftpreader.get_unnotified_batches (_instance_id varchar);

create or replace function sftpreader.get_unnotified_batch_splits
(
	_instance_id varchar
)
returns setof refcursor
as $$
	declare _batch_split_ids integer[];
	declare _batch_ids integer[];
begin

	select
		array_agg(b.batch_split_id) into _batch_split_ids
	from sftpreader.batch_split b
	where b.instance_id = _instance_id
	and b.have_notified = false;

	select
		array_agg(b.batch_id) into _batch_ids
	from sftpreader.batch_split b
	where b.instance_id = _instance_id
	and b.have_notified = false;

	return query
	select
	  * 
	from sftpreader.get_batch_splits(_batch_split_ids);

	return query
	select
	  * 
	from sftpreader.get_batches(_batch_ids);

end;
$$ language plpgsql;