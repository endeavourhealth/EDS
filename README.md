# Endeavour Data Services

<img src="https://docs.google.com/drawings/d/1_sSB_SM9BU1ju5Q21TSYhI-ekC0Nf-Aoto3v_2gACWo/pub?w=936&amp;h=547">

## Pre-requisites

You will need the following installed on your development machine
* [Virtualbox](https://www.virtualbox.org/)
* [Vagrant](www.vagrantup.com)
* [Git](https://git-scm.com/)
* enough RAM to run a 3Gb Virtualbox machine

`git clone git@github.com:endeavourhealth/EDS.git`
**Important** Make sure you are using the develop branch.
git checkout develop
vagrant up

### Java
openJDK8 and Maven is provisioned automatically to the Vagrant box using apt-get, so that the build can take place in the standardised environment of the VM. (There's nothing stopping you doing the build on your development machine, but therein is created some opportunity for inconsistency or hard-to-debug errors due to Java and Maven versions)

### Node.Js, Bower, TypeScript
Node and npm are installed via NVM, which is itself installed via a small script during the Vagrant provisioning. After this, bower and typescript are installed.

### RabbitMQ, 2-node Cassandra cluster, Tomcat, postgreSQL
The above are all installed as their official Docker images by Docker Compose, during provisioning. Data volumes are created in order to allow the service containers to be stateless.

## Steps that are not yet automated
`vagrant ssh`
`mvn clean package`
Tomcat should be able to see the .war files where they are created by Maven, due to the volume sharing settings in docker-compose.yml
restart Tomcat docker container if needed
populate the database using the SQL scripts
connect to postgres in its container
`docker exec -it vagrant_postgres_1 psql -U postgres postgres`
Postgres should be able to see the scripts where they are in the source, because of the volume sharing settings in docker-compose.yml
