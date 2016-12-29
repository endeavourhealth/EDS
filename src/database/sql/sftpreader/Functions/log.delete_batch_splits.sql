
create or replace function log.delete_batch_splits
(
	_batch_id integer
)
returns void as
$$

	delete from log.batch_split bs
	where bs.batch_id = _batch_id;
	
$$ language sql;