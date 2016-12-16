/*
	insert test data
*/

insert into configuration.channel 
(
	channel_id, 
	channel_name, 
	port_number, 
	is_active,
	use_tls, 
	sending_application,
	sending_facility,
	receiving_application,
	receiving_facility,
	notes
)
values
(1, 'HOMERTON', 8900, true, false, 'HOMERTON_TIE', 'HOMERTON', 'EDS', 'ENDEAVOUR', ''),
(2, 'BARTS', 8901, true, false, 'BLT_TIE', 'BLT', 'EDS', 'ENDEAVOUR', '');

insert into configuration.channel_message_type
(
	channel_id,
	message_type,
	is_allowed
)
values
(1, 'ADT^A04', true),
(1, 'ACK^A04', true)


