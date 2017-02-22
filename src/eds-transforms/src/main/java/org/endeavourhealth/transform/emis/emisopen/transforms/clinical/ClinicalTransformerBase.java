package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AuthorType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IdentType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DomainResource;
import org.hl7.fhir.instance.model.Reference;

import java.util.Date;

public abstract class ClinicalTransformerBase {


    protected static Date findRecordedDate(AuthorType authorType) throws TransformException {
        if (authorType == null) {
            return null;
        }
        String dateStr = authorType.getSystemDate();
        if (Strings.isNullOrEmpty(dateStr)) {
            return null;
        }

        return DateConverter.getDate(dateStr);
    }

    protected static String findRecordedUserGuid(AuthorType authorType) {
        if (authorType == null) {
            return null;
        }
        IdentType identType = authorType.getUser();
        if (identType == null) {
            return null;
        }

        return identType.getGUID();
    }

    protected static void addRecordedByExtension(DomainResource resource, String recordedByGuid) throws TransformException {
        if (Strings.isNullOrEmpty(recordedByGuid)) {
            return;
        }

        Reference reference = EmisOpenHelper.createPractitionerReference(recordedByGuid);
        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
    }

    protected static void addRecordedDateExtension(DomainResource resource, Date recordedDate) throws TransformException {
        if (recordedDate == null) {
            return;
        }

        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(recordedDate)));
    }
}
