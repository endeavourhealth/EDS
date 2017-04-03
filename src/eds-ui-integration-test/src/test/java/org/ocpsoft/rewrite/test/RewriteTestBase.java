package org.ocpsoft.rewrite.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.endeavourhealth.core.security.keycloak.client.resources.HttpDeleteWithBody;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Base utility class for Rewrite Tests.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class RewriteTestBase
{
    /**
     * Resolve an {@link Archive} from Maven coordinates.
     */
    protected static File[] resolveDependencies(final String coords)
    {
        return Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(coords)
                .withTransitivity()
                .asFile();
    }

    /**
     * Resolve an {@link Archive} from Maven coordinates.
     */
    protected static File resolveDependency(final String coords)
    {
        return resolveDependencies(coords)[0];
    }

    /**
     * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
     * automatically prepended to the given path.
     * <p>
     * E.g: A path of '/example' will be sent as '/rewrite-test/example'
     *
     * @throws Exception
     */
    protected HttpAction<HttpGet> get(final String path) throws Exception
    {
        HttpClient client = HttpClientBuilder.create().build();;
        return get(client, path);
    }

    /**
     * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
     * automatically prepended to the given path.
     * <p>
     * E.g: A path of '/example' will be sent as '/rewrite-test/example'
     *
     * @throws Exception
     */
    protected HttpAction<HttpGet> get(HttpClient client, String path) throws Exception
    {
        return get(client, path, new Header[0]);
    }

    /**
     * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
     * automatically prepended to the given path.
     * <p>
     * E.g: A path of '/example' will be sent as '/rewrite-test/example'
     *
     * @throws Exception
     */
    public HttpAction<HttpGet> get(HttpClient client, String path, Header... headers) throws Exception
    {
        HttpGet request = new HttpGet(getBaseURL() + getContextPath() + path);
        if (headers != null && headers.length > 0) {
            request.setHeaders(headers);
        }
        HttpContext context = new BasicHttpContext();
        HttpResponse response = client.execute(request, context);

        return new HttpAction<HttpGet>(client, context, request, response, getBaseURL(), getContextPath());
    }

    protected HttpAction<HttpPost> post(HttpClient client, String path, String jsonContent, Header... headers) throws Exception
    {
        HttpPost request = new HttpPost(getBaseURL() + getContextPath() + path);
        if (headers != null && headers.length > 0) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        HttpContext context = new BasicHttpContext();
        HttpResponse response = client.execute(request, context);

        return new HttpAction<HttpPost>(client, context, request, response, getBaseURL(), getContextPath());
    }

    protected HttpAction<HttpPut> put(HttpClient client, String path, String jsonContent, Header... headers) throws Exception
    {
        HttpPut request = new HttpPut(getBaseURL() + getContextPath() + path);
        if (headers != null && headers.length > 0) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        HttpContext context = new BasicHttpContext();
        HttpResponse response = client.execute(request, context);

        return new HttpAction<HttpPut>(client, context, request, response, getBaseURL(), getContextPath());
    }

    public HttpAction<HttpDelete> delete(HttpClient client, String path, Header... headers) throws Exception
    {
        HttpDelete request = new HttpDelete(getBaseURL() + getContextPath() + path);
        if (headers != null && headers.length > 0) {
            request.setHeaders(headers);
        }
        HttpContext context = new BasicHttpContext();
        HttpResponse response = client.execute(request, context);

        return new HttpAction<HttpDelete>(client, context, request, response, getBaseURL(), getContextPath());
    }

    protected HttpAction<HttpDeleteWithBody> delete(HttpClient client, String path, String jsonContent, Header... headers) throws Exception
    {
        HttpDeleteWithBody request = new HttpDeleteWithBody(getBaseURL() + getContextPath() + path);
        if (headers != null && headers.length > 0) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        HttpContext context = new BasicHttpContext();
        HttpResponse response = client.execute(request, context);

        return new HttpAction<HttpDeleteWithBody>(client, context, request, response, getBaseURL(), getContextPath());
    }

    /**
     * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
     * automatically prepended to the given path.
     * <p>
     * E.g: A path of '/example' will be sent as '/rewrite-test/example'
     */
    protected HttpAction<HttpHead> head(final String path)
    {
        HttpClient client = HttpClientBuilder.create().build();;
        try
        {
            HttpHead request = new HttpHead(getBaseURL() + getContextPath() + path);
            HttpContext context = new BasicHttpContext();
            HttpResponse response = client.execute(request, context);

            return new HttpAction<HttpHead>(client, context, request, response, getBaseURL(), getContextPath());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    //@ArquillianResource
    URL baseUrl;

    protected String getBaseURL()
    {
        if(baseUrl == null) {
            try {
                baseUrl = new URL("http://localhost:8080");
            } catch (MalformedURLException e) {

            }
        }

        return baseUrl.getProtocol() + "://" + baseUrl.getHost()
                + (baseUrl.getPort() == -1 ? "" : ":" + baseUrl.getPort());
    }

    protected String getContextPath()
    {
        String contextPath = baseUrl.getPath();
        if (!"/".equals(contextPath))
            contextPath = contextPath.replaceAll("^(.*)/$", "$1").replaceAll("ROOT$", "");
        return contextPath;
    }

    protected HtmlAction getWebClient(String path) throws FailingHttpStatusCodeException, IOException
    {
        try {
            WebClient client = new WebClient();
            return new HtmlAction(client, (HtmlPage) client.getPage(getBaseURL() + getContextPath() + path));
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies that the given text contains the given string.
     */
    protected static void assertContains(String text, String s)
    {
        if (text == null || s == null || !text.contains(s)) {
            Assert.fail("Could not find [" + s + "] in text: " + text);
        }
    }

}