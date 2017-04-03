package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ReferralRequest;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class ReferralTransformer extends ClinicalTransformerBase {
    private static final Logger LOG = LoggerFactory.getLogger(ReferralTransformer.class);

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        ReferralListType referralList = medicalRecord.getReferralList();
        if (referralList == null) {
            return;
        }

        for (ReferralType referral : referralList.getReferral()) {
            transform(referral, resources, patientGuid);
        }
    }

    public static void transform(ReferralType referral, List<Resource> resources, String patientGuid) throws TransformException {

        //create a referral for a basic coded item, then populate with specific referral details
        ReferralRequest fhirReferral = createBasicReferral(referral, patientGuid);

        IdentType consultant = referral.getConsultant();
        if (consultant != null) {
            Reference consultantReference = EmisOpenHelper.createPractitionerReference(consultant.getGUID());
            fhirReferral.addRecipient(consultantReference);
        }

        //TODO - below elements need handling

        /**
         protected IdentType provider;
         protected StringCodeType speciality;
         protected BigInteger requestType;
         protected IdentType team;
         protected String referralReason;
         protected BigInteger community;
         protected BigInteger urgency;
         protected BigInteger nhs;
         protected BigInteger transport;
         protected String referralRef;
         protected IdentType sourceType;
         protected BigInteger direction;
         protected IdentType sourceLocation;
         protected XMLGregorianCalendar datedReferral;
         protected Byte rejected;
         protected String rejectionReason;
         protected Byte accepted;
         protected Byte assessed;
         protected StringCodeType reasonTerm;
         protected String sourceDescription;
         protected IdentType sourceSpeciality;
         protected String referralMode;
         */

        linkToProblem(referral, patientGuid, fhirReferral, resources);

        resources.add(fhirReferral);
    }

    public static void transform(EventType eventType, List<Resource> resources, String patientGuid) throws TransformException {

        ReferralRequest fhirReferral = createBasicReferral(eventType, patientGuid);

        linkToProblem(eventType, patientGuid, fhirReferral, resources);

        resources.add(fhirReferral);
    }

    private static ReferralRequest createBasicReferral(CodedItemBaseType codedItem, String patientGuid) throws TransformException {
        ReferralRequest fhirReferral = new ReferralRequest();
        fhirReferral.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_REFERRAL_REQUEST));

        String eventGuid = codedItem.getGUID();
        EmisCsvHelper.setUniqueId(fhirReferral, patientGuid, eventGuid);

        fhirReferral.setPatient(EmisOpenHelper.createPatientReference(patientGuid));

        fhirReferral.setDateElement(DateConverter.convertPartialDateToDateTimeType(codedItem.getAssignedDate(), codedItem.getAssignedTime(), codedItem.getDatePart()));

        fhirReferral.addServiceRequested(CodeConverter.convert(codedItem.getCode(), codedItem.getDisplayTerm()));

        IdentType author = codedItem.getAuthorID();
        if (author != null) {
            Reference practitionerReference = EmisOpenHelper.createPractitionerReference(author.getGUID());
            fhirReferral.setRequester(practitionerReference);
        }

        String text = codedItem.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirReferral.setDescription(text);
        }

        Date dateRecorded = findRecordedDate(codedItem.getOriginalAuthor());
        addRecordedDateExtension(fhirReferral, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(codedItem.getOriginalAuthor());
        addRecordedByExtension(fhirReferral, recordedByGuid);

        return fhirReferral;
    }
}
