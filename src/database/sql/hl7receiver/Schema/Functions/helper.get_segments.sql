
create or replace function helper.get_segments
(
	_message_id integer
)
returns table
(
	name text,
	text text
)
as $$
begin

	return query
	select 
		split_part(segment_text, '|', 1) as name,
		segment_text as text
	from
	(
		select unnest(string_to_array(inbound_payload, chr(13))) segment_text
		from log.message
		where message_id = _message_id	
	) segments
	where segment_text != '';

end;
$$ language plpgsql;
