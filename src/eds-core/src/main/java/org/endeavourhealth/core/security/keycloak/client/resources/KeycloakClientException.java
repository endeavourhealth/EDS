package org.endeavourhealth.core.security.keycloak.client.resources;

public class KeycloakClientException extends Exception {

    public KeycloakClientException(String message, String httpClientMessage) {
        super(message + " - " + httpClientMessage);
    }
}
