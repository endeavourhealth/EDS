package org.endeavourhealth.reference;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.informationmodel.EmisClinicalCodesIMUpdaterDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.EmisCodeDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.models.EmisClinicalCodeForIMUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EmisClinicalCodesIMUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(EmisClinicalCodesIMUpdater.class);

    public static void updateEmisClinicalCodes(String[] args) throws Exception {
        if (args.length != 1) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: emis_clinical_codes");
        }

        LOG.info("Emis Clinical Codes IM Update Starting.");

        updateIMEmisClinicalCodes();

        LOG.info("Finished Emis Clinical Codes IM Update.");
    }

    private static void updateIMEmisClinicalCodes() throws Exception {

        EmisCodeDalI emisCodeDal = DalProvider.factoryEmisCodeDal();
        EmisClinicalCodesIMUpdaterDalI emisClinicalCodesIMUpdaterDal = DalProvider.factoryEmisClinicalCodesIMUpdaterDal();

        List<EmisClinicalCodeForIMUpdate> codeList = emisCodeDal.getClinicalCodesForIMUpdate();

        emisClinicalCodesIMUpdaterDal.updateIMForEmisClinicalCodes(codeList);
    }


}