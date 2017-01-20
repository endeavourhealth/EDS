package org.endeavourhealth.transform.emis.reverseCsv.transforms;

import org.endeavourhealth.transform.common.AbstractCsvWriter;
import org.hl7.fhir.instance.model.Resource;

import java.util.Map;

public class FhirScheduleTransformer extends AbstractTransformer {


    @Override
    protected void transform(Resource resource, String sourceId, Map<Class, AbstractCsvWriter> writers) {

    }

    @Override
    protected void transformDeleted(String sourceId, Map<Class, AbstractCsvWriter> writers) {

    }
}
