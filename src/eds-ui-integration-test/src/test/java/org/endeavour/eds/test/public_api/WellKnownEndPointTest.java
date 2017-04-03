package org.endeavour.eds.test.public_api;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.test.HttpAction;
import org.ocpsoft.rewrite.test.RewriteTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@RunWith(Arquillian.class)
public class WellKnownEndPointTest extends RewriteTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(WellKnownEndPointTest.class);

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
    public void testAuthConfig() throws Exception {
        HttpClient client = HttpClientBuilder.create().build();;

        HttpAction<HttpGet> authconfig = get(client, "/public/wellknown/authconfig");
        Assert.assertEquals(200, authconfig.getStatusCode());

        String response = authconfig.getResponseContent();
        Assert.assertTrue(StringUtils.isNotEmpty(response));

        if(LOG.isDebugEnabled()) {
            LOG.debug(response);
        }
    }
}
