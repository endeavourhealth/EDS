package org.endeavourhealth.fhir.cron.utilities;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FHIRAuditUtil {
    public  String getProperty(String propFileName, String key) throws IOException {
        InputStream inputStream = null;
        Properties prop = new Properties();

        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        return prop.getProperty(key);
    }

}
