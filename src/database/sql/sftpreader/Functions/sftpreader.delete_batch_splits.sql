-- Function: sftpreader.delete_batch_splits()

-- DROP FUNCTION sftpreader.delete_batch_splits();

CREATE OR REPLACE FUNCTION sftpreader.delete_batch_splits(_batch_id integer)
  RETURNS void AS
$BODY$

	delete from sftpreader.batch_split bs
	where bs.batch_id = _batch_id;
	
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION sftpreader.delete_batch_splits(integer)
  OWNER TO postgres;
