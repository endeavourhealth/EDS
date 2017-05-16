# -*- mode: ruby -*-
# vi: set ft=ruby :

unless Vagrant.has_plugin?("vagrant-docker-compose")
  system("vagrant plugin install vagrant-docker-compose")
  puts "Dependencies installed, please try the command again."
  exit
end

Vagrant.configure("2") do |config|
  # I am using bento/ubuntu because of a bug in ubuntu/xenial64
  # info: https://bugs.launchpad.net/cloud-images/+bug/1569237
  config.vm.box = "bento/ubuntu-16.04"
  config.vm.box_check_update = true

  ## PORT FORWARDING SETTINGS
  # the 'autocorrect' flag corrects any port clashes on your local machine
  # (but can introduce inconsistency about on which port a given service is located)
  # EDS => normal port 80 web left forwarded, but changed from Vagrant's
  # default of 8080 to something else (so as not to clash with Tomcat)
  config.vm.network "forwarded_port", guest: 80, host: 4080, auto_correct: true
  # Tomcat (8080) on VM guest forwarded to tomcat:8080 on host
  config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true
  # PostgreSQL (5432) on VM guest forwarded to postgres:5432 on host
  config.vm.network "forwarded_port", guest: 5432, host: 5432, auto_correct: true
  # RabbitMQ Management web UI (15672) on VM guest forwarded to rabbitmq:15672 on host
  config.vm.network "forwarded_port", guest: 15672, host: 15672, auto_correct: true

  # config.vm.network "private_network", ip: "192.168.33.10"
  # config.vm.network "public_network"

  config.vm.synced_folder ".", "/vagrant"

  config.vm.provider "virtualbox" do |vb|
    # Display the VirtualBox GUI when booting the machine
    # vb.gui = true
    # Customize the amount of memory on the VM:
    vb.memory = "3072"
  end

  ## PROVISIONING

  # OSupdates
  # config.vm.provision :shell, inline: "sudo apt-get update"
  # config.vm.provision :shell, inline: "sudo apt-get upgrade"
  # config.vm.provision :shell, inline: "sudo apt-get -y autoremove"

  # java SDK
  config.vm.provision :shell, inline: "sudo apt-get -y install openjdk-8-jdk"
  config.vm.provision :shell, inline: "sudo apt-get -y install maven"

  # nodejs
  config.vm.provision "shell", path: "./install-node.sh"

  # install docker
  config.vm.provision :docker
  # install docker-compose (runs several containers and links them together automatically)
  # this installs tomcat, cassandra, postgres and rabbitmq in separate linked containers
  config.vm.provision :docker_compose, yml: "/vagrant/docker-compose.yml", rebuild: true, run: "always"

  # remove the default contents of Tomcat's webapp directory 
  config.vm.provision :shell, inline: "docker exec vagrant_tomcat_1 rm -rf /usr/local/tomcat/webapps/ /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/docs /usr/local/tomcat/webapps/host-manager /usr/local/tomcat/webapps/manager /usr/local/tomcat/webapps/examples ;true" # this command raises an error, although it has worked, ;true suppresses the error

  # TODO: postgres DB scripts here
  # TODO: cassandra DB scripts here
  # TODO: MySQL DB scripts here
end
