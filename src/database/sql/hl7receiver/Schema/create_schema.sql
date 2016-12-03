/* 
	create schema
*/

create schema hl7receiver;

create table hl7receiver.instance
(
	instance_id varchar(100) not null,
	description varchar(1000) not null,

	constraint hl7receiver_instance_instanceid_pk primary key (instance_id),
	constraint hl7receiver_instance_instanceid_ck check (char_length(trim(instance_id)) > 0),
	constraint hl7receiver_instance_description_uq unique (description),
	constraint hl7receiver_instance_description_ck check (char_length(trim(description)) > 0)
);

