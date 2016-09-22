package org.endeavourhealth.eds.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.eds.bootstrap.keycloak.BootstrapKeycloak;
import org.endeavourhealth.eds.bootstrap.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main (String[] args) {

        LOG.info("------------------------------------------");
        LOG.info("           EDS Bootstrapper");
        LOG.info("------------------------------------------");

        LOG.info("{}", (Object[])args);

        if(args.length != 1) {
            LOG.info("Please pass the config file path as the first argument and try again...");
        }

        File configFile = new File(args[0]);

        ObjectMapper objectMapper = new ObjectMapper();

        Config config = null;
        try {
            config = objectMapper.readValue(configFile, Config.class);
        } catch (IOException e) {
            LOG.error("Error reading configuration", e);
            return;
        }

        BootstrapKeycloak b = new BootstrapKeycloak();
        b.bootstrap(config);
    }
}
