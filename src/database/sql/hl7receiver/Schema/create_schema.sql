/* 
	create schema
*/

create schema configuration;
create schema log;

create table configuration.instance
(
	instance_id integer not null,
	instance_name varchar(100) not null,
	description varchar(1000) not null,

	constraint configuration_instance_instanceid_pk primary key (instance_id),
	constraint configuration_instance_instancename_uq unique (instance_name), 
	constraint configuration_instance_instancename_ck check (char_length(trim(instance_name)) > 0),
	constraint configuration_instance_description_uq unique (description),
	constraint configuration_instance_description_ck check (char_length(trim(description)) > 0)
);

create table configuration.channel
(
	channel_id integer not null,
	channel_name varchar(100) not null,
	port_number integer not null,
	is_active boolean not null,
	use_tls boolean not null,
	remote_application varchar(100) not null,
	remote_facility varchar(100) not null,
	local_application varchar(100) not null,
	local_facility varchar(100) not null,
	use_acks boolean not null,
	notes varchar(1000) not null,

	constraint configuration_channel_channelid_pk primary key (channel_id),
	constraint configuration_channel_channelname_uq unique (channel_name),
	constraint configuration_channel_portnumber_uq unique (port_number),
	constraint configuration_channel_portnumber_ck check (port_number > 0),
	constraint configuration_channel_channelname_ck check (char_length(trim(channel_name)) > 0)
);

create table log.connection
(
	connection_id serial not null,
	instance_id integer not null,
	channel_id integer not null,
	local_port integer not null,
	remote_host varchar(100) not null,
	remote_port integer not null,
	connected timestamp not null,
	disconnected timestamp null,
	
	constraint log_connection_connectionid_pk primary key (connection_id),
	constraint log_connection_instanceid_fk foreign key (instance_id) references configuration.instance (instance_id),
	constraint log_connection_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_connection_localport_ck check (local_port > 0),
	constraint log_connection_remotehost_ck check (char_length(trim(remote_host)) > 0), 
	constraint log_connection_remoteport_ck check (remote_port > 0),
	constraint log_connection_connected_disconnected_ck check ((disconnected is null) or (connected <= disconnected)) 
);

create table log.message
(
	message_id serial not null,
	channel_id integer not null,
	connection_id integer not null,
	inbound_date timestamp null,
	inbound_payload varchar null,
	outbound_date timestamp null,
	outbound_payload varchar null,
	
	constraint log_message_messageid_pk primary key (message_id),
	constraint log_message_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_message_inbounddate_inboundpayload_outbounddate_outboundpayload_ck check ((inbound_date is not null and inbound_payload is not null) or (outbound_date is not null and outbound_payload is not null))
);
