INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'cassandra', '{
 "node" : [
    "127.0.0.1"
  ]
 }' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'logback', '<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">

  <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{dd MMM HH:mm:ss.SSS} [%thread] %-5level %logger{10}:%-3line - %msg%n
      </pattern>
    </encoder>
  </appender>

  <appender name="db" class="ch.qos.logback.classic.db.DBAppender">
    <connectionSource class="ch.qos.logback.core.db.DriverManagerConnectionSource">
      <driverClass>org.postgresql.Driver</driverClass>
      <url>jdbc:postgresql://localhost:5432/logback</url>
      <user>postgres</user>
      <password></password>
    </connectionSource>
  </appender>

  <!-- wrap the DB appender in an async one, that means it won''t block when logging to the DB -->
  <appender name="db_async" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="db" />
    <discardingThreshold>0</discardingThreshold>
    <includeCallerData>true</includeCallerData>
  </appender>

  <timestamp key="bySecond" datePattern="yyyyMMdd''T''HHmmss"/>

  <!--================================-->
  <!--logging settings for development-->
  <!--================================-->

  <!-- only want ERRORs from these packages -->
  <logger name="ch.qos.logback" level="ERROR"/>
  <logger name="com.mchange" level="ERROR"/>
  <logger name="com.datastax" level="ERROR"/>
  <logger name="org.hibernate" level="ERROR"/>
  <logger name="io.netty" level="ERROR"/>

  <!-- enable TRACE logging for Endeavour code -->
  <logger name="org.endeavourhealth" level="TRACE"/>

  <!-- only log to stdout with INFO level -->
  <root level="INFO">
    <appender-ref ref="stdout" />
    <appender-ref ref="db_async" />
  </root>

  <!-- specify a shutdown hook for logging, so all loggers are flushed before app exit -->
  <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>

</configuration>' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'keycloak', '{
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
('eds-ui', 'keycloak_proxy_user', 'eds-ui' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'keycloak_proxy_pass', 'bd285adbc36842d7a27088e93c36c13e29ed69fa63a6' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'application', '{ "appUrl" : "http://localhost:8080" }');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'rabbit', '{
	"username" : "guest",
	"password" : "guest",
	"nodes"	: "127.0.0.1:15672"
}');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'routings', '[
{
	"uuid": "a5813d55-4264-443d-9550-c85712f44a74",
	"regex": "[A-M].*",
	"routeKey": "A-M",
	"name": "Default A-M",
	"description": "Default fallback group, initial character A-M"
},
{
	"uuid": "a0e84d9b-3f17-4954-ba7a-ceb34564cb84",
	"regex": "[N-Z].*",
	"routeKey": "N-Z",
	"name": "Default A-Z",
	"description": "Default fallback group, initial character N-Z"
},
{
	"uuid": "b94aac35-b667-45b7-b7c8-d9e8109f9da5",
	"regex": "[0-9].*",
	"routeKey": "0-9",
	"name": "Default 0-9",
	"description": "Default fallback group, initial character 0-9"
},
{
	"uuid": "1fa471a6-4ec8-4a89-8702-a5bae11f5e69",
	"regex": ".*",
	"routeKey": "Fallback",
	"name": "Default Fallback",
	"description": "Default fallback group, all remaining"
}
]');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'rePostMessageToExchangeConfig', '{
	"username" : "guest",
	"password" : "guest",
	"nodes" : ["localhost:5672"],
	"exchange" : "EdsInbound",
	"routingHeader" : "SenderLocalIdentifier"
}');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'logbackDb','{
   "url" : "jdbc:postgresql://localhost:5432/logback",
   "username" : "postgres",
   "password" : "Taz123"
}');