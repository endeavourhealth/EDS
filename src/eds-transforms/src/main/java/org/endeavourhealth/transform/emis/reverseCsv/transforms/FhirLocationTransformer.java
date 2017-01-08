package org.endeavourhealth.transform.emis.reverseCsv.transforms;

import org.endeavourhealth.transform.emis.reverseCsv.schema.AbstractCsvWriter;
import org.hl7.fhir.instance.model.Resource;

import java.util.Map;

public class FhirLocationTransformer extends AbstractTransformer {


    @Override
    protected void transform(Resource resource, String sourceId, Map<Class, AbstractCsvWriter> writers) {

    }

    @Override
    protected void transformDeleted(String sourceId, Map<Class, AbstractCsvWriter> writers) {

    }
}
