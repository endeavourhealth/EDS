package org.endeavourhealth.reference;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.informationmodel.VisionClinicalCodesIMUpdaterDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.VisionCodeDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.models.VisionClinicalCodeForIMUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VisionClinicalCodesIMUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(VisionClinicalCodesIMUpdater.class);

    public static void updateVisionClinicalCodes(String[] args) throws Exception {
        if (args.length != 1) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: vision_clinical_codes");
        }

        LOG.info("Vision Clinical Codes IM Update Starting.");

        updateIMVisionClinicalCodes();

        LOG.info("Finished Vision Clinical Codes IM Update.");
    }

    private static void updateIMVisionClinicalCodes() throws Exception {

        VisionCodeDalI visionCodeDal = DalProvider.factoryVisionCodeDal();

        VisionClinicalCodesIMUpdaterDalI visionClinicalCodesIMUpdaterDal = DalProvider.factoryVisionClinicalCodesIMUpdaterDal();

        List<VisionClinicalCodeForIMUpdate> codeList = visionCodeDal.getClinicalCodesForIMUpdate();

        visionClinicalCodesIMUpdaterDal.updateIMForVisionClinicalCodes(codeList);
    }

}