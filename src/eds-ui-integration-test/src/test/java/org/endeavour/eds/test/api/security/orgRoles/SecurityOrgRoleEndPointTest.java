package org.endeavour.eds.test.api.security.orgRoles;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.endeavour.eds.test.AuthHelper;
import org.endeavourhealth.core.security.keycloak.client.KeycloakClient;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.test.HttpAction;
import org.ocpsoft.rewrite.test.RewriteTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void testList() throws Exception {
        AuthHelper.auth();

        HttpClient client = new DefaultHttpClient();

        HttpAction<HttpGet> httpGet = get(client, "/api/security/orgRoles/00000000-0000-0000-0000-000000000000", KeycloakClient.instance().getAuthorizationHeader());

        String response = httpGet.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        LOG.info("--------------------------------------------------");
        LOG.info("--------------------------------------------------");
        LOG.info("--------------------------------------------------");
        LOG.info("--------------------------------------------------");
        LOG.info(response);
        LOG.info("--------------------------------------------------");
        LOG.info("--------------------------------------------------");
        LOG.info("--------------------------------------------------");
        LOG.info("--------------------------------------------------");

        Assert.assertEquals(200, httpGet.getStatusCode());
    }
}
