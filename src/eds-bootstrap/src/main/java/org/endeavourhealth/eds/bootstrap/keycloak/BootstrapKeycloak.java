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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BootstrapKeycloak implements Bootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapKeycloak.class);

    private CassandraClient cassandraClient;

    @Override
    public void bootstrap(Config config) {

        LOG.info("Bootstrapping...");
        Keycloak client = initClient(config);


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
            LOG.warn("Realm does not exist or failed to delete...", e);
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
        clientRepresentation.setBaseUrl(config.getKeycloak().getServer().getServerUrl());
        client.realm(newRealm).clients().create(clientRepresentation);
        createProtocolMapper(client, newRealm, clientName);

        updateCassandraKeycloakClientConfig(client, newRealm, clientName, "c865eb28-58dd-4ec4-9ca9-fda1273566bf", "KeycloakConfig");

        LOG.info("Adding EDS SFTP Reader client...");

        clientName = "eds-sftpreader";
        clientRepresentation = getClientRepresentation(clientName);

        client.realm(newRealm).clients().create(clientRepresentation);
        createProtocolMapper(client, newRealm, clientName);

        updateCassandraKeycloakClientConfig(client, newRealm, clientName, "f48c8bf3-a03d-411e-baf1-8712569ed67f", "KeycloakConfig-SFTPReader");
        createRealmUsersGroupsRoles(client, newRealm);


        cassandraClient.close(); // TODO: handle graceful close on exceptions
    }

    private void updateCassandraKeycloakClientConfig(Keycloak client, String newRealm, String clientName, String key, String keycloakConfig) {
        String edsUIClientJson = client.realm(newRealm).clients().get(clientName).getInstallationProvider("keycloak-oidc-keycloak-json");
        //edsUIClientJson = edsUIClientJson.replace("\"ssl-required\" : \"external\"", "\"ssl-required\" : \"all\"");
        LOG.info(edsUIClientJson);

        setCassandraConfig(key, keycloakConfig, edsUIClientJson);
    }

    public Keycloak initClient(Config config) {
        // TODO: remove this when RestEasy uses the new HttpClient that doesn't have SNI problems
        try {
            KeycloakClient.trustAllCertificates();
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyManagementException e) {
        }

        // TODO: remove this when RestEasy uses a newer version of HttpClient
        ResteasyClient resteasyClient = (new ResteasyClientBuilder()).disableTrustManager().connectionPoolSize(50).connectionTTL(1000, TimeUnit.MILLISECONDS).build();
        return new CustomKeycloak(
                config.getKeycloak().getServer().getServerUrl(),
                config.getKeycloak().getServer().getRealm(),
                config.getKeycloak().getServer().getUsername(),
                config.getKeycloak().getServer().getPassword(),
                config.getKeycloak().getServer().getClientId(),
                null, "password", resteasyClient);
    }

    public void users(Config config) {
        createRealmUsersGroupsRoles(initClient(config), config.getKeycloak().getRealm());
    }

    public void createRealmUsersGroupsRoles(Keycloak client, String newRealm) {
        //
        // Roles
        //

        LOG.info("Adding roles...");

        addRole(client, newRealm, "eds_user", "EDS User with basic access");
        addRole(client, newRealm, "eds_admin", "EDS Admin User");
        addRole(client, newRealm, "eds_superuser", "EDS Super User");
        addRole(client, newRealm, "eds_messaging_get", "EDS Messaging API GET");
        addRole(client, newRealm, "eds_messaging_post", "EDS Messaging API POST");
        addRole(client, newRealm, "eds_user_professional", "EDS Professional User");
        addRole(client, newRealm, "eds_service", "EDS machine service user");

        addRole(client, newRealm, "eds_read_only", "EDS read-only access");
        addRole(client, newRealm, "eds_read_write", "EDS read-write access");

        List<RoleRepresentation> realmRoles = client.realm(newRealm).roles().list();

        //
        // Groups
        //
        String organisationId = "00000000-0000-0000-0000-000000000000";     // root organisation id
        LOG.info("Adding groups...");
        addGroup(client, newRealm, realmRoles, "eds_admins", "EDS admins", organisationId, Lists.newArrayList("eds_user", "eds_admin"));
        addGroup(client, newRealm, realmRoles, "eds_user_professionals", "EDS professionals", organisationId, Lists.newArrayList("eds_user", "eds_user_professional"));
        addGroup(client, newRealm, realmRoles, "eds_sftpusers", "EDS sftpusers", organisationId, Lists.newArrayList("eds_user", "eds_service", "eds_messaging_post"));

        organisationId = "11111111-1111-1111-1111-111111111111";
        addGroup(client, newRealm, realmRoles, "test_user_read_only", "Test group read-only users", organisationId, Lists.newArrayList("eds_read_only"));

        organisationId = "22222222-2222-2222-2222-222222222222";
        addGroup(client, newRealm, realmRoles, "test_group_user_read_write", "Test group for read-write users", organisationId, Lists.newArrayList("eds_read_write"));

        organisationId = "33333333-3333-3333-3333-333333333333";
        addGroup(client, newRealm, realmRoles, "test_group_user_gp", "Test group for GP users", organisationId, Lists.newArrayList("eds_read_only", "eds_user", "eds_user_professional"));

        //
        // Add users
        //

        LOG.info("Adding application users...");
        addAdminUser(client.realm("master"), newRealm, "eds-ui", "bd285adbc36842d7a27088e93c36c13e29ed69fa63a6", "EDS", "UI", "edsui@endeavourhealth.com", Lists.newArrayList("view-users", "manage-users", "view-events"));

        LOG.info("Adding users...");

        addUser(client.realm(newRealm), realmRoles, "admin", "Test1234", "Admin", "User", "admin@example.com", Lists.newArrayList("eds_admins"));
        addUser(client.realm(newRealm), realmRoles, "professional", "Test1234", "Professional", "User", "professional@example.com",Lists.newArrayList("eds_user_professionals", "test_user_read_only", "test_group_user_read_write", "test_group_user_gp"));
        addUser(client.realm(newRealm), realmRoles, "sftpuser", "sftppassword", "SFTP", "User", "sftpuser@example.com",Lists.newArrayList("eds_sftpusers"));
    }

    private void addRole(Keycloak client, String realm, String roleName, String roleDescription) {
        try {
            client.realm(realm).roles().create(new RoleRepresentation(roleName, roleDescription, false));
        } catch(Exception e) {
            LOG.error("FAILED to add role '{}': '{}'", roleName, e.getMessage());
        }
    }

    private void addGroup(Keycloak client, String newRealm, List<RoleRepresentation> currentRealmRoles, String groupName, String groupDescription, String organisationId, List<String> realmRoles) {
        LOG.info("Adding group {}/{} for org {}...", newRealm, groupName, organisationId);

        try {
            GroupRepresentation group = new GroupRepresentation();

            group.setName(groupName);
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("description", Lists.newArrayList(groupDescription));
            attributes.put("organisation-id", Lists.newArrayList(organisationId));
            group.setAttributes(attributes);
            client.realm(newRealm).groups().add(group);

            LOG.info(" - group added");

            LOG.info(" - getting roles...");
            List<RoleRepresentation> roles = toRoles(currentRealmRoles, realmRoles);

            LOG.info(" - getting group...");
            GroupRepresentation groupResource = getGroup(client.realm(newRealm), groupName);

            LOG.info(" - adding roles...");
            client.realm(newRealm).groups().group(groupResource.getId()).roles().realmLevel().add(roles);

            LOG.info(" - group complete");

            LOG.info(toJson(group));
        } catch(Exception e) {
            LOG.error("FAILED: '{}'", e.getMessage());
        }
    }

    private List<RoleRepresentation> toRoles(List<RoleRepresentation> currentRealmRoles, List<String> realmRoles) {
        return Lists.transform(realmRoles,
                s -> Iterables.find(currentRealmRoles,
                        (Predicate<RoleRepresentation>) t -> { return t.getName().equalsIgnoreCase(s); }, null) );
    }

    private List<String> toRoleIds(RealmResource realm, List<String> realmRoles) {
        final List<RoleRepresentation> r = realm.roles().list();
        return Lists.transform(realmRoles,
                s -> Iterables.find(r,
                        (Predicate<RoleRepresentation>) t -> { return t.getName().equalsIgnoreCase(s); }, null).getId() );
    }

    private void createProtocolMapper(Keycloak client, String newRealm, String clientName) {
        LOG.info("Adding protocol mapper to {}/{}...", newRealm, clientName);

        ProtocolMapperRepresentation protocolMapper = new ProtocolMapperRepresentation();
        protocolMapper.setName("endeavour-organisation-roles-mapper");
        protocolMapper.setProtocol("openid-connect");
        protocolMapper.setProtocolMapper("endeavourhealth-org-group-membership-mapper");
        Map<String,String> protocolMapperConfig = new HashMap<>();
        protocolMapperConfig.put("claim.name", "orgGroups");
        protocolMapperConfig.put("access.token.claim", "true");
        protocolMapperConfig.put("id.token.claim", "true");
        protocolMapperConfig.put("full.path", "true");
        protocolMapper.setConfig(protocolMapperConfig);
        client.realm(newRealm).clients().get(clientName).getProtocolMappers().createMapper(protocolMapper);
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

    private void addUser(RealmResource realm, List<RoleRepresentation> currentRealmRoles, String username, String password, String firstName, String lastName, String email, List<String> groups) {
        try {
            UserRepresentation user = new UserRepresentation();

            LOG.info("  user: {}", username);

            user.setEnabled(true);
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setRequiredActions(new ArrayList<>());

            user.setEmailVerified(true);

            // create basic user
            realm.users().create(user);

            LOG.info("  - user created");


            LOG.info("  - getting user...");
            String id = realm.users().search("", "", "", email, 0, 10).get(0).getId();

            // set password
            LOG.info("  - setting password...");
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            realm.users().get(id).resetPassword(credential);

            // add roles
            LOG.info("  - adding roles...");
            realm.users().get(id).roles().realmLevel().add(toRoles(currentRealmRoles, Lists.newArrayList("eds_user_professional")));

            // add groups
            LOG.info("  - adding groups...");
            for(GroupRepresentation group : getGroups(realm, groups)) {
                realm.users().get(id).joinGroup(group.getId());
            }

            LOG.info(toJson(getGroups(realm, groups)));
            LOG.info(toJson(user));
        } catch(Exception e) {
            LOG.error("FAILED to add user '{}': '{}' on {}", username, e.getMessage(), e.getStackTrace()[0]);
            LOG.error("Exception:", e);
        }
    }

    private GroupRepresentation getGroup(RealmResource realm, String groupName) {
        List<GroupRepresentation> groups = realm.groups().groups();
        for(GroupRepresentation g : groups) {
            if(g.getName().equalsIgnoreCase(groupName)) {
                return g;
            }
        }
        return null;
    }


    private Collection<? extends GroupRepresentation> getGroups(RealmResource realm, List<String> groups) {
        final List<GroupRepresentation> r = realm.groups().groups();
        return Lists.transform(groups,
                s -> Iterables.find(r,
                        (Predicate<GroupRepresentation>) t -> { return t.getName().equalsIgnoreCase(s); }, null));
    }

    private void addAdminUser(RealmResource realm, String targetRealm, String username, String password, String firstName, String lastName, String email, List<String> roles) {
        try {
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
        } catch(Exception e) {
            LOG.error("FAILED to add admin user '{}': '{}' on {}", username, e.getMessage(), e.getStackTrace()[0]);
            LOG.error("Exception:", e);
        }
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

    public void edsUIClient(Config config) {

        cassandraClient = new CassandraClient();
        cassandraClient.connect(config);

        Keycloak client = initClient(config);
        String newRealm = config.getKeycloak().getRealm();

        LOG.info("Adding EDS-UI client config to cassandra...");

        String clientName = "eds-ui";
        updateCassandraKeycloakClientConfig(client, newRealm, clientName, "c865eb28-58dd-4ec4-9ca9-fda1273566bf", "KeycloakConfig");

        cassandraClient.close(); // TODO: handle graceful close on exceptions
    }
}
