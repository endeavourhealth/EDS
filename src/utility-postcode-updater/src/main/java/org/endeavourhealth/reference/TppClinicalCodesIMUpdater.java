package org.endeavourhealth.reference;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.informationmodel.TppClinicalCodesIMUpdaterDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.TppCtv3SnomedRefDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.models.TppClinicalCodeForIMUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TppClinicalCodesIMUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(TppClinicalCodesIMUpdater.class);

    public static void updateTppClinicalCodes(String[] args) throws Exception {
        if (args.length != 1) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: tpp_clinical_codes");
        }

        LOG.info("TPP Clinical Codes IM Update Starting.");

        updateIMTppClinicalCodes();

        LOG.info("Finished TPP Clinical Codes IM Update.");
    }

    private static void updateIMTppClinicalCodes() throws Exception {

        TppCtv3SnomedRefDalI tppCtv3SnomedRefDal = DalProvider.factoryTppCtv3SnomedRefDal();

        TppClinicalCodesIMUpdaterDalI tppClinicalCodesIMUpdaterDal = DalProvider.factoryTppClinicalCodesIMUpdaterDal();

        List<TppClinicalCodeForIMUpdate> codeList = tppCtv3SnomedRefDal.getClinicalCodesForIMUpdate();

        tppClinicalCodesIMUpdaterDal.updateIMForTppClinicalCodes(codeList);
    }

}