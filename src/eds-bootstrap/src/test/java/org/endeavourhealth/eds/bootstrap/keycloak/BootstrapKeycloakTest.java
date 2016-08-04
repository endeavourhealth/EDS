package org.endeavourhealth.eds.bootstrap.keycloak;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

public class BootstrapKeycloakTest {
    @org.junit.Test
    @org.junit.Ignore
    public void keycloakClientAuthRefreshAndAccountInfo() throws Exception {
        KeycloakClient.init("https://keycloak.eds.c.healthforge.io/auth/", "sftptest", "sftpuser", "sftppassword","eds-sftp");

        System.out.println("Token:");
        System.out.println();
        AccessTokenResponse token = KeycloakClient.instance().getToken();
        Assert.assertNotNull(token);
        System.out.println(token.getToken());
        System.out.println();

        System.out.println("Token (after refresh):");
        System.out.println();
        AccessTokenResponse newToken = KeycloakClient.instance().refreshToken();
        Assert.assertNotNull(newToken);
        System.out.println(newToken.getToken());
        System.out.println();

        Assert.assertNotEquals(token.getToken(), newToken.getToken());

        System.out.println("User account details:");
        System.out.println();
        UserRepresentation account = KeycloakClient.instance().getUserAccount();
        System.out.println(JsonSerialization.writeValueAsPrettyString(account));
        Assert.assertTrue(StringUtils.isNoneEmpty(account.getId()));
    }
}