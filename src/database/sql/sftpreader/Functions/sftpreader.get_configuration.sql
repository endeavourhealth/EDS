
create or replace function sftpreader.get_configuration
(
	_instance_id varchar(100)
)
returns table
(
	instance_id varchar,
	hostname varchar,
	port integer,
	remote_path varchar,
	username varchar,
	client_private_key varchar,
	client_private_key_password varchar,
	host_public_key varchar
)
as $$

	select
		i.instance_id,
		ic.hostname,
		ic.port,
		ic.remote_path,
		ic.username,
		ic.client_private_key,
		ic.client_private_key_password,
		ic.host_public_key
	from sftpreader.instance i
	inner join sftpreader.instance_configuration ic on i.instance_id = ic.instance_id
	where i.instance_id = _instance_id

$$ language sql;


