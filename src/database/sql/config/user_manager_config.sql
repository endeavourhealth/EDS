INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-user-manager', 'keycloak', '{
  "realm": "endeavour",
  "realm-public-key": "<Keycloak public key here>",
  "auth-server-url": "<Keycloak url here>",
  "ssl-required": "external",
  "resource": "eds-user-manager",
  "public-client": true
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-user-manager', 'keycloak_proxy', '{
 "user" : "eds-ui",
 "pass" : "<Keycloak proxy password here>",
 "url" : "<Keycloak url here>"
}');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-user-manager', 'application', '{ "appUrl" : "http://localhost:8080" }');
