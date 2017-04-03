# EDS Authentication

The EDS UI and APIs are secured using an external Identity Server using OpenID Connect and UMA.

## Pre-requisites for development

- Git
- Java 1.8
- Maven 3.3
- Keycloak 2.0.0
- Tomcat 8.0.36
- Keycloak Tomcat 8 Adapter

The following tools are also useful:

- IntelliJ Ultimate (online recompiling of TypeScript in to JavaScript)
- Docker

## Reading List

It is highly recommended that you read the following:

- [Keycloak Tomcat 8 Adapter](https://keycloak.gitbooks.io/securing-client-applications-guide/content/v/2.0/topics/oidc/java/tomcat-adapter.html)
- [Java Security Context](https://keycloak.gitbooks.io/securing-client-applications-guide/content/v/2.0/topics/oidc/java/adapter-context.html)
- [Clustering Guide](https://keycloak.gitbooks.io/securing-client-applications-guide/content/v/2.0/topics/oidc/java/application-clustering.html)
- [Java Adapter Config](https://keycloak.gitbooks.io/securing-client-applications-guide/content/v/2.0/topics/oidc/java/java-adapter-config.html)
- [JavaScript Adapter Config](https://keycloak.gitbooks.io/securing-client-applications-guide/content/v/2.0/topics/oidc/javascript-adapter.html)

## Development environment setup

The quickest way to get a development environment running is to download the following files:

- [Packaged and configured Keycloak for EDS](http://s3.amazon.com/...)
- [Packaged and configured Tomcat 8.0.36 with Keycloak adapter](http://s3.amazon.com/...)

### Getting Keycloak

Download the latest release of Keycloak:

[https://downloads.jboss.org/keycloak/2.0.0.Final/keycloak-2.0.0.Final.tar.gz](https://downloads.jboss.org/keycloak/2.0.0.Final/keycloak-2.0.0.Final.tar.gz)

Start Keycloak and setup the master user. Create a realm called `endeavour` and set up a **public** client called `eds`.

### Configuring Tomcat 8 for development

Download and extract the latest release (currently 8.0.36) of Tomcat from the Apache Foundation's website:

[https://tomcat.apache.org/download-80.cgi](https://tomcat.apache.org/download-80.cgi)

*Do not use Tomcat 8.54 as there are some library compatibility issues with the Keycloak adapter!*

Download the Keycloak Tomcat 8 adapter:

[https://downloads.jboss.org/keycloak/2.0.0.Final/adapters/keycloak-oidc/keycloak-tomcat8-adapter-dist-2.0.0.Final.tar.gz](https://downloads.jboss.org/keycloak/2.0.0.Final/adapters/keycloak-oidc/keycloak-tomcat8-adapter-dist-2.0.0.Final.tar.gz)

Extract the adapter and it contains the following jars:

    bcpkix-jdk15on-1.52.jar                       httpcore-4.4.1.jar                            keycloak-adapter-core-2.0.0.Final.jar         keycloak-tomcat-core-adapter-2.0.0.Final.jar
    bcprov-jdk15on-1.52.jar                       jackson-annotations-2.5.4.jar                 keycloak-adapter-spi-2.0.0.Final.jar          keycloak-tomcat8-adapter-2.0.0.Final.jar
    commons-codec-1.9.jar                         jackson-core-2.5.4.jar                        keycloak-common-2.0.0.Final.jar
    commons-logging-1.2.jar                       jackson-databind-2.5.4.jar                    keycloak-core-2.0.0.Final.jar
    httpclient-4.5.jar                            jboss-logging-3.3.0.Final.jar                 keycloak-tomcat-adapter-spi-2.0.0.Final.jar

Delete `commons-logging-1.2.jar` as it conflicts with Logback Classic in EDS and will prevent logging.

Copy the rest of the jars in the Tomcat lib directory: `<TOMCAT_8_HOME>/lib`.

This Tomcat is now ready to deploy the EDS war files to, either as archives or exploded by IntelliJ. The configuration needed for Keycloak is found each project's `META-INF` and `resources/keycloak.json` files.

### Running Keycloak in Development

It is a good idea to run the Keycloak server with a port offset so that it doesn't conflict with Tomcat. All pre-configured packages use an offset of `1000` so Keycloak will appear at [http://localhost:9080/auth](http://localhost:9080/auth):

    <KEYCLOAK_HOME>/bin/standalone.sh -Djboss.socket.binding.port-offset=1000

This will leave port 8080 to run Tomcat and the EDS applications.

### Keycloak Theme Development

Keycloak has good documentation explaining theme development here [https://keycloak.gitbooks.io/server-developer-guide/content/v/2.0/topics/themes.html](https://keycloak.gitbooks.io/server-developer-guide/content/v/2.0/topics/themes.html) and examples here [https://github.com/keycloak/keycloak/tree/master/examples/themes](https://github.com/keycloak/keycloak/tree/master/examples/themes).

In the Keycloak there is a directory `<KEYCLOAK_HOME>/themes` with the default themes - copy the `keycloak` theme to `eds` and adjust the templates and CSS as necessary:

    ├── README.txt
    ├── eds
    │   ├── account
    │   │   ├── resources
    │   │   └── theme.properties
    │   ├── common
    │   │   └── resources
    │   ├── email
    │   │   └── theme.properties
    │   └── login
    │       ├── login.ftl
    │       ├── resources
    │       ├── template.ftl
    │       └── theme.properties
    └── keycloak
        ├── account
        │   ├── resources
        │   └── theme.properties
        ├── admin
        │   ├── resources
        │   └── theme.properties
        ├── common
        │   └── resources
        ├── email
        │   └── theme.properties
        ├── login
        │   ├── resources
        │   └── theme.properties
        └── welcome
            ├── index.ftl
            └── resources

It is a good idea to turn off theme caching during development by changing `<KEYCLOAK_HOME>/standalone/configuration/keycloak-server.json`:

    "theme": {
        ...
        "staticMaxAge": -1,
        "cacheTemplates": false,
        "cacheThemes": false,
        ...
    }

Changes made to the themes should reflect immediately and remember to enable the `eds` theme for the `endeavour` realm.

## Development Tips

### JEE Security and JAX-RS Annotations

Keycloak handles these mechanisms of authourisation **differently** but they can be made to work together.

Firstly, the JEE Security Settings must be set for the protected API and it is recommended that there is a base role that is required to access the protected API generally. Any unauthenticated APIs should be exposed with a second servlet. Unfortunately this is a limitation that cannot be worked around currently.

#### Protected API (web.xml)

        <!-- protected resources that require auth -->
        <servlet>
            <servlet-name>Endeavour Data Service</servlet-name>
            <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
            <init-param>
                <param-name>jersey.config.server.provider.packages</param-name>
                <param-value>org.endeavourhealth.ui.endpoints</param-value>
            </init-param>
            <init-param>
                <param-name>jersey.config.server.provider.classnames</param-name>
                <param-value>org.glassfish.jersey.jackson.JacksonFeature,org.endeavourhealth.core.security.CustomRolesAllowedFeature</param-value>
            </init-param>
    
            <!-- enable directory listings -->
            <init-param>
                <param-name>listings</param-name>
                <param-value>true</param-value>
            </init-param>
    
            <load-on-startup>1</load-on-startup>
        </servlet>
    
        <servlet-mapping>
            <servlet-name>Endeavour Data Service</servlet-name>
            <url-pattern>/api/*</url-pattern>
        </servlet-mapping>
        
        <!-- ============================================ -->
        <!-- JEE security settings - Keycloak integration -->
        <!-- ============================================ -->
    
        <!-- set the class to provide keycloak.json -->
        <context-param>
            <param-name>keycloak.config.resolver</param-name>
            <param-value>org.endeavourhealth.core.security.keycloak.KeycloakConfigResolverImpl</param-value>
        </context-param>
    
        <!-- API is private and requires the user to authenticate and be an EDS user -->
        <security-constraint>
            <web-resource-collection>
                <web-resource-name>api</web-resource-name>
                <url-pattern>/api/*</url-pattern>
            </web-resource-collection>
            <auth-constraint>
                <role-name>eds_user_professional</role-name>
            </auth-constraint>
        </security-constraint>
    
        <!-- use Keycloak authentication valve installed in the container -->
        <login-config>
            <auth-method>KEYCLOAK</auth-method>
            <realm-name>Endeavour</realm-name>
        </login-config>
    
        <!-- all roles in the system MUST be specified here -->
        <security-role>
            <role-name>eds_user_professional</role-name>
        </security-role>        

#### Public API (web.xml)

        <!-- public resources, no auth needed -->
        <servlet>
            <servlet-name>Public Resources</servlet-name>
            <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
            <init-param>
                <param-name>jersey.config.server.provider.packages</param-name>
                <param-value>org.endeavourhealth.ui.endpoints_public</param-value>
            </init-param>
            <init-param>
                <param-name>jersey.config.server.provider.classnames</param-name>
                <param-value>org.glassfish.jersey.jackson.JacksonFeature</param-value>
            </init-param>
    
            <load-on-startup>1</load-on-startup>
        </servlet>
    
        <servlet-mapping>
            <servlet-name>Public Resources</servlet-name>
            <url-pattern>/public/*</url-pattern>
        </servlet-mapping>

#### JAX-RS Annotations

JAX-RS annotations are enabled by adding `org.endeavourhealth.core.security.CustomRolesAllowedFeature` to the `web.xml`:

    <init-param>
        <param-name>jersey.config.server.provider.classnames</param-name>
        <param-value>org.glassfish.jersey.jackson.JacksonFeature,org.endeavourhealth.core.security.CustomRolesAllowedFeature</param-value>
    </init-param>

This *dynamic feature* scans all resources for the following annotations:

    org.endeavourhealth.core.security.annotations.*
    javax.annotation.security.RolesAllowed
    javax.annotation.security.RolesDenied
    javax.annotation.security.PermitAll
    javax.annotation.security.DenyAll

See the `RequiresAdmin` annotation for how you can create your own, but they must be in the `org.endeavourhealth.core.security.annotations` package to work with the automatic package scanning.

You can then secure you API method as follows:

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveConfig")
    @RequiresAdmin <==========================
    public Response saveConfig(@Context SecurityContext sc, ConfigurationResource configurationResource) {
        ...
    }

**Remember that the JEE role applies *first* and then the JAX-RS annotation role applies after that!**

If you are having trouble with the Keycloak authorisation valve, it is best to enable logging for all `org.keycloak` packages in the Tomcat `log4j` settings - doing in your webapp won't show the logging as the authentication valve is loaded into the container.

#### Keycloak Java Client

You can use the `keycloak-admin-client` for functions:

- interact with the Keycloak Admin REST API
- as an OAuth bearer token client (an alternative is the [Apache OLTU library](https://oltu.apache.org/))

You can create realms, users, roles, etc after you have instantiated the admin client as follows:

    import org.keycloak.admin.client.Keycloak;
    import org.keycloak.representations.idm.RealmRepresentation;
    ...
    
    Keycloak keycloak = Keycloak.getInstance(
        "http://localhost:8080/auth",
        "master",
        "admin",
        "password",
        "admin-cli");
    RealmRepresentation realm = keycloak.realm("master").toRepresentation();

Remember that the token you obtain from the client is **only for the *Admin REST API*** - if you try to use the token with other Keycloak realms, access will be denied with no useful logging. This is because the Keycloak client you are using is `admin-cli`. If you want to authenticate with the library for another realm, create another client, that is `public` or `confidential` depending on your needs. 

## Production Installation Considerations

In production the following things should be considered:

- All keys and keystores should be regenerated and no development configuration files should be used.
- Keycloak should be reverse proxied through nginx or Apache (or similar load balancing appliance) that will also handle SSL termination (including Client Certificate Authentication).
- Real DNS entries and valid certificates should be used.
- Storage should be in Postgres and *not* the embedded H2 database - is not suitable for production.
- The HTTP session should be shared between nodes in the cluster: either with the Tomcat Postgres connector or using Infinispan.
- The Identity Servers should be as close as possible to the application servers with the most load to reduce authentication latency.
- LDAP users used for SFTP authentication should be stored in a clustered LDAP instance - OpenLDAP can be configured to run in a master-master replica pair.
- Server logs from both Tomcat and Keycloak should be collected using FileBeats and sent to LogStash.

Use the [EDS bootstrapping tool](src/eds-bootstrap) to set the configuration and [see instructions for usage here](src/eds-bootstrap/README.me).  

# Installation Guide

- Download Keycloak 2.0.0.Latest
- Unzip contents into `/opt/jboss/keycloak`
- Make sure you have the following build artifacts:
    - `eds-keycloak-providers.jar`
    - `eds-keycloak-theme-1.0-SNAPSHOT.jar`
- Setup the theme by unziping the theme contents to `/opt/jboss/keycloak/themes`
- Set the custom providers:

    ```
    # Add custom providers
    /opt/jboss/keycloak/bin/jboss-cli.sh --command="module add --name=org.endeavourhealth.eds-keycloak-providers --resources=<ARTIFACT_DIR>/eds-keycloak-providers.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-common,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-services,org.jboss.resteasy.resteasy-jaxrs,org.jboss.logging,javax.ws.rs.api"
    
    # Modify the keycloak-server.json file (NB: after Keycloak v2.1.0 this will need to be done in the standalone.xml file)
    jq '.providers |= .+ ["module:org.endeavourhealth.eds-keycloak-providers"]' /opt/jboss/keycloak/standalone/configuration/keycloak-server.json > /tmp/keycloak-server.json
    cp /tmp/keycloak-server.json /opt/jboss/keycloak/standalone/configuration/keycloak-server.json
    ```

- Set up JDBC:
    - Run

        ```bash
        xmlstarlet ed --inplace -N ds=urn:jboss:domain:datasources:4.0 -s '//ds:datasource[@jndi-name="java:jboss/datasources/KeycloakDS"]/ds:connection-url' -t text -n '' -v "jdbc:postgresql://$POSTGRES_HOST/" -s '//ds:datasource[@jndi-name="java:jboss/datasources/KeycloakDS"]/ds:security/ds:user-name' -t text -n '' -v "$POSTGRES_USER" -s '//ds:datasource[@jndi-name="java:jboss/datasources/KeycloakDS"]/ds:security/ds:password' -t text -n '' -v "$POSTGRES_PASSWORD" /opt/jboss/keycloak/standalone/configuration/standalone.xml
        ```

    - Download `https://jdbc.postgresql.org/download/postgresql-9.4.1209.jar` to `/opt/jboss/keycloak/modules/org/postgresql/main/`
    - Create this file in `/opt/jboss/keycloak/modules/org/postgresql/main/module.xml`

        ```xml
        <module xmlns="urn:jboss:module:1.3" name="org.postgresql">
           <resources>
              <resource-root path="postgresql-9.4.1209.jar"/>
           </resources>
           <dependencies>
              <module name="javax.api"/>
              <module name="javax.transaction.api"/>
           </dependencies>
        </module>
        ```

- Make sure `Host` headers are used by setting `proxy-address-forwarding="true"` in `standalone.xml`:

    ```xml
    <subsystem xmlns="urn:jboss:domain:undertow:3.0">
        <buffer-cache name="default"/>
        <server name="default-server">
            <http-listener name="default" proxy-address-forwarding="true" socket-binding="http" redirect-socket="https"/>
            <host name="default-host" alias="localhost">
                <location name="/" handler="welcome-content"/>
                <filter-ref name="server-header"/>
                <filter-ref name="x-powered-by-header"/>
            </host>
        </server>
    ```
    
Start the Keycloak server with `/opt/jboss/keycloak/bin/standalone.sh` for testing, but this should really be run as a service - configure this as appropriate for your Linux distribution.