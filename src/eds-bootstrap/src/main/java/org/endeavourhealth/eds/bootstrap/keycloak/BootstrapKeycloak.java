package org.endeavourhealth.eds.bootstrap.keycloak;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.endeavourhealth.eds.bootstrap.Bootstrap;
import org.endeavourhealth.eds.bootstrap.models.Config;
import org.endeavourhealth.eds.common.CassandraClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.CustomKeycloak;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BootstrapKeycloak implements Bootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapKeycloak.class);

    private CassandraClient cassandraClient;

    @Override
    public void bootstrap(Config config) {

        LOG.info("Bootstrapping...");

        // TODO: remove this when RestEasy uses the new HttpClient that doesn't have SNI problems
        try {
            KeycloakClient.trustAllCertificates();
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyManagementException e) {
        }

        // TODO: remove this when RestEasy uses a newer version of HttpClient
        ResteasyClient resteasyClient = (new ResteasyClientBuilder()).disableTrustManager().connectionPoolSize(10).build();
        Keycloak client = new CustomKeycloak(
                config.getKeycloak().getServer().getServerUrl(),
                config.getKeycloak().getServer().getRealm(),
                config.getKeycloak().getServer().getUsername(),
                config.getKeycloak().getServer().getPassword(),
                config.getKeycloak().getServer().getClientId(),
                null, "password", resteasyClient);

        LOG.info("Connected to Keycloak version {}...", client.serverInfo().getInfo().getSystemInfo().getVersion());

        cassandraClient = new CassandraClient();
        cassandraClient.connect(config);

        if(cassandraClient.getSession() == null || cassandraClient.getSession().isClosed()) {
            LOG.error("Failed to connect to Cassandra...");
            return;
        }

        String newRealm = config.getKeycloak().getRealm();

        LOG.info("Deleting realm '{}'...", newRealm);
        try {
            client.realms().realm(newRealm).remove();
        } catch(Exception e) {
            LOG.warn("Realm does not exist or failed to delete...");
        }

        LOG.info("Adding realm '{}'...", newRealm);

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(newRealm);
        realmRepresentation.setDisplayName("Endeavour Health");
        realmRepresentation.setDisplayNameHtml("Endeavour Health");
        realmRepresentation.setEnabled(true);

        realmRepresentation.setAccountTheme("eds");
        realmRepresentation.setLoginTheme("eds");
        realmRepresentation.setEmailTheme("eds");

        realmRepresentation.setRegistrationAllowed(false);
        realmRepresentation.setResetPasswordAllowed(true);

        realmRepresentation.setBruteForceProtected(true);

        client.realms().create(realmRepresentation);

        // TODO: generate a new realm key-pair

        //
        // Clients
        //

        ClientRepresentation clientRepresentation = null;
        String clientName = null;

        LOG.info("Adding EDS-UI client...");

        clientName = "eds-ui";
        clientRepresentation = getClientRepresentation(clientName);
        client.realm(newRealm).clients().create(clientRepresentation);

        String edsUIClientJson = client.realm(newRealm).clients().get(clientName).getInstallationProvider("keycloak-oidc-keycloak-json");
        LOG.info(edsUIClientJson);

        setCassandraConfig("c865eb28-58dd-4ec4-9ca9-fda1273566bf", "KeycloakConfig", edsUIClientJson);

        LOG.info("Adding EDS SFTP Reader client...");

        clientName = "eds-sftpreader";
        clientRepresentation = getClientRepresentation(clientName);

        client.realm(newRealm).clients().create(clientRepresentation);

        String edsSFTPReader = client.realm(newRealm).clients().get(clientName).getInstallationProvider("keycloak-oidc-keycloak-json");
        LOG.info(edsSFTPReader);

        setCassandraConfig("f48c8bf3-a03d-411e-baf1-8712569ed67f", "KeycloakConfig-SFTPReader", edsSFTPReader);

        //
        // Roles
        //

        LOG.info("Adding roles...");

        client.realm(newRealm).roles().create(new RoleRepresentation("eds_user", "EDS User with basic access", false));
        client.realm(newRealm).roles().create(new RoleRepresentation("eds_admin", "EDS Admin User", false));
        client.realm(newRealm).roles().create(new RoleRepresentation("eds_superuser", "EDS Super User", false));
        client.realm(newRealm).roles().create(new RoleRepresentation("eds_messaging_get", "EDS Messaging API GET", false));
        client.realm(newRealm).roles().create(new RoleRepresentation("eds_messaging_post", "EDS Messaging API POST", false));
        client.realm(newRealm).roles().create(new RoleRepresentation("eds_user_professional", "EDS Professional User", false));
        client.realm(newRealm).roles().create(new RoleRepresentation("eds_service", "EDS machine service user", false));

        //
        // Add users
        //

        LOG.info("Adding application users...");
        addAdminUser(client.realm("master"), newRealm, "eds-ui", "bd285adbc36842d7a27088e93c36c13e29ed69fa63a6", "EDS", "UI", "edsui@endeavourhealth.com", Lists.newArrayList("view-users", "manage-users", "view-events"));

        LOG.info("Adding users...");

        addUser(client.realm(newRealm), "superuser", "Test1234", "Super", "User", "superuser@example.com", Lists.newArrayList("eds_superuser"));
        addUser(client.realm(newRealm), "admin", "Test1234", "Admin", "User", "admin@example.com",Lists.newArrayList("eds_admin"));
        addUser(client.realm(newRealm), "basicuser", "Test1234", "Basic", "User", "basicuser@example.com",Lists.newArrayList("eds_user"));
        addUser(client.realm(newRealm), "messaging", "Test1234", "Messaging", "User", "messaging@example.com",Lists.newArrayList("eds_messaging_get", "eds_messaging_post"));
        addUser(client.realm(newRealm), "professional", "Test1234", "Professional", "User", "professional@example.com",Lists.newArrayList("eds_user_professional"));

        addUser(client.realm(newRealm), "sftpuser", "sftppassword", "SFTP", "User", "sftpuser@example.com",Lists.newArrayList("eds_service", "eds_messaging_post"));

        cassandraClient.close(); // TODO: handle graceful close on exceptions
    }

    private ClientRepresentation getClientRepresentation(String clientName) {
        ClientRepresentation clientRepresentation;
        clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(clientName);
        clientRepresentation.setName(clientName);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setRedirectUris(Lists.newArrayList("*"));
        clientRepresentation.setWebOrigins(Lists.newArrayList("*"));
        return clientRepresentation;
    }

    private void setCassandraConfig(String key, String name, String value) {

        cassandraClient.getSession().execute("INSERT INTO configuration.configuration_resource\n" +
                "(configuration_id, configuration_name, configuration_data)\n" +
                "VALUES(?, ?, ?);\n", UUID.fromString(key), name, value);

    }

    private void addUser(RealmResource realm, String username, String password, String firstName, String lastName, String email, List<String> roles) {
        UserRepresentation user = new UserRepresentation();

        user.setEnabled(true);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRequiredActions(new ArrayList<>());

        user.setEmailVerified(true);

        // create basic user
        realm.users().create(user);

        // set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        String id = realm.users().search("", "", "", email, 0, 10).get(0).getId();

        realm.users().get(id).resetPassword(credential);

        // add roles
        realm.users().get(id).roles().realmLevel().add(getRoles(realm, roles));

        LOG.info(toJson(user));
    }

    private void addAdminUser(RealmResource realm, String targetRealm, String username, String password, String firstName, String lastName, String email, List<String> roles) {
        UserRepresentation user = new UserRepresentation();

        user.setEnabled(true);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRequiredActions(new ArrayList<>());

        user.setEmailVerified(true);

        // create basic user
        realm.users().create(user);

        // set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        String id = realm.users().search("", "", "", email, 0, 10).get(0).getId();

        realm.users().get(id).resetPassword(credential);

        // add roles
        String clientId = realm.clients().findByClientId(targetRealm+"-realm").get(0).getId();
        realm.users().get(id).roles().clientLevel(clientId).add(getClientRoles(clientId, realm, roles));
    }

    private String toJson(Object value) {
        try {
            return JsonSerialization.writeValueAsString(value);
        } catch (IOException e) {

        }
        return null;
    }

    private List<RoleRepresentation> getClientRoles(String clientId, RealmResource realm, List<String> roles) {
        final List<RoleRepresentation> r = realm .clients().get(clientId).roles().list();
        return Lists.transform(roles,
                s -> Iterables.find(r,
                        (Predicate<RoleRepresentation>) t -> { return t.getName().equalsIgnoreCase(s); }, null));
    }

    private List<RoleRepresentation> getRoles(RealmResource realm, List<String> roles) {
        final List<RoleRepresentation> r = realm.roles().list();
        return Lists.transform(roles,
                s -> Iterables.find(r,
                        (Predicate<RoleRepresentation>) t -> { return t.getName().equalsIgnoreCase(s); }, null));
    }
}
