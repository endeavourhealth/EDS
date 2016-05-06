package org.endeavourhealth.core.utilities.html;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.net.URL;

public class Html
{
    // do caching, singleton, thread safety etc

    public static String GetIndexPage() throws IOException
    {
        return GetResource("IndexPage.html");
    }

    public static String GetErrorPage(int httpStatus) throws IOException
    {
        String errorDescription = Integer.toString(httpStatus) + " " +  HttpStatus.getMessage(httpStatus);

        return GetResource("ErrorPage.html").replace("{{error-description}}", errorDescription);
    }

    private static String GetResource(String resourceName) throws IOException
    {
        URL url = Html.class.getResource(resourceName);
        return IOUtils.toString(url.openStream());
    }
}
