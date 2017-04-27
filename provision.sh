#!/usr/bin/env bash

## OS updates
sudo apt update && sudo apt dist-upgrade
sudo apt autoremove

## localization/i18n foo
sudo apt -y install language-pack-en

## JAVA openJDK 8 via apt
sudo apt -y install openjdk-8-jdk

## APACHE MAVEN - version in ubuntu repos was latest version anyway
sudo apt -y install maven

## POSTGRESQL 9.5 via apt
#https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-16-04
#sudo apt-get -y install postgresql postgresql-contrib

# RABBITMQ 3.6 from RabbitMQ repo NOT Ubuntu repos as they are out of date #(3.5.7)
## from https://www.rabbitmq.com/install-debian.html
#echo 'deb http://www.rabbitmq.com/debian/ testing main' |
     #sudo tee /etc/apt/sources.list.d/rabbitmq.list
#wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc |
     #sudo apt-key add -
#sudo apt-get update
#sudo apt-get install rabbitmq-server

## enable the management API
#sudo rabbitmq-plugins enable rabbitmq_management

## CASSANDRA 3.5 from ASF
# from https://www.digitalocean.com/community/tutorials/how-to-install-cassandra-and-ru#n-a-single-node-cluster-on-ubuntu-14-04

echo "deb http://www.apache.org/dist/cassandra/debian 22x main" | sudo tee -a #/etc/apt/sources.list.d/cassandra.sources.list
echo "deb-src http://www.apache.org/dist/cassandra/debian 22x main" | sudo tee #-a /etc/apt/sources.list.d/cassandra.sources.list

## add these 4 keys
#gpg --keyserver pgp.mit.edu --recv-keys F758CE318D77295D
#gpg --export --armor F758CE318D77295D | sudo apt-key add -
#gpg --keyserver pgp.mit.edu --recv-keys 2B5C1B00
#gpg --export --armor 2B5C1B00 | sudo apt-key add -
#gpg --keyserver pgp.mit.edu --recv-keys 0353B12C
#gpg --export --armor 0353B12C | sudo apt-key add -
#gpg --keyserver pgp.mit.edu --recv-keys A278B781FE4B2BDA
#gpg --export --armor A278B781FE4B2BDA | sudo apt-key add -

#sudo apt-get update
#sudo apt-get install cassandra

## TOMCAT 8
#https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-#on-ubuntu-16-04

#sudo groupadd tomcat
#sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat
#cd /tmp
curl -O http://apache.mirror.anlx.net/tomcat/tomcat-8/v8.5.12/bin/apache-tomcat-8.5.12.t#ar.gz
#sudo mkdir /opt/tomcat
#sudo tar xzvf apache-tomcat-8*tar.gz -C /opt/tomcat --strip-components=1
#sudo chgrp -R tomcat /opt/tomcat
#sudo chmod -R g+r conf
#sudo chmod g+x conf
#sudo chown -R tomcat webapps/ work/ temp/ logs/ conf/

## check JAVA_HOME path
#sudo update-java-alternatives -l

## create service
#sudo nano /etc/systemd/system/tomcat.service
#paste in (amend JAVA_HOME if required based on previous command)
#[Unit]
#Description=Apache Tomcat Web Application Container
#After=network.target
#[Service]
#Type=forking
#Environment=JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre
#Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
#Environment=CATALINA_HOME=/opt/tomcat
#Environment=CATALINA_BASE=/opt/tomcat
#Environment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true #-Djava.security.egd=file:/dev/./urandom'
#ExecStart=/opt/tomcat/bin/startup.sh
#ExecStop=/opt/tomcat/bin/shutdown.sh
#User=tomcat
#Group=tomcat
#UMask=0007
#RestartSec=10
#Restart=always
#[Install]
#WantedBy=multi-user.target

#Ctrl-O (save)
#Ctrl-X (exit)

#sudo systemctl daemon-reload
#sudo systemctl start tomcat
#sudo systemctl status tomcat

check Tomcat is up by visiting localhost:8080 on the host machine. #localhost:8080 is port-forwarded to the guest:8080

## remove all the apps in /opt/tomcat/webapps

## install nodejs (long term support version)
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.1/install.sh | #bash
#close & reopen shell or run
#export NVM_DIR="$HOME/.nvm"
#[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
#nvm ls-remote (if required to find out which is current LTS)
#nvm install 6.10.1 (in this case 6.10.1 was the current latest LTS version)

## build the applications
cd /vagrant (this dir /vagrant/ is shared outside the vm as the repo directory #you called vagrant up from)

#mvn clean package

## move the generated .war files to tomcat's webapp directory
#sudo cp src/eds-ui/target/eds-ui-1.0-SNAPSHOT.war /opt/tomcat/webapps/ROOT.war
sudo cp src/eds-messaging-api/target/eds-messaging-api-1.0-SNAPSHOT.war #/opt/tomcat/webapps/messaging.war

## copy keycloak .jars to /opt/tomcat/lib
TODO we need a place where these jars can be downloaded from (?can they be checked into Git with everything else? - is there private / proprietary #information in them)
#TODO copy command from wherever these jars end up to /opt/tomcat/lib

## run db setup scripts in Postgres
# (this bit is all wrong)
#sudo -u postgres psql
## at the `postgres=#` prompt, use the following commands to create the tables:
## config
#\i /vagrant/src/database/sql/config/schema/create_database.sql
#\i /vagrant/src/database/sql/config/schema/create_schema.sql
## eds
#\i /vagrant/src/database/sql/eds/schema/create_database.sql
#\i /vagrant/src/database/sql/eds/schema/create_tables.sql
## logback
#\i /vagrant/src/database/sql/logback/schema/create_database.sql
#\i /vagrant/src/database/sql/logback/schema/create_schema.sql
## reference
#\i /vagrant/src/database/sql/reference/schema/create_database.sql
#\i /vagrant/src/database/sql/reference/schema/create_tables.sql
## transform
#\i /vagrant/src/database/sql/transform/schema/create_database.sql
#\i /vagrant/src/database/sql/transform/schema/create_tables.sql

#\i /vagrant/src/database/sql/config/eds_ui_config.sql
#\i /vagrant/src/database/sql/config/enterprise_config.sql
#\i /vagrant/src/database/sql/config/global_config.sql
#\i /vagrant/src/database/sql/config/hl7receiver_config.sql
#\i /vagrant/src/database/sql/config/messaging_api_config.sql
#\i /vagrant/src/database/sql/config/patient_explorer_config.sql
#\i /vagrant/src/database/sql/config/queuereader_config.sql
#\i /vagrant/src/database/sql/config/sftpreader_config.sql

## things to have checked and harmonised with the production env
#config of tomcat
#permissions in /opt/tomcat
