package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.TestRequestHeaderType;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class TestRequestHeaderTransformer {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        MedicalRecordType.TestRequestHeaderList testRequestList = medicalRecord.getTestRequestHeaderList();
        if (testRequestList == null) {
            return;
        }

        for (TestRequestHeaderType testRequest : testRequestList.getTestRequestHeader()) {
            Resource resource = transform(testRequest, patientGuid);
            if (resource != null) {
                resources.add(resource);
            }
        }
    }

    public static Resource transform(TestRequestHeaderType testRequest, String patientGuid) throws TransformException {

//TODO - finish

        return null;
    }
}
