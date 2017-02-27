INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-patient-explorer', 'keycloak', '{
  "realm": "endeavour",
  "realm-public-key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlnMTOPmUhfZVgd/qCV1svJe0AWFTm6hs3zaas2HNNHhR1rIGH02YBXeDazrbMHvJz1Q/weXn1j0tyfTNf53cwH4KQy3+OXjnh1vXOlR26XzmjdkvG+Hoy5L3+JbpIV5ktflFRxstml1CU3p8jZSXMyjLcCn1I1IbCWG/YsO1ST34ZOSI0K+11Y3N/fYZnsZW7OIPTc6zTpUIq0/jOySgSD1xwOS9q/MPJ6gq8B2LyDDYDR+pJPzlxQ3JPk2gbvqYYTSQcpwZwNZiyVhatiX4lxutDXzZd0FFMF2WVv9uFUcQkt1GkKxs9mN9u5EdbaWTb2noznBvBvdvbdePuSJdpQIDAQAB",
  "auth-server-url": "https://keycloak.eds.c.healthforge.io/auth",
  "ssl-required": "external",
  "resource": "eds-ui",
  "public-client": true
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-patient-explorer', 'keycloak_proxy_user', 'eds-ui' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-patient-explorer', 'keycloak_proxy_pass', 'bd285adbc36842d7a27088e93c36c13e29ed69fa63a6' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-patient-explorer', 'enterprise-lite-db', 'jdbc:postgresql://localhost/enterprise_data?user=postgres&password=');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-patient-explorer', 'application', '{ "appUrl" : "http://localhost:8080" }');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-patient-explorer', 'enterprise-lite','{
    "username" : "postgres",
    "password" : "",
    "ui-username" : "restricted",
    "ui-password" : "",
    "url"    : "jdbc:postgresql://localhost:5432/enterprise_data"
}');