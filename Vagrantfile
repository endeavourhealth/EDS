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
  # Tomcat (8080) on VM guest forwarded to localhost:8080 on host
  config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true
  # PostgreSQL (5432) on VM guest forwarded to localhost:5432 on host
  config.vm.network "forwarded_port", guest: 5432, host: 5432, auto_correct: true

  # config.vm.network "private_network", ip: "192.168.33.10"
  # config.vm.network "public_network"

  config.vm.synced_folder ".", "/vagrant"

  config.vm.provider "virtualbox" do |vb|
    # Display the VirtualBox GUI when booting the machine
    # vb.gui = true
    # Customize the amount of memory on the VM:
    vb.memory = "2048"
  end

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  ## PROVISIONING
  $sagd = <<SCRIPT
  DEBIAN_FRONTEND=noninteractive \
  sudo apt-get \
  -o Dpkg::Options::="--force-confnew" \
  --force-yes \
  -fuy \
  dist-upgrade
SCRIPT

  # updates
  config.vm.provision :shell, inline: "sudo apt-get update"
  config.vm.provision :shell, inline: $sagd
  config.vm.provision :shell, inline: "sudo apt-get -y autoremove"
  # java SDK
  config.vm.provision :shell, inline: "sudo apt-get -y install openjdk-8-jdk"
  config.vm.provision :shell, inline: "sudo apt-get -y install maven"
  # nodejs
  config.vm.provision "shell", path: "./install-node.sh"
  # install docker
  config.vm.provision :docker
  # install docker-compose (runs several containers and links them together automatically)
  # this installs tomcat, cassandra, postgres and rabbitmq in separate containers
  config.vm.provision :docker_compose, yml: "/vagrant/docker-compose.yml", run: "always"
  # config.vm.provision "shell", path: "./provision.sh"
end
