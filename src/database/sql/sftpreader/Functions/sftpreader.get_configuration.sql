
create or replace function sftpreader.get_configuration
(
	_instance_id varchar(100)
)
returns table
(
	instance_id varchar,
	instance_description varchar,
	batch_type_id integer,
	batch_type_description varchar,
	poll_frequency_seconds integer,
	local_root_path varchar,
	hostname varchar,
	port integer,
	remote_path varchar,
	username varchar,
	client_private_key varchar,
	client_private_key_password varchar,
	host_public_key varchar,
	pgp_file_extension_filter varchar,
	pgp_sender_public_key varchar,
	pgp_recipient_public_key varchar,
	pgp_recipient_private_key varchar,
	pgp_recipient_private_key_password varchar
)
as $$

	select
		i.instance_id,
		i.description as instance_description,
		c.batch_type_id,
		bt.description as batch_type_description,
		c.poll_frequency_seconds,	
		c.local_root_path,
		cs.hostname,
		cs.port,
		cs.remote_path,
		cs.username,
		cs.client_private_key,
		cs.client_private_key_password,
		cs.host_public_key,
		cp.file_extension_filter as pgp_file_extension_filter,
		cp.sender_public_key as pgp_sender_public_key,
		cp.recipient_public_key as pgp_recipient_public_key,
		cp.recipient_private_key as pgp_recipient_private_key,
		cp.recipient_private_key_password as pgp_private_key_password
	from sftpreader.instance i
	left outer join sftpreader.configuration c on i.instance_id = c.instance_id
	left outer join sftpreader.batch_type bt on c.batch_type_id = bt.batch_type_id
	left outer join sftpreader.configuration_sftp cs on i.instance_id = cs.instance_id
	left outer join sftpreader.configuration_pgp cp on i.instance_id = cp.instance_id
	where i.instance_id = _instance_id;

$$ language sql;
