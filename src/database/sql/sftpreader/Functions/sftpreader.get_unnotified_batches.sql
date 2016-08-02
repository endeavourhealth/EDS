
create or replace function sftpreader.get_unnotified_batches
(
	_instance_id varchar
)
returns setof refcursor
as $$
declare
	_batch_ids integer[];
begin

	select
		array_agg(b.batch_id) into _batch_ids
	from sftpreader.batch b
	where b.instance_id = _instance_id
	and b.is_complete = true
	and b.have_notified = false;

	return query
	select
	  * 
	from sftpreader.get_batches(_batch_ids);

end;
$$ language plpgsql;