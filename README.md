# Endeavour Data Services

## Pre-requisites

You will need the following:

* Java 1.8
* RabbitMQ 3.6
* Cassandra 3.5
* Tomcat 8 or 9

## Development Environment Setup

Clone this code repository to your local machine. The instructions below assume the target directory for Tomcat and artifacts is `/opt/eds`.

1. Install Java 1.8

2. Install Cassandra 3.5

3. Install RabbitMQ 3.6 and enable the management API with `rabbitmq-plugins enable rabbitmq_management`

4. Install Tomcat and remove all the sample webapps, including `ROOT`

5. Build the entire project:

```bash
mvn clean package
```
    
6. Copy the output war files to Tomcat:


```bash
cp src/eds-ui/target/eds-ui-1.0-SNAPSHOT.WAR /opt/eds/tomcat/webapps/ROOT.war
cp src/eds-message-api/target/eds-messaging-api-1.0-SNAPSHOT.WAR /opt/eds/tomcat/webapps/messaging.war
```

     
7. Start Tomcat and you should be able to access the admin UI at http://localhost:8080/

8. Send a test message (`src/eds-messaging-api/src/main/resources/Message.xml`) to the messaging API as follows:


```bash
curl -H "Content-Type: text/xml" --binary-message @src/eds-messaging-api/src/main/resources/Message.xml http://localhost:8080/messaging/api/PostMessageAsync
```