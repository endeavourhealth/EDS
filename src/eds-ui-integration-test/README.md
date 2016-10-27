# EDS Integration Testing

Integration tests on running instances using Apache HttpClient and Selenium.

## Getting started

1. Start the following:
    - Cassandra
    - RabbitMQ
    - Postgres
2. Keycloak must be running on port 9080
3. Build and run `eds-ui` on port 8080 on a Tomcat with the Keycloak adapter

## Running the integration tests

From the project root run:
 
    mvn test -P integrationTests -pl src/eds-ui-integration-test