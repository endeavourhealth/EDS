package org.endeavourhealth.reference;

import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /**
     * utility to update the various tables in the reference DB
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            LOG.error("Incorrect number of parameters");
            LOG.error("First parameter must be one of lsoa, msoa, postcode, deprivation");
            return;
        }

        ConfigManager.Initialize("PostcodeUpdater");

        try {

            String type = args[0];
            if (type.equalsIgnoreCase("lsoa")) {
                LsoaUpdater.updateLsoas(args);

            } else if (type.equalsIgnoreCase("msoa")) {
                LsoaUpdater.updateMsoas(args);

            } else if (type.equalsIgnoreCase("postcode")) {
                PostcodeUpdater.updatePostcodes(args);

            } else if (type.equalsIgnoreCase("deprivation")) {
                DeprivationUpdater.updateDeprivationScores(args);

            } else if (type.equalsIgnoreCase("copy_lsoa")) {
                LsoaCopier.copyLsoas(args);

            } else if (type.equalsIgnoreCase("copy_msoa")) {
                MsoaCopier.copyMsoas(args);

            } else if (type.equalsIgnoreCase("copy_deprivation")) {
                DeprivationCopier.copyDeprivation(args);

            } else {
                LOG.error("Unknown first argument " + type);
            }

            //TODO - test postcode update
            //TODO - test deprivation copying

        } catch (Exception ex) {
            LOG.error("", ex);
        }

        System.exit(0);
    }
}
