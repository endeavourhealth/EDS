package org.endeavourhealth.reference;

import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ScheduledTaskAuditDalI;
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

        ConfigManager.Initialize("ReferenceUpdater");
        String type = args[0];

        try {
            if (type.equalsIgnoreCase("deprivation")) {
                DeprivationUpdater.updateDeprivationScores(args);

            } else if (type.equalsIgnoreCase("opcs4")) {
                Opcs4Updater.updateOpcs4Lookup(args);

            } else if (type.equalsIgnoreCase("snomed")) {
                SnomedAndDMDUpdater.updateSnomedConceptsAndDescriptions(false, args);

            } else if (type.equalsIgnoreCase("dmd")) {
                SnomedAndDMDUpdater.updateSnomedConceptsAndDescriptions(true, args);

            } else if (type.equalsIgnoreCase("snomedToBnfChapter")) {
                SnomedToBnfChapterUpdater.updateSnomedToBnfChapterLookup(args);

            } else if (type.equalsIgnoreCase("icd10")) {
                Icd10Updater.updateIcd10Lookup(args);

            } else if (type.equalsIgnoreCase("ons_all")) {
                OnsUpdater.updateOns(args);

            } else if (type.equalsIgnoreCase("emis_clinical_codes")) {
                EmisClinicalCodesIMUpdater.updateEmisClinicalCodes(args);

            } else if (type.equalsIgnoreCase("tpp_clinical_codes")) {
                TppClinicalCodesIMUpdater.updateTppClinicalCodes(args);

            } else if (type.equalsIgnoreCase("vision_clinical_codes")) {
                VisionClinicalCodesIMUpdater.updateVisionClinicalCodes(args);

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

            auditSuccess(type, args);

        } catch (Throwable ex) {
            LOG.error("", ex);
            auditFailure(type, args, ex);
        }
    }

    private static void auditSuccess(String queryName, String[] args) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskSuccess(queryName, args);
    }

    private static void auditFailure(String queryName, String[] args, Throwable t) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskFailure(queryName, args, t);
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
