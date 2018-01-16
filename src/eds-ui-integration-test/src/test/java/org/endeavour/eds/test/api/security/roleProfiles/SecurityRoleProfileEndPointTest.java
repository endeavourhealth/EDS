package org.endeavour.eds.test.api.security.roleProfiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.endeavour.eds.test.AuthHelper;
import org.endeavour.eds.test.JsonHelper;
import org.endeavour.eds.test.api.security.orgRoles.JsonOrgRole;
import org.endeavourhealth.common.security.OrgRoles;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.test.HttpAction;
import org.ocpsoft.rewrite.test.RewriteTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.endeavour.eds.test.api.security.orgRoles.SecurityOrgRoleEndPointTest.deleteGroup;

@RunWith(Arquillian.class)
public class SecurityRoleProfileEndPointTest extends RewriteTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRoleProfileEndPointTest.class);

/*    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(ZipImporter.class, "ROOT.war")
                .importFrom(new File("/Users/mark/Data/endeavourhealth/src-clean/EDS/src/eds-ui/target/eds-ui-1.0-SNAPSHOT.war"))
                .as(WebArchive.class)
                .addAsManifestResource("arquillian.xml");
    }
*/



    @Test
    @RunAsClient
    public void testListAvailableRoles() throws Exception {
        AuthHelper.auth();

        HttpClient client = HttpClientBuilder.create().build();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/roleProfiles/availableRoles", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        LOG.trace(response);
        Assert.assertEquals(200, httpGet.getStatusCode());
    }

    @Test
    @RunAsClient
    public void testList() throws Exception {
        AuthHelper.auth();

        HttpClient client = HttpClientBuilder.create().build();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/roleProfiles", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        LOG.trace(response);

        Assert.assertEquals(200, httpGet.getStatusCode());
    }

    public static JsonRoleProfile getRole(RewriteTestBase base, HttpClient client, String roleName) throws Exception {

        HttpAction<HttpGet> httpGet = base.get(client, "/api/security/roleProfiles", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpGet.getStatusCode());

        List<JsonRoleProfile> roleProfiles = JsonHelper.toObject(response, new TypeReference<List<JsonRoleProfile>>() {});
        for(JsonRoleProfile roleProfile : roleProfiles) {
            if(roleProfile.getName().equalsIgnoreCase(roleName)) {
                return roleProfile;
            }
        }
        return null;
    }

    public static JsonRole getAvailableRole(RewriteTestBase base, HttpClient client, String roleName) throws Exception {

        HttpAction<HttpGet> httpGet = base.get(client, "/api/security/roleProfiles/availableRoles", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpGet.getStatusCode());

        List<JsonRole> roles = JsonHelper.toObject(response, new TypeReference<List<JsonRole>>() {});
        for(JsonRole role : roles) {
            if(role.getName().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return null;
    }

    @Test
    @RunAsClient
    public void testCreateAndNestingAndDelete() throws Exception {
        AuthHelper.auth();

        HttpClient client = HttpClientBuilder.create().build();

        String rnd = UUID.randomUUID().toString().substring(0, 6);
        String roleId = getAvailableRole(this, client, "eds_admin").getRoleProfileId().toString();

        // create composite roles
        JsonRoleProfile roleProfile = new JsonRoleProfile(null, "integration-test-" + rnd + ".1.1", "integration-test-" + rnd + ".1.1", null);
        JsonRoleProfile roleProfileOut = postRoleProfile(client, roleProfile);

        JsonRoleProfile roleProfile1 = new JsonRoleProfile(null, "integration-test-" + rnd + ".1", "integration-test-" + rnd + ".1", Lists.newArrayList(roleProfileOut.getRoleProfileId().toString()));
        JsonRoleProfile roleProfileOut1 = postRoleProfile(client, roleProfile1);

        JsonRoleProfile roleProfile2 = new JsonRoleProfile(null, "integration-test-" + rnd, "integration-test-" + rnd, Lists.newArrayList(roleProfileOut1.getRoleProfileId().toString()));
        JsonRoleProfile roleProfileOut2 = postRoleProfile(client, roleProfile2);

        Assert.assertEquals("eds_role_profile_integration_test_" + rnd + "_1_1", roleProfileOut.getName());

        // add to role group
        String orgId = OrgRoles.ROOT_ORGANISATION_ID;
        JsonOrgRole orgRoleGroup = new JsonOrgRole(null, "org-role-integration-test-" + rnd, "org-role-integration-test-" + rnd, UUID.fromString(orgId), Lists.newArrayList(roleProfileOut2.getRoleProfileId().toString()));
        HttpAction<HttpPost> httpPost = post(client, "/api/security/orgRoleGroups/" + orgId, JsonHelper.toJson(orgRoleGroup), KeycloakClient.instance().getAuthorizationHeader());
        String response = httpPost.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpPost.getStatusCode());
        JsonOrgRole orgRoleGroupOut = JsonHelper.toObject(response, new TypeReference<JsonOrgRole>() {});

        // check effective roles
        List<JsonRole> roles = getEffectiveRoles(client, orgId, orgRoleGroupOut);
        List<JsonRole> roles2 = getEffectiveRoles(client, orgId.replace("0", "1"), orgRoleGroupOut);

        // update
        orgRoleGroup = new JsonOrgRole(orgRoleGroupOut.getOrgRoleId(), "org-role-integration-test-" + rnd, "org-role-integration-test-" + rnd, UUID.fromString(orgId), Lists.newArrayList(roleProfileOut.getRoleProfileId().toString()));
        HttpAction<HttpPut> httpPut = put(client, "/api/security/orgRoleGroups/" + orgId + "/" + orgRoleGroupOut.getOrgRoleId().toString(), JsonHelper.toJson(orgRoleGroup), KeycloakClient.instance().getAuthorizationHeader());
        response = httpPut.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpPut.getStatusCode());

        // check effective roles
        List<JsonRole> roles3 = getEffectiveRoles(client, orgId, orgRoleGroupOut);

        deleteRoleProfile(client, roleProfileOut);
        deleteRoleProfile(client, roleProfileOut1);
        deleteRoleProfile(client, roleProfileOut2);

        deleteGroup(this, client, orgId.toString(), orgRoleGroupOut.getOrgRoleId().toString());

        LOG.info("Effective roles [1]: {}", JsonHelper.toJson(roles.stream().map(r -> r.getName()).collect(Collectors.toList())));
        LOG.info("Effective roles [2]: {}", JsonHelper.toJson(roles2.stream().map(r -> r.getName()).collect(Collectors.toList())));
        LOG.info("Effective roles [3]: {}", JsonHelper.toJson(roles3.stream().map(r -> r.getName()).collect(Collectors.toList())));

        Assert.assertTrue(hasRole(roles, roleProfileOut.getName()));
        Assert.assertTrue(hasRole(roles, roleProfileOut1.getName()));
        Assert.assertTrue(hasRole(roles, roleProfileOut2.getName()));

        Assert.assertFalse(hasRole(roles2, roleProfileOut.getName()));
        Assert.assertFalse(hasRole(roles2, roleProfileOut1.getName()));
        Assert.assertFalse(hasRole(roles2, roleProfileOut2.getName()));

        Assert.assertTrue(hasRole(roles3, roleProfileOut.getName()));
        Assert.assertFalse(hasRole(roles3, roleProfileOut1.getName()));
        Assert.assertFalse(hasRole(roles3, roleProfileOut2.getName()));
    }

    private List<JsonRole> getEffectiveRoles(HttpClient client, String orgId, JsonOrgRole orgRoleGroupOut) throws Exception {
        String response;HttpAction<HttpGet> httpGet = get(client, "/api/security/orgRoleGroups/" + orgId + "/" + orgRoleGroupOut.getOrgRoleId() + "/effective", KeycloakClient.instance().getAuthorizationHeader());
        response = httpGet.getResponseContent();
        if(httpGet.getStatusCode() != 200) {
            return new ArrayList<>();
        }
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        return JsonHelper.toObject(response, new TypeReference<List<JsonRole>>() {});
    }

    private boolean hasRole(List<JsonRole> roles, String name) {
        return roles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(name));
    }

    private void deleteRoleProfile(HttpClient client, JsonRoleProfile roleProfile) throws Exception {
        HttpAction<HttpDelete> httpDelete = delete(client, "/api/security/roleProfiles/" + roleProfile.getName(), KeycloakClient.instance().getAuthorizationHeader());
        EntityUtils.consumeQuietly(httpDelete.getResponse().getEntity());
        Assert.assertEquals(200, httpDelete.getStatusCode());
    }

    private JsonRoleProfile postRoleProfile(HttpClient client, JsonRoleProfile roleProfile) throws Exception {
        HttpAction<HttpPost> httpPost = post(client, "/api/security/roleProfiles", JsonHelper.toJson(roleProfile), KeycloakClient.instance().getAuthorizationHeader());
        String response = httpPost.getResponseContent();
        Assert.assertEquals(200, httpPost.getStatusCode());
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        LOG.info(response);
        return JsonHelper.toObject(response, new TypeReference<JsonRoleProfile>() {});
    }
}
