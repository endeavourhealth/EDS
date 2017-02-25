package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvestigationTransformer extends ClinicalTransformerBase {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        InvestigationListType investigationList = medicalRecord.getInvestigationList();
        if (investigationList == null) {
            return;
        }

        for (InvestigationType investigation : investigationList.getInvestigation()) {
            transform(investigation, resources, patientGuid);
        }
    }

    public static void transform(InvestigationType investigation, List<Resource> resources, String patientGuid) throws TransformException {

        DiagnosticReport fhirReport = new DiagnosticReport();
        fhirReport.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_REPORT));

        String reportGuid = investigation.getGUID();
        EmisOpenHelper.setUniqueId(fhirReport, patientGuid, reportGuid);

        fhirReport.setSubject(EmisOpenHelper.createPatientReference(patientGuid));

        fhirReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        IdentType author = investigation.getAuthorID();
        if (author != null) {
            Reference reference = EmisOpenHelper.createPractitionerReference(author.getGUID());
            fhirReport.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.DIAGNOSTIC_REPORT_FILED_BY, reference));
        }

        fhirReport.setCode(CodeConverter.convert(investigation.getCode(), investigation.getDisplayTerm()));

        String text = investigation.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirReport.setConclusion(text);
        }

        fhirReport.setEffective(DateConverter.convertPartialDateToDateTimeType(investigation.getAssignedDate(), investigation.getAssignedTime(), investigation.getDatePart()));

        Date dateRecorded = findRecordedDate(investigation.getOriginalAuthor());
        addRecordedDateExtension(fhirReport, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(investigation.getOriginalAuthor());
        addRecordedByExtension(fhirReport, recordedByGuid);

        resources.add(fhirReport);

        EventListType events = investigation.getEventList();
        if (events != null) {

            List<Resource> childResources = new ArrayList<>();

            for (EventType event: events.getEvent()) {
                EventTransformer.transform(event, childResources, patientGuid);
            }

            for (Resource childResource: childResources) {

                //link the child resource to its parent
                Reference reference = ReferenceHelper.createReferenceExternal(childResource);
                fhirReport.getResult().add(reference);

                resources.add(childResource);
            }
        }
    }
}
