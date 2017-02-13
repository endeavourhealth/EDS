package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.InvestigationListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.InvestigationType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class InvestigationTransformer {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        InvestigationListType investigationList = medicalRecord.getInvestigationList();
        if (investigationList == null) {
            return;
        }

        for (InvestigationType investigation : investigationList.getInvestigation()) {
            Resource resource = transform(investigation, patientGuid);
            if (resource != null) {
                resources.add(resource);
            }
        }
    }

    public static Resource transform(InvestigationType investigation, String patientGuid) throws TransformException {

//TODO - finish

        return null;
    }
}
