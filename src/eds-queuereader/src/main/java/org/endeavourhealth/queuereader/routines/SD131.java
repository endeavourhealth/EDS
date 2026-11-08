package org.endeavourhealth.queuereader.routines;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SD131 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD131.class);

    public static void testDdsuiBackendCall(String configName) {

        testDdsuiBackendCall(configName, "api/exchangeAudit/postToExchange");
        LOG.error("");
        testDdsuiBackendCall(configName, "api/exchangeAudit/postToExchangeApi");
        LOG.error("");
        testDdsuiBackendCall(configName, "api/exchangeAudit/testPostToExchange");
        LOG.error("");
        testDdsuiBackendCall(configName, "api/exchangeAudit/testPostToExchangeApi");
        LOG.error("");
        testDdsuiBackendCall(configName, "api/exchangeAudit/testPostToExchangeAll");
        LOG.error("");

        testDdsuiBackendCall(configName, "machine-api/exchangeAudit/postToExchange");
        LOG.error("");
        testDdsuiBackendCall(configName, "machine-api/exchangeAudit/postToExchangeApi");
        LOG.error("");
        testDdsuiBackendCall(configName, "machine-api/exchangeAudit/testPostToExchange");
        LOG.error("");
        testDdsuiBackendCall(configName, "machine-api/exchangeAudit/testPostToExchangeApi");
        LOG.error("");
        testDdsuiBackendCall(configName, "machine-api/exchangeAudit/testPostToExchangeAll");
        LOG.error("");



    }

    private static void testDdsuiBackendCall(String configName, String path) {
        LOG.debug("Making call using " + configName + " to " + path);

        try {

            //get the Keycloak details from the EMIS config
            JsonNode json = ConfigManager.getConfigurationAsJson(configName, "queuereader");
            json = json.get("missing_code_fix");

            String ddsUrl = json.get("dds-ui-url").asText();
            String keyCloakUrl = json.get("keycloak-url").asText();
            String keyCloakRealm = json.get("keycloak-realm").asText();
            String keyCloakUser = json.get("keycloak-username").asText();
            String keyCloakPass = json.get("keycloak-password").asText();
            String keyCloakClientId = json.get("keycloak-client-id").asText();

            KeycloakClient kcClient = new KeycloakClient(keyCloakUrl, keyCloakRealm, keyCloakUser, keyCloakPass, keyCloakClientId);


            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonRequestRoot = new ObjectNode(mapper.getNodeFactory());

            //post a non-queueable exchange, so this should be safe to keep trying with
            jsonRequestRoot.put("exchangeId", "8135829d-1322-445a-8932-226aad05d896");
            jsonRequestRoot.put("serviceId", "7275d4c8-05eb-47a9-8c6b-0c29f47e57d8");
            jsonRequestRoot.put("systemId", "c9883c41-ca29-4ad6-bcb4-eb05b0782c81");
            jsonRequestRoot.put("exchangeName", "EdsInbound"); //want to target the inbound queue
            jsonRequestRoot.put("postMode", "This"); //want to queue from our first exchange ID and onward
            jsonRequestRoot.put("reason", "Testing for SD-131"); //free-text reason for re-queuing

            String parameters = mapper.writeValueAsString(jsonRequestRoot);


            //use a slightly different URL now so that we can use a user from the machine realm in Keycloak
            //TODO - finish tesitng this and get it using the new Keycloak user and different API
            WebTarget target = ClientBuilder.newClient().target(ddsUrl).path(path);


            Response response = target
                    .request()
                    .header("Authorization", "Bearer " + kcClient.getToken().getToken())
                    .post(Entity.entity(parameters, MediaType.APPLICATION_JSON));

            if (response.getStatus() != HttpStatus.SC_OK) {
                LOG.error("Got response " + response.getStatus());

                String ret = response.readEntity(String.class);
                LOG.error("" + ret);

            } else {
                LOG.info("Got response " + response.getStatus());
            }



        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}
