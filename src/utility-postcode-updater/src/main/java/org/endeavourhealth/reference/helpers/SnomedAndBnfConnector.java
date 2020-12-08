package org.endeavourhealth.reference.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * 1. Connect to BNF website
 * 2. parse the html page and get the url of zip file
 * 3. download the zip file locally
 */
public class SnomedAndBnfConnector {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnomedAndBnfConnector.class);

    private static final String PATTERN_ZIP = ".zip";

    public String downloadBNFAndSNOMEDMappingFile(String websiteURL, String localPath, String baseURL) throws Exception {

        //search the html content and get the line containing .zip from the html
        String htmlURLString = getURL(websiteURL);
        String urlForZipFile = null;
        String zipFileName = null;

        //get the resource locator of zip file
        if (htmlURLString != null) {
            urlForZipFile = parseURL(htmlURLString);
            LOG.info("resource url: " + urlForZipFile);
        }

        //build the full url to be used to download the zip file
        if (urlForZipFile != null) {
            urlForZipFile = baseURL.concat(urlForZipFile);
            LOG.info("full url: " + urlForZipFile);
            int index = urlForZipFile.lastIndexOf("/");
            zipFileName = urlForZipFile.substring(index + 1, urlForZipFile.length());
            LOG.info("zip file name : " + zipFileName);
        }
        //download the zip file
        URL url = new URL(urlForZipFile);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream(localPath + zipFileName);
        //download zip locally
        copy(in, out, 1024);
        out.close();

        return zipFileName;
    }
    /**
     * search the html content and get the line containing .zip from the html
     */
    private String getURL(String urlToRead) throws Exception {
        URL url; // The URL to read
        HttpURLConnection conn; // The actual connection to the web page
        BufferedReader rd; // Used to read results from the web page
        String line; // An individual line of the web page HTML
        //String result = ""; // A long string containing all the HTML
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                if (line.contains(PATTERN_ZIP)) {
                    return line;
                }
            }
            rd.close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * parse the zip file href from the html string
     */
    private static String parseURL(String sourceString) {
        // <p><a href="/sites/default/files/2020-10/BNF%20Snomed%20Mapping%20data%2020201021.zip" target="_blank" title="October 2020 BNF/Snomed mapping document">SNOMED - BNF mapping document October 2020 (ZIP file: 15MB)</a></p>
        StringTokenizer st = new StringTokenizer(sourceString, "\"");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().toString();
            if(token.contains(PATTERN_ZIP)) {
                return token;
            }
        }
        return null;
    }

    /**
     * download the zip
     */
    private void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }
}
