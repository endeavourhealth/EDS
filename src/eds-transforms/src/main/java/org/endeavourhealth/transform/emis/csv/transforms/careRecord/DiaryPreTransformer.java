package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.Map;

public class DiaryPreTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(Diary.class);
        while (parser.nextRecord()) {

            try {
                createResource((Diary)parser, fhirResourceFiler, csvHelper, version);
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }


    private static void createResource(Diary parser,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper,
                                       String version) throws Exception {

        if (parser.getDeleted()) {
            return;
        }

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            String diaryGuid = parser.getDiaryGuid();
            String patientGuid = parser.getPatientGuid();

            csvHelper.cacheConsultationRelationship(consultationGuid,
                    patientGuid,
                    diaryGuid,
                    ResourceType.ProcedureRequest);
        }
    }

}
