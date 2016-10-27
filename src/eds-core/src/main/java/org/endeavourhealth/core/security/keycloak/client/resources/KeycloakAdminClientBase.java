package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.security.KeycloakConfigUtils;
import org.endeavourhealth.core.security.keycloak.client.KeycloakClient;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class KeycloakAdminClientBase {

    protected static final Logger LOG = LoggerFactory.getLogger(KeycloakAdminClientBase.class);

    protected KeycloakDeployment keycloakDeployment;
    protected String keycloakRealm;
    protected ObjectMapper objectMapper;

    protected boolean initKeycloakAdmin = false;

    protected void initKeycloakAdminClient() {

        // get the Endeavour realm name
        keycloakDeployment = KeycloakConfigUtils.getConfig(ConfigurationRepository.KEYCLOAK_CONFIG);
        keycloakRealm = keycloakDeployment.getRealm();

        // get config details for the realm admin client
        Map<String, String> envVars = System.getenv();

        String adminClientUsername = envVars.get("KEYCLOAK_PROXY_USER");
        String adminClientPassword = envVars.get("KEYCLOAK_PROXY_PASSWORD");

        // build the admin client
        KeycloakClient.init(keycloakDeployment.getAuthServerBaseUrl(),
                "master",
                adminClientUsername,
                adminClientPassword,
                "admin-cli");

        try {
            LOG.trace("Keycloak token = '{}'", KeycloakClient.instance().getToken().getToken());
        } catch (IOException e) {
            LOG.trace("Keycloak token = 'null'");
        }

        objectMapper = new ObjectMapper();

        initKeycloakAdmin = true;
    }

    protected void assertKeycloakAdminClientInitialised() {
        if(!initKeycloakAdmin) {
            initKeycloakAdminClient();
        }
    }


    //
    // Helper methods
    //

    protected HttpResponse doGet(CloseableHttpClient httpClient, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
        return httpClient.execute(httpGet);
    }

    protected HttpResponse doPost(CloseableHttpClient httpClient, String url, Object entity) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(KeycloakClient.instance().getAuthorizationHeader());
        String content = objectMapper.writeValueAsString(entity);
        HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
        httpPost.setEntity(httpEntity);
        return httpClient.execute(httpPost);
    }

    protected HttpResponse doPut(CloseableHttpClient httpClient, String url, Object entity) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader(KeycloakClient.instance().getAuthorizationHeader());
        String content = objectMapper.writeValueAsString(entity);
        HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
        httpPut.setEntity(httpEntity);
        return httpClient.execute(httpPut);
    }

    protected HttpResponse doDelete(CloseableHttpClient httpClient, String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.addHeader(KeycloakClient.instance().getAuthorizationHeader());
        return httpClient.execute(httpDelete);
    }

    protected HttpResponse doDelete(CloseableHttpClient httpClient, String url, Object entity) throws IOException {
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
        httpDelete.addHeader(KeycloakClient.instance().getAuthorizationHeader());
        String content = objectMapper.writeValueAsString(entity);
        HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
        httpDelete.setEntity(httpEntity);
        return httpClient.execute(httpDelete);
    }

    protected <T> T toEntity(HttpResponse httpResponse, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(httpResponse.getEntity().getContent(), typeReference);
    }

    protected String getIdFromLocation(HttpResponse httpResponse) {
        Header locationHeader = httpResponse.getFirstHeader(HttpHeaders.LOCATION);
        if(locationHeader == null || StringUtils.isBlank(locationHeader.getValue())) {
            return null;
        }

        return locationHeader.getValue().substring(locationHeader.getValue().lastIndexOf("/") + 1);
    }

    protected boolean isHttpOkStatus(HttpResponse response) {
        return (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300);
    }

}
