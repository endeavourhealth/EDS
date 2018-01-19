![Endeavour Logo](http://www.endeavourhealth.org/github/logo-text-left-cropped.png)

# Endeavour Data Service

## Pre-requisites

You will need the following:

* Java 8
* RabbitMQ 3.6
* MySQL and MySQL Workbench
* Tomcat 8 or 9
* Git
* Node.js
* IntelliJ (Ultimate edition)
* Bower
* Typescript
* TSLint


### Java
Download and install the Java SDK (minimum required is 8u102) from [here] (http://www.oracle.com/technetwork/java/javase/downloads/index.html "Java Download")

### RabbitMQ
Download and run the rabbitMQ installer.  If you do not have ERLANG installed you will need to install this first but it will warn you when you try to install RabbitMQ.
Once RabbitMQ is installed, using the command line, navigate to rabbitmq\sbin folder. 
```bash
cd c:\Program Files\RabbitMQ Server\rabbitmq_server-3.6.6\sbin
rabbitmq-plugins enable rabbitmq_management
```
**Note:** If you get a ERLANG_HOME is not set correctly error, ensure your environment variable ERLANG_HOME is set to the erl8.2\bin\ folder. It defaulted to just the erl8.2 folder when I installed it.
Then run the following to enable the plug-ins
```bash
rabbitmq-service.bat stop 
rabbitmq-service.bat install 
rabbitmq-service.bat start
```
Finally, navigate to [here] (http://localhost:15672/#/ "Rabbit MQ") and use a username and password of 'guest' to login and you should see an overview page.  This means RabbitMQ is installed correctly.

### MySQL and MySQL Workbench
Download MySQL and MySQL Workbench from https://dev.mysql.com/downloads/installer/ then install it

Once installed, restore the sample databases found at https://1drv.ms/u/s!AhdraGkSN4_agdYZB2A9tSB_lotG8w
Inside this zip file is an "Instructions.txt" file, which contains steps on how to restore the DBs from the zip - follow this

### Tomcat
To use KeyCloak, a modified version of Tomcat is available [here] (https://drive.google.com/file/d/0B7zLWmSZKB2LY2J0TV9kZW5Vd0k/view?pref=2&pli=1 "key cloak modified tomcat")

Download this zip file and unzip somewhere and remove all the sample webapps, including `ROOT`

### Git
[Download Git] (http://git-scm.com/ "Get Git")

### Node.Js
[Download NodeJS] (https://nodejs.org/en/ "Get Node")

### IntelliJ
Note, you will need a GitHub account with permissions to view our two repos (https://github.com/endeavourhealth-discovery and https://github.com/endeavourhealth), so create a GitHub account if you don't already have one, and ask a team member to invite you into the repos.
 
* [Download IntelliJ] (https://www.jetbrains.com/idea/ "IntelliJ")
* Start up IntelliJ and select Check out from Version Control -> GitHub
* Enter your Git username and password if prompted
* Select GitHub, then enter the URL for the "EDS" repo (found on GitHub by viewing the repo and clicking the "Clone or Download" button)
* Select the destination folder on your local machine and click Clone
* It should now clone the repo
* It will ask if you want to create it as a Maven Project - answer yes to this
* It may ask if you want to enable auto imports - answer yes to this too
* It should then download all the dependent libraries, which may take some time on slow internet connections
* Once complete, you should be able to view the source code in the "Project" pane (Alt+1 to display if not already shown)
* You can repeat the above few steps from within IntellI using New -> Project from Version Control -> GitHub

### Bower
use cmd to run the following.
```bash
npm install -g bower
```
### TypeScript
Install TypeScript with the following
```bash
npm install -g typescript
```
Then install the typings definition manager with the following commands
```bash
npm install typings --global
typings install debug --save
```
We need to install the typescript module too (note, we specifically want the tslint 2.5.1 version because subsequent versions removed rules that are used by the Enterprise tslint.json):
```bash
npm install tslint@2.5.1 typescript
```
To manually run the TypeScript compiler, from the command line, simply navigate to the root of the web folder and type:
```bash
tsc
```
### TSLint
TSLint is required for the IntelliJ typescript compiler
To enable TSLint in IntelliJ, open Settings->Languages & Frameworks->Node.js and NPM
Click the green + button
Wait for a while, then you should find TSLint in the list of available packages
Click “Install Package” then close the dialog when installation completes
Then go to Settings->Languages & Frameworks->Typescript->TSLint
Make sure it’s enabled and select the option in the TSLint package combo
Ok the dialog


## Development Environment Setup

Clone this code repository to your local machine. The instructions below assume the target directory for Tomcat and artifacts is `/opt/eds`.



1. Install all pre-requisites as described above

2. Setup Typings for individual Web Apps
    
    Each web app that uses typescript needs to have bower and typings set up. So, for each of these folders, run these two commands: 
    ```bash
    typings install
    bower install 
    ```
    in the following locations
    ..\EDS\src\eds-ui\src\main\frontend
    ..\EDS\src\eds-patient-ui\src\main\frontend
    
3. Create Run Configurations for Web Apps (EDS-UI, EDS-Patient-Explorer and EDS-Messaging-API).
    
    For each of the three artifacts (**eds-ui:war exploded**, **eds-patient-explorer:war exploded** and **eds-messaging-api:war exploded**), do the following:
    * Open Run->Edit Configurations.
    * Click +, then Tomcat Server->Local to create the configuration. 
    * Name the configuration
    * In the Application Server field, select the Tomcat folder you unzipped
    * Select the Deployment tab 
    * Click + and select the relevant artifact
    * Select the Startup/Connection tab
    * Add the environment variable CONFIG_JDBC_USERNAME with your PostgreSQL user (postgres) for both Debug and Run configurations    
    * Add the environment variable CONFIG_JDBC_PASSWORD with your PostgreSQL password for both Debug and Run configurations
    * Add the environment variable CONFIG_JDBC_URL with the value jdbc:postgresql://localhost:5432/config
    * Click OK

4. Create Run Configuration for SFTP Reader
   
   To set up the run configuration for the SFTP Reader:
    * Open Run->Edit Configurations
    * Add a new Application configuration
    * Select org.endeavourhealth.sftpreader.Main as the main class
    * Select eds-sftpreader as the module
    * Add the environment variable CONFIG_JDBC_USERNAME with your PostgreSQL user (postgres) for both Debug and Run configurations    
    * Add the environment variable CONFIG_JDBC_PASSWORD with your PostgreSQL password for both Debug and Run configurations
    * Add the environment variable CONFIG_JDBC_URL with the value jdbc:postgresql://localhost:5432/config    
    * set the VM Options to -DINSTANCE_NAME=TEST001    
    * Click OK.

5. Additional steps to get SFTP Reader running
    Get the instance name used above by running the following in the sftpreader DB in postgreSQL (TEST001 is default)
    ```sql
    select * from configuration.configuration
    ```
    Create a folder on your local hard drive to store messages from the SFTP server and set local_root_path to this folder by using the following
    ```sql
    update configuration.configuration set local_root_path = 'C:\Endeavour\sftpData'     
    ```    
    Set the postgres password in the config table in the config DB
    ```sql
    update config set config_data = 'xxxxx' where app_id = 'sftpreader' and config_id = 'postgres-password'
    ```
    If you get errors when you run the application relating to audit, this is because the data we have does not have any audit information in it.  To get around this temporarily, delete the file_type_identifiers from the configuration.interface_file_type table in the sftpreader DB.
    ```sql
    delete from configuration.interface_file_type where file_type_identifier = 'Audit_RegistrationAudit'
    delete from configuration.interface_file_type where file_type_identifier = 'Audit_PatientAudit'
    ```
6. Create Run Configuration for the inbound queue reader

    To set up the run configuration for the Inbound Queue Reader:
     * Open Run->Edit Configurations
     * Add a new Application configuration
     * Select org.endeavourhealth.queuereader.Main as the main class
     * Select eds-queuereader as the module
     * Add the environment variable CONFIG_JDBC_USERNAME with your PostgreSQL user (postgres) for both Debug and Run configurations    
     * Add the environment variable CONFIG_JDBC_PASSWORD with your PostgreSQL password for both Debug and Run configurations
     * Add the environment variable CONFIG_JDBC_URL with the value jdbc:postgresql://localhost:5432/config    
     * set the program arguments to inbound    
     * Click OK.

7.  Additional steps to get the inbound queue reader running  
    
    Amend the config_data in the config table in the config DB for the queue reader.  
    Replace the SharedStoragePath value with the location of your shared storage path as set above in step 5.  
      This will also change the queue to EdsInbound-A-M from EdsInbound-GPs_Eng_Wls which is required.
    ```sql
    update config set config_data = '<?xml version="1.0" encoding="UTF-8"?>
     <QueueReaderConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/QueueReaderConfiguration.xsd">
         <!--<Queue>EdsInbound-GPs_Eng_Wls</Queue>-->
         <Queue>EdsInbound-A-M</Queue>
         <Pipeline>
             <PostMessageToLog>
                 <EventType>Transform_Start</EventType>
             </PostMessageToLog>
             <MessageTransformInbound>
                 <SharedStoragePath>C:\Endeavour\sftpData</SharedStoragePath>
                 <FilingThreadLimit>10</FilingThreadLimit>
             </MessageTransformInbound>
             <PostMessageToLog>
                 <EventType>Transform_End</EventType>
             </PostMessageToLog>
             <!--<PostMessageToExchange>
                 <Exchange>EdsResponse</Exchange>
                 <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
             </PostMessageToExchange>-->
             <PostMessageToExchange>
                 <Exchange>EdsProtocol</Exchange>
                 <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
                 <MulticastHeader>BatchIds</MulticastHeader>
             </PostMessageToExchange>
         </Pipeline>
     </QueueReaderConfiguration>'
    ```
 
## Application Information

### EDS-UI

This is a web front end application that allows you to configure the EDS system.  
Launch by choosing the EDS_UI run configuration and clicking the green Run Arrow.

This should deploy the artifact and obtain the keycloack config from the postgreSQL table viewable in the server log.  
It should then open a browser to localhost:8080 and give you a log in screen. Details are:  
professional  
Test1234

Once logged in it should present you with a dashboard.

**To configure EDS_UI for intial population**  
The initial test data has data for 2 practices that are not configured as standard.  To add these, repeat the following for both EMIS99 and D82027:
* click on organisations in the left hand menu
* click add new
* Enter National Id (EMIS99, D82027) and give it a name
* Save and Close
* On the left menu select Services
* Enter the local Identifier and Name for the organisations (EMIS99, D82027)
* Click Add next to Endpoints
* Choose EMIS Web Test as the System
* Choose EMISExtractService as the Technical Interface
* Leave endpoint as its default value (http://)
* Click Edit next to Organisations and press enter in the search criteria leaving it blank
* Choose the corresponding organisation from the matches list

**RabbitMQ syncronisation**  
To ensure EDS-UI is communicating with RabbitMQ correctly, go to the dashboard and you should see a load of queues listed and a green Node Status.  
Click on queuing on the left hand menu and check that all the queues have green ticks next to them.  
If they have red pluses instead, click the sync button and they should all turn green.

### SFTP Reader

This application connects to the Secure FTP site and polls for new messages then downloads them and processes them.  
It splits up the messages as they can be really large in live (>1GB). It then sends the messages to the EDS-Messaging-API

If messages need to be reprocessed run the following script in postgreSQL
```sql
update log.batch_split set have_notified = false, notification_date = null
```
Then run the SFTP reader again and it should reprocess the messages.
### EDS-Messaging-API

This application accepts the messages from the SFTP Reader and puts the messsages on the RabbitMQ queue.

### Inbound Queue Reader (pipeline)

This application takes the messages from the RabbitMQ queue and files the patient data into Cassandra.

### Patient explorer

This is a web front end used by care professionals to view patient data.  
This should deploy the artifact and obtain the keycloack config from the postgreSQL table viewable in the server log.  
It should then open a browser to localhost:8080 and give you a log in screen. Details are:  
professional  
Test1234

## Process Flow

Run EDS-Messaging-API and wait for it to initialise.  It will open a browser with an error but there is no front end to this application so this can be just closed down.  
Run STFP Reader which will pull down the files from the SFTP and send them across to the EDS-Messaging-API.  

Look at the server log in the SFTP Reader for any errors, you should see a lot of entries advising you of what is being done.  You are looking for a message that says something like successful messages sent = 2, failed = 0.  
Once that is done, you can stop the SFTP Reader and EDS-Messaging-API.  
  
Next, run the Inbound Queue Reader which will take the messages from the RabbitMQ queue, process them and populate the cassandra database.  This will take a while to finish.  

Finally, run the Patient Explorer, choose the relevant organisation and search for a patient.  
Patient explorer gets its list of organisations from Keycloak explorer based on which services the user has access to.







