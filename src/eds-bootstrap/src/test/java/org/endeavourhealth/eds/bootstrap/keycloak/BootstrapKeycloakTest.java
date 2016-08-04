package org.endeavourhealth.eds.bootstrap.keycloak;

import org.junit.Assert;
import org.keycloak.representations.AccessTokenResponse;

import static org.junit.Assert.*;

public class BootstrapKeycloakTest {
    @org.junit.Test
    public void getToken() throws Exception {
        AccessTokenResponse token = BootstrapKeycloak.getToken();
        Assert.assertNotNull(token);
        System.out.println(token.getToken());
    }
}