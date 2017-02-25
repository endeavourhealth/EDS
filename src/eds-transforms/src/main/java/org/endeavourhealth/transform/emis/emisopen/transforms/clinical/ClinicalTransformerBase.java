package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DomainResource;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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


    protected static List<QualifierType> findQualifiers(CodedItemBaseType codedItem, String qualifierGroupName) {
        List<QualifierType> ret = new ArrayList<>();

        if (codedItem.getQualifierList() != null) {

            for (QualifierType qualifier : codedItem.getQualifierList().getQualifier()) {
                IntegerCodeType qualifierGroup = qualifier.getGroup();
                if (qualifierGroup != null) {
                    String term = qualifierGroup.getTerm();
                    if (term != null
                            && term.equalsIgnoreCase(qualifierGroupName)) {
                        ret.add(qualifier);
                    }
                }
            }
        }

        return ret;
    }

    protected static List<String> findQualifierTerms(CodedItemBaseType codedItem, String qualifierGroupName) {
        List<String> ret = new ArrayList<>();

        List<QualifierType> qualifiers = findQualifiers(codedItem, qualifierGroupName);
        for (QualifierType qualifier: qualifiers) {
            IntegerCodeType item = qualifier.getQualifierItemID();
            if (item != null) {
                String term = item.getTerm();
                if (!Strings.isNullOrEmpty(term)) {
                    ret.add(term);
                }
            }
        }
        return ret;
    }

    protected static QualifierType findQualifier(CodedItemBaseType codedItem, String qualifierGroupName) {
        if (codedItem.getQualifierList() == null) {
            return null;
        }
        for (QualifierType qualifier: codedItem.getQualifierList().getQualifier()) {
            IntegerCodeType qualifierGroup = qualifier.getGroup();
            if (qualifierGroup != null) {
                String term = qualifierGroup.getTerm();
                if (term != null
                        && term.equalsIgnoreCase(qualifierGroupName)) {
                    return qualifier;
                }
            }
        }

        return null;
    }

    protected static String findQualifierTerm(CodedItemBaseType codedItem, String qualifierGroupName) {
        QualifierType qualifier = findQualifier(codedItem, qualifierGroupName);
        if (qualifier != null) {
            IntegerCodeType item = qualifier.getQualifierItemID();
            if (item != null) {
                return item.getTerm();
            }
        }
        return null;
    }

    protected static void createProblemIfRequired(CodedItemBaseType codedItem, List<Resource> resources) {

        //if the coded item isn't a problem, just reutn out
        if (codedItem.getProblem() == null) {
            return;
        }


    }
}
