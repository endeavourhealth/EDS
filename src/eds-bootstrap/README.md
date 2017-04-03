# EDS Bootstrap Tool

    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    THIS TOOL WILL DELETE ANY EXISTING REALMS, SO USE WITH CAUTION!!!

    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


This tool bootstraps the following configuration in EDS:

- Keycloak
    - realms
    - default users
    - roles
- Cassandra configuration
    - Keycloak client settings for
        - eds-ui
        - eds-sftpreader
        
# Configuration

An example configuration file can be found in `src/examples/bootstrap.json` and you can use it as a base for your own configuration file:

    {
      "cassandra" : {
        "url" : "cassandra", <---------------------------- CHANGE
        "port" : 9042,
        "username" : "bob", <----------------------------- CHANGE
        "password" : "123456789" <------------------------ CHANGE
      },
      "keycloak" : {
        "server" : {
          "serverUrl":"http://127.0.0.1:9080/auth", <----- CHANGE
          "realm":"master",
          "username":"sue", <----------------------------- CHANGE
          "password":"123456789", <----------------------- CHANGE
          "clientId":"admin-cli"
        },
        "realm" : "endeavour"
      }
    }

# Running the bootstrap tool:

Run the tool as follows:

    EDS/src/eds-bootstrap$> ./target/appassembler/bin/eds-bootstrap bootstrap.json
    
You will need to supply the `bootstrap.json` file for your environment.


