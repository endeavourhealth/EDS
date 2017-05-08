# Endeavour Data Services

## Pre-requisites

You will need the following:

* [Virtualbox](https://www.virtualbox.org/)
* [Vagrant](www.vagrantup.com)
* [Git](https://git-scm.com/)

git clone
**Important** Make sure you are using the develop branch.
git checkout -b develop origin/develop
vagrant up

### Java
openJDK8 is provisioned automatically by Vagrant using apt-get

### Node.Js
### Bower
### TypeScript
Node and npm are installed via NVM, which is itself installed via a small script during the Vagrant provisioning. After this, bower and typescript are installed

### RabbitMQ
### Cassandra
### Tomcat
### postgreSQL
The above are all installed as their official Docker images by Docker Compose, during provisioning.

## Steps that are not yet automated

`vagrant ssh`
`mvn clean package`

Tomcat should be able to see the .war files where they are created by Maven, due to the volume sharing settings in docker-compose.yml

restart Tomcat docker container if needed

populate the database using the SQL scripts
connect to postgres in its container
`docker exec -it vagrant_postgres_1 psql -U postgres postgres`

 Postgres should be able to see the scripts where they are in the source, because of the volume sharing settings in docker-compose.yml

 
