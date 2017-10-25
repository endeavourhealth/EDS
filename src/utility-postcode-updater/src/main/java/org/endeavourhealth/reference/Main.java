package org.endeavourhealth.reference;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceCopierDalI;
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

            } else if (type.equalsIgnoreCase("copy_all")) {

                if (args.length != 2) {
                    LOG.error("Incorrect number of parameters");
                    LOG.error("Usage: copy_msoa <enterprise config name>");
                    return;
                }
                String enterpriseConfigName = args[1];
                ReferenceCopierDalI referenceCopierDal = DalProvider.factoryReferenceCopierDal();
                referenceCopierDal.copyReferenceDataToEnterprise(enterpriseConfigName);

            } else {
                LOG.error("Unknown first argument " + type);
            }

        } catch (Exception ex) {
            LOG.error("", ex);
        }

        System.exit(0);
    }
}
