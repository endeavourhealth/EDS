INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'postgres', '{
	"username" : "postgres",
	"password" : "",
	"url"	: "jdbc:postgresql://localhost:5432/enterprise_data"
}' );
