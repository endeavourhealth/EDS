
create or replace function sftpreader.delete_batch_splits
(
	_batch_id integer
)
returns void as
$$

	delete from sftpreader.batch_split bs
	where bs.batch_id = _batch_id;
	
$$ language sql;