package org.endeavourhealth.reference;

import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceCopierDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
            if (type.equalsIgnoreCase("deprivation")) {
                DeprivationUpdater.updateDeprivationScores(args);

            } else if (type.equalsIgnoreCase("opcs4")) {
                Opcs4Updater.updateOpcs4Lookup(args);

            } else if (type.equalsIgnoreCase("snomed")) {
                SnomedUpdater.updateSnomedConceptsAndDescriptions(args);

            } else if (type.equalsIgnoreCase("snomedToBnfChapter")) {
                SnomedToBnfChapterUpdater.updateSnomedToBnfChapterLookup(args);

            } else if (type.equalsIgnoreCase("icd10")) {
                Icd10Updater.updateIcd10Lookup(args);

            } else if (type.equalsIgnoreCase("ons_all")) {
                /*
                * 1. Download the latest "NHS Postcode Directory UK Full" dataset from the ONS:
                * https://ons.maps.arcgis.com/home/search.html?q=NHS%20Postcode%20Directory%20UK%20Full&start=1&sortOrder=desc&sortField=modified#content
                * 2. Unzip somewhere
                * 3. Run this app with parameters: ons_all <path to unzipped content>
                *
                * Feb 2019 data at: https://ons.maps.arcgis.com/home/item.html?id=e1dc68a2c7f64adeb834bd089bd87ca5
                * Nov 2018 data at: https://ons.maps.arcgis.com/home/item.html?id=3506c198565a444d9432d31f85257ade
                * Aug 2018 data at: https://ons.maps.arcgis.com/home/item.html?id=1ad8f296756447bf87b011ec445391fc
                * May 2018 data at: https://ons.maps.arcgis.com/home/item.html?id=726532de7e62432dbc0d443c22ad810f
                * Aug 2016 data at: http://ons.maps.arcgis.com/home/item.html?id=dc23a64fa2e34e1289901b27d91c335b
                */


                File lsoaFile = LsoaUpdater.findFile(args);
                File msoaFile = MsoaUpdater.findFile(args);
                File ccgFile = CcgUpdater.findFile(args);
                File wardFile = WardUpdater.findFile(args);
                File laFile = LocalAuthorityUpdater.findFile(args);
                File postcodeFile = PostcodeUpdater.findFile(args);

                LsoaUpdater.updateLsoas(lsoaFile);
                MsoaUpdater.updateMsoas(msoaFile);
                CcgUpdater.updateCcgs(ccgFile);
                WardUpdater.updateWards(wardFile);
                LocalAuthorityUpdater.updateLocalAuthorities(laFile);
                PostcodeUpdater.updatePostcodes(postcodeFile);

            } else if (type.equalsIgnoreCase("ons_lsoa")) {
                File lsoaFile = LsoaUpdater.findFile(args);
                LsoaUpdater.updateLsoas(lsoaFile);

            } else if (type.equalsIgnoreCase("ons_msoa")) {
                File msoaFile = MsoaUpdater.findFile(args);
                MsoaUpdater.updateMsoas(msoaFile);

            } else if (type.equalsIgnoreCase("ons_ccg")) {
                File ccgFile = CcgUpdater.findFile(args);
                CcgUpdater.updateCcgs(ccgFile);

            } else if (type.equalsIgnoreCase("ons_ward")) {
                File wardFile = WardUpdater.findFile(args);
                WardUpdater.updateWards(wardFile);

            } else if (type.equalsIgnoreCase("ons_local_authority")) {
                File laFile = LocalAuthorityUpdater.findFile(args);
                LocalAuthorityUpdater.updateLocalAuthorities(laFile);

            } else if (type.equalsIgnoreCase("ons_postcode")) {
                File postcodeFile = PostcodeUpdater.findFile(args);
                PostcodeUpdater.updatePostcodes(postcodeFile);

            } else if (type.equalsIgnoreCase("copy_all")) {

                if (args.length != 2) {
                    LOG.error("Incorrect number of parameters");
                    LOG.error("Usage: copy_all <enterprise config name>");
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

    public static File findFile(String fileExt, String fileNameRegex, String root, String... path) {
        File dir = new File(root);
        if (!dir.exists()) {
            throw new RuntimeException("" + dir + " does not exist");
        }
        for (String pathDir: path) {
            dir = new File(dir, pathDir);
            if (!dir.exists()) {
                throw new RuntimeException("" + dir + " does not exist");
            }
        }

        List<File> matches = new ArrayList<>();
        for (File child: dir.listFiles()) {
            String name = child.getName();
            String ext = FilenameUtils.getExtension(name);
            if (ext.equalsIgnoreCase(fileExt)) {
                String baseName = FilenameUtils.getBaseName(name);
                if (Pattern.matches(fileNameRegex, baseName)) {
                    matches.add(child);
                }
            }
        }

        if (matches.size() == 1) {
            return matches.get(0);

        } else if (matches.isEmpty()) {
            throw new RuntimeException("Failed to find " + fileExt + " file matching " + fileNameRegex + " in " + dir);
        } else {
            throw new RuntimeException("Found " + matches.size() + " " + fileExt + " files matching " + fileNameRegex + " in " + dir);
        }
    }
}
