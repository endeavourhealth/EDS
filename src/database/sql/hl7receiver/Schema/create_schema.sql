/* 
	create schema
*/

create schema configuration;
create schema message;

create table configuration.instance
(
	instance_id varchar(100) not null,
	description varchar(1000) not null,

	constraint configuration_instance_instanceid_pk primary key (instance_id),
	constraint configuration_instance_instanceid_ck check (char_length(trim(instance_id)) > 0),
	constraint configuration_instance_description_uq unique (description),
	constraint configuration_instance_description_ck check (char_length(trim(description)) > 0)
);

create table configuration.port
(
	port_number integer not null,
	tls boolean not null,
	notes varchar(1000) not null,

	constraint configuration_port_portnumber_pk primary key (port_number),
	constraint configuration_port_portnumber_ck check (port_number > 0)
);

create table configuration.channel
(
	channel_id integer not null,
	channel_name varchar(100) not null,
	remote_application varchar(100) not null,
	remote_facility varchar(100) not null,
	local_application varchar(100) not null,
	local_facility varchar(100) not null,
	port_number integer not null,
	use_acks boolean not null,
	notes varchar(1000) not null,

	constraint configuration_channel_channelid_pk primary key (channel_id),
	constraint configuration_channel_channelname_uq unique (channel_name),
	constraint configuration_channel_channelname_ck check (char_length(trim(channel_name)) > 0),
	constraint configuration_channel_portnumber_fk foreign key (port_number) references configuration.port (port_number)
);

