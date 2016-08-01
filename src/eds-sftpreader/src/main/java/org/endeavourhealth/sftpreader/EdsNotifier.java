package org.endeavourhealth.sftpreader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

public class EdsNotifier
{
    public void notifyEds(String url, String message) throws IOException
    {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new ByteArrayEntity(message.getBytes()));

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        if (entity != null)
        {
            InputStream instream = entity.getContent();

            try
            {
                // do something useful
            }
            finally
            {
                instream.close();
            }
        }
    }
}
