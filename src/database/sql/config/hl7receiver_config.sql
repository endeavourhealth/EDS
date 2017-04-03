insert into config
(
	app_id, 
	config_id, 
	config_data
)
values
('hl7receiver', 'postgres-url', 'jdbc:postgresql://localhost:5432/hl7receiver'),
('hl7receiver', 'postgres-username', 'postgres'),
('hl7receiver', 'postgres-password', '');
