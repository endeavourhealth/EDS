package org.endeavour.eds.test.api.security.orgRoles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
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
import org.endeavour.eds.test.api.security.roleProfiles.SecurityRoleProfileEndPointTest;
import org.endeavourhealth.core.security.OrgRoles;
import org.endeavourhealth.core.security.keycloak.client.KeycloakClient;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.test.HttpAction;
import org.ocpsoft.rewrite.test.RewriteTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RunWith(Arquillian.class)
public class SecurityOrgRoleEndPointTest extends RewriteTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityOrgRoleEndPointTest.class);

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
    public void testAccessDenied() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/orgRoleGroups/00000000-0000-0000-0000-000000000000", KeycloakClient.instance().getAuthorizationHeader(), AuthHelper.getUnauthorisedOrgHeader());

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

        HttpAction<HttpGet> httpGet = get(client, "/api/security/orgRoleGroups/00000000-0000-0000-0000-000000000000", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        LOG.trace(response);

        Assert.assertEquals(200, httpGet.getStatusCode());
    }

    public static JsonOrgRole getGroup(RewriteTestBase base, HttpClient client, String groupName) throws Exception {

        HttpAction<HttpGet> httpGet = base.get(client, "/api/security/orgRoleGroups/" + OrgRoles.ROOT_ORGANISATION_ID, KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        Assert.assertEquals(200, httpGet.getStatusCode());

        List<JsonOrgRole> groups = JsonHelper.toObject(response, new TypeReference<List<JsonOrgRole>>() {});
        for(JsonOrgRole g : groups) {
            if(g.getName().equalsIgnoreCase(groupName)) {
                return g;
            }
        }
        return null;
    }

    @Test
    @RunAsClient
    public void testCreateAndDelete() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        UUID orgId = UUID.fromString(OrgRoles.ROOT_ORGANISATION_ID);
        String rnd = UUID.randomUUID().toString();

        String roleId = SecurityRoleProfileEndPointTest.getAvailableRole(this, client, "eds_messaging_post").getRoleProfileId().toString();
        JsonOrgRole orgRoleGroup = new JsonOrgRole(null, "integration-test-" + rnd, "integration-test-" + rnd, orgId, Lists.newArrayList(roleId));
        HttpAction<HttpPost> httpPost = post(client, "/api/security/orgRoleGroups/" + orgId.toString(), JsonHelper.toJson(orgRoleGroup), KeycloakClient.instance().getAuthorizationHeader());
        Assert.assertEquals(200, httpPost.getStatusCode());
        String response = httpPost.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));
        LOG.info(response);
        JsonOrgRole orgRoleGroupOut = JsonHelper.toObject(response, new TypeReference<JsonOrgRole>() {});
        JsonOrgRole orgRoleGroupOut2 = getGroup(this, client, orgRoleGroup.getName());

        Assert.assertEquals(orgRoleGroupOut2.getOrgRoleId(), orgRoleGroupOut.getOrgRoleId());
        Assert.assertEquals(orgRoleGroupOut2.getName(), orgRoleGroupOut.getName());
        Assert.assertEquals(orgRoleGroupOut2.getDescription(), orgRoleGroupOut.getDescription());
        Assert.assertEquals(1, orgRoleGroupOut2.getRoles().size());
        Assert.assertEquals(1, orgRoleGroupOut.getRoles().size());

        deleteGroup(this, client, orgId.toString(), orgRoleGroupOut.getOrgRoleId().toString());
    }

    public static void deleteGroup(RewriteTestBase base, HttpClient client, String orgId, String orgRoleId) throws Exception {
        HttpAction<HttpDelete> httpDelete = base.delete(client, "/api/security/orgRoleGroups/" + orgId + "/" + orgRoleId, KeycloakClient.instance().getAuthorizationHeader());
        EntityUtils.consumeQuietly(httpDelete.getResponse().getEntity());
        Assert.assertEquals(200, httpDelete.getStatusCode());
    }
}
