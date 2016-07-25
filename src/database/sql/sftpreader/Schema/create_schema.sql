/* 
	create schema
*/

create schema sftpreader;

create table sftpreader.instance
(
	instance_id varchar(100) not null,

	constraint sftpreader_instance_instanceid_pk primary key (instance_id),
	constraint sftpreader_instance_instanceid_ck check (char_length(trim(instance_id)) > 0)
);

create table sftpreader.instance_configuration
(
	instance_id varchar(100) not null,
	hostname varchar(100) not null,
	port integer not null,
	remote_path varchar(1000) not null,
	username varchar(100) not null,
	client_public_key varchar(8000) not null,
	client_private_key varchar(8000) not null,
	client_private_key_password varchar(1000) not null,
	host_public_key varchar(8000) not null,

	constraint sftpreader_instanceconfiguration_intsanceid_pk primary key (instance_id),
	constraint sftpreader_instanceconfiguration_instanceid_fk foreign key (instance_id) references sftpreader.instance (instance_id)
);

