package org.endeavour.eds.test.api.security.users;

import com.fasterxml.jackson.core.type.TypeReference;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.endeavour.eds.test.AuthHelper;
import org.endeavour.eds.test.JsonHelper;
import org.endeavour.eds.test.api.security.orgRoles.JsonOrgRole;
import org.endeavour.eds.test.api.security.orgRoles.SecurityOrgRoleEndPointTest;
import org.endeavourhealth.core.security.OrgRoles;
import org.endeavourhealth.core.security.keycloak.client.KeycloakClient;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.ocpsoft.rewrite.test.HttpAction;
import org.ocpsoft.rewrite.test.RewriteTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(Arquillian.class)
public class SecurityUsersEndPointTest extends RewriteTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUsersEndPointTest.class);

    @Test
    @RunAsClient
    public void testAuthzDenied() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/users", KeycloakClient.instance().getAuthorizationHeader(), AuthHelper.getUnauthorisedOrgHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        LOG.trace(response);
        Assert.assertEquals(403, httpGet.getStatusCode());
    }

    @Test
    @RunAsClient
    public void testList() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/users?offset=0&limit=1000", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        Assert.assertEquals(200, httpGet.getStatusCode());

        String currentUserId = KeycloakClient.instance().getUserAccount().getId();

        boolean found = false;
        List<UserRepresentation> users = JsonHelper.toObject(response, new TypeReference<List<UserRepresentation>>() {});
        for(UserRepresentation user : users) {
            if(user.getId().equalsIgnoreCase(currentUserId)) {
                found = true;
                break;
            }
        }

        Assert.assertTrue("Auth user not found in user list", found);
    }

    @Test
    @RunAsClient
    public void testAccessDenied() throws Exception {
        AuthHelper.auth(true, "professional", "Test1234");

        try {
            HttpClient client = new DefaultHttpClient();

            HttpAction<HttpGet> httpGet = get(client, "/api/security/users", KeycloakClient.instance().getAuthorizationHeader());

            String response = httpGet.getResponseContent();
            Assert.assertTrue(StringUtils.isNotEmpty(response));

            LOG.trace(response);
            Assert.assertEquals(403, httpGet.getStatusCode());
        } finally {
            AuthHelper.logout();
        }
    }

    @Test
    @RunAsClient
    public void testGet() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        String userId = KeycloakClient.instance().getUserAccount().getId();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/users/" + userId, KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpGet.getStatusCode());
        LOG.trace(response);

        UserRepresentation user = JsonHelper.toObject(response, new TypeReference<UserRepresentation>() {});

    }

    @Test
    @RunAsClient
    public void testCreateAndDelete() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        UserRepresentation user = createUser(client);
        String response;

        HttpAction<HttpDelete> httpDelete = delete(client, "/api/security/users/" + user.getId(), KeycloakClient.instance().getAuthorizationHeader());
        EntityUtils.consumeQuietly(httpDelete.getResponse().getEntity());
        Assert.assertEquals(200, httpDelete.getStatusCode());

        HttpAction<HttpGet> httpGet = get(client, "/api/security/users/" + user.getId(), KeycloakClient.instance().getAuthorizationHeader());
        response = httpGet.getResponseContent();
        LOG.info(response);
        Assert.assertEquals(500, httpGet.getStatusCode());
    }

    private UserRepresentation createUser(HttpClient client) throws Exception {
        String rnd = UUID.randomUUID().toString();
        JsonUser jsonUser = new JsonUser(null, "integration-test-" + rnd, rnd + "@example.com", "first-" + rnd, "last-" + rnd);
        HttpAction<HttpPost> httpPost = post(client, "/api/security/users", JsonHelper.toJson(jsonUser), KeycloakClient.instance().getAuthorizationHeader());

        String response = httpPost.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        LOG.trace(response);
        Assert.assertEquals(200, httpPost.getStatusCode());
        return JsonHelper.toObject(response, new TypeReference<UserRepresentation>() {});
    }


    @Test
    @RunAsClient
    public void testJoinAndLeaveGroup() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        // assign admin user to role
        String userId = KeycloakClient.instance().getUserAccount().getId();
        String groupName = "eds_sftpusers";
        String roleName = "eds_messaging_post";

        // fetch role id
        JsonOrgRole group = SecurityOrgRoleEndPointTest.getGroup(this, client, groupName);
        LOG.info("Group '{}' = id '{}'", roleName, group.getOrgRoleId());

        // assign to role
        HttpAction<HttpPost> httpPost = post(client, "/api/security/users/" + userId + "/groups/" + group.getOrgRoleId().toString(), "", KeycloakClient.instance().getAuthorizationHeader());
        EntityUtils.consumeQuietly(httpPost.getResponse().getEntity());
        Assert.assertEquals(200, httpPost.getStatusCode());

        AuthHelper.logout();
        AuthHelper.auth();

        // get current users roles and compare
        List<String> roles = getRoles(client);
        LOG.info("Roles: {}", roles);
        Assert.assertTrue("Doesn't contain role after group assignment", roles.contains(roleName));

        // remove role and compare
        HttpAction<HttpDelete> httpDelete = delete(client, "/api/security/users/" + userId + "/groups/" + group.getOrgRoleId().toString(), KeycloakClient.instance().getAuthorizationHeader());
        EntityUtils.consumeQuietly(httpDelete.getResponse().getEntity());
        Assert.assertEquals(200, httpDelete.getStatusCode());

        AuthHelper.logout();
        AuthHelper.auth();

        roles = getRoles(client);
        LOG.info("Roles: {}", roles);
        Assert.assertFalse("Still contains role after group removal", roles.contains(roleName));
    }

    private List<String> getRoles(HttpClient client) throws Exception {
        HttpAction<HttpGet> httpGet = get(client, "/api/security/info/organisationRoles", KeycloakClient.instance().getAuthorizationHeader(), AuthHelper.getDefaultOrgHeader());
        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpGet.getStatusCode());
        Map<String, Object> map;
        List<String> roles;
        map = JsonHelper.toObject(response, new TypeReference<Map<String, Object>>() {});
        roles = (List<String>) map.get("orgRoles");
        return roles;
    }

}
