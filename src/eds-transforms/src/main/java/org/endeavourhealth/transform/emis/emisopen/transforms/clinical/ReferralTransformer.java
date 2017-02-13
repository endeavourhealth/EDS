package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.ReferralListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.ReferralType;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class ReferralTransformer {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        ReferralListType referralList = medicalRecord.getReferralList();
        if (referralList == null) {
            return;
        }

        for (ReferralType referral : referralList.getReferral()) {
            Resource resource = transform(referral, patientGuid);
            if (resource != null) {
                resources.add(resource);
            }
        }
    }

    public static Resource transform(ReferralType referral, String patientGuid) throws TransformException {

//TODO - finish

        return null;
    }
}
