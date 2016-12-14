/*
	create extensions
*/
create extension "uuid-ossp";

/* 
	create schemas
*/
create schema dictionary;
create schema configuration;
create schema log;

/*
	create tables
*/

create table dictionary.message_type
(
	message_type varchar(100) not null,
	description varchar(100) not null,
	
	constraint dictionary_messagetype_messagetype_pk primary key (message_type),
	constraint dictionary_messagetype_messagetype_ck check (char_length(trim(message_type)) > 0),
	constraint dictionary_messagetype_description_ck check (char_length(trim(description)) > 0)
);

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
	sending_application varchar(100),
	sending_facility varchar(100),
	recipient_application varchar(100),
	recipient_facility varchar(100),
	notes varchar(1000) not null,

	constraint configuration_channel_channelid_pk primary key (channel_id),
	constraint configuration_channel_channelname_uq unique (channel_name),
	constraint configuration_channel_portnumber_uq unique (port_number),
	constraint configuration_channel_portnumber_ck check (port_number > 0),
	constraint configuration_channel_channelname_ck check (char_length(trim(channel_name)) > 0)
);

create table configuration.channel_message_type
(
	channel_id integer not null,
	message_type varchar(100) not null,
	is_allowed boolean not null,
	
	constraint configuration_channelmessagetype_channelid_messagetype_pk primary key (channel_id, message_type),
	constraint configuration_channelmessagetype_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint configuration_channelmessagetype_messagetype_fk foreign key (message_type) references dictionary.message_type (message_type)
);

create table log.connection
(
	connection_id serial not null,
	instance_id integer not null,
	channel_id integer not null,
	local_port integer not null,
	remote_host varchar(100) not null,
	remote_port integer not null,
	connect_date timestamp not null,
	disconnect_date timestamp null,
	
	constraint log_connection_connectionid_pk primary key (connection_id),
	constraint log_connection_instanceid_fk foreign key (instance_id) references configuration.instance (instance_id),
	constraint log_connection_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_connection_channelid_connectionid_uq unique (channel_id, connection_id),
	constraint log_connection_localport_ck check (local_port > 0),
	constraint log_connection_remotehost_ck check (char_length(trim(remote_host)) > 0), 
	constraint log_connection_remoteport_ck check (remote_port > 0),
	constraint log_connection_connectdate_disconnectdate_ck check ((disconnect_date is null) or (connect_date <= disconnect_date)) 
);

create table log.message
(
	message_id serial not null,
	channel_id integer not null,
	connection_id integer not null,
	log_date timestamp not null,
	message_control_id varchar(100) not null,
	inbound_message_type varchar(100) not null,
	inbound_payload varchar not null,
	outbound_message_type varchar(100) not null,
	outbound_payload varchar not null,
	
	constraint log_message_messageid_pk primary key (message_id),
	constraint log_message_channelid_connectionid_fk foreign key (channel_id, connection_id) references log.connection (channel_id, connection_id),
	constraint log_message_inboundmessagetype_fk foreign key (channel_id, inbound_message_type) references configuration.channel_message_type (channel_id, message_type),
	constraint log_message_outboundmessagetype_fk foreign key (channel_id, outbound_message_type) references configuration.channel_message_type (channel_id, message_type),
	constraint log_message_messagecontrolid_ck check (char_length(trim(message_control_id)) > 0)
);

create table log.error
(
	error_id serial not null,
	error_uuid uuid not null,
	error_count integer not null,
	exception varchar(1000) not null,
	method varchar(1000) not null,
	message text,
	
	constraint log_error_errorid_pk primary key (error_id),
	constraint log_error_erroruuid_fk unique (error_uuid),
	constraint log_error_errorcount_ck check (error_count > 0),
	constraint log_error_exception_method_message_uq unique (exception, method, message)
);

create table log.dead_letter
(
	dead_letter_id serial not null,
	log_date timestamp not null,
	connection_id integer null,
	local_port integer not null,
	remote_host varchar(100) not null,
	remote_port integer not null,
	channel_id integer null,
	sending_application varchar(100) null,
	sending_facility varchar(100) null,
	recipient_application varchar(100) null,
	recipient_facility varchar(100) null,
	inbound_payload text null,
	outbound_payload text null,
	error_id integer null,
	
	constraint log_deadletter_deadletterid_pk primary key (dead_letter_id),
	constraint log_deadletter_connectionid_fk foreign key (connection_id) references log.connection (connection_id),
	constraint log_deadletter_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_deadletter_errorid_fk foreign key (error_id) references log.error (error_id)
);

/*
	insert data
*/

insert into dictionary.message_type (message_type, description) values
('ADT^A01', 'Admit / visit notification'),
('ADT^A02', 'Transfer a patient'),
('ADT^A03', 'Discharge/end visit'),
('ADT^A04', 'Register a patient'),
('ADT^A05', 'Pre-admit a patient'),
('ADT^A06', 'Change an outpatient to an inpatient'),
('ADT^A07', 'Change an inpatient to an outpatient'),
('ADT^A08', 'Update patient information'),
('ADT^A09', 'Patient departing - tracking'),
('ADT^A10', 'Patient arriving - tracking'),
('ADT^A11', 'Cancel admit/visit notification'),
('ADT^A12', 'Cancel transfer'),
('ADT^A13', 'Cancel discharge/end visit'),
('ADT^A14', 'Pending admit'),
('ADT^A15', 'Pending transfer'),
('ADT^A16', 'Pending discharge'),
('ADT^A17', 'Swap patients'),
('ADT^A18', 'Merge patient information'),
('ADT^A19', 'Patient query'),
('ADT^A20', 'Bed status update'),
('ADT^A21', 'Patient goes on a "leave of absence"'),
('ADT^A22', 'Patient returns from a "leave of absence"'),
('ADT^A23', 'Delete a patient record'),
('ADT^A24', 'Link patient information'),
('ADT^A25', 'Cancel pending discharge'),
('ADT^A26', 'Cancel pending transfer'),
('ADT^A27', 'Cancel pending admit'),
('ADT^A28', 'Add person information'),
('ADT^A29', 'Delete person information'),
('ADT^A30', 'Merge person information'),
('ADT^A31', 'Update person information'),
('ADT^A32', 'Cancel patient arriving - tracking'),
('ADT^A33', 'Cancel patient departing - tracking'),
('ADT^A34', 'Merge patient information - patient ID only'),
('ADT^A35', 'Merge patient information - account number only'),
('ADT^A36', 'Merge patient information - patient ID and account number'),
('ADT^A37', 'Unlink patient information'),
('ADT^A38', 'Cancel pre-admit'),
('ADT^A39', 'Merge person - external ID'),
('ADT^A40', 'Merge patient - internal ID'),
('ADT^A41', 'Merge account - patient account number'),
('ADT^A42', 'Merge visit - visit number'),
('ADT^A43', 'Move patient information - internal ID'),
('ADT^A44', 'Move account information - patient account number'),
('ADT^A45', 'Move visit information - visit number'),
('ADT^A46', 'Change external ID'),
('ADT^A47', 'Change internal ID'),
('ADT^A48', 'Change alternate patient ID'),
('ADT^A49', 'Change patient account number'),
('ADT^A50', 'Change visit number'),
('ADT^A51', 'Change alternate visit ID');
