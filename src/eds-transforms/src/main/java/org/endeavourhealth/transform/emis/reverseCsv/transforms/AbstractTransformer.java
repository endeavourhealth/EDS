package org.endeavourhealth.transform.emis.reverseCsv.transforms;

import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.ResourceIdMapRepository;
import org.endeavourhealth.core.data.transform.models.ResourceIdMapByEdsId;
import org.endeavourhealth.transform.common.AbstractCsvWriter;
import org.hl7.fhir.instance.model.Resource;

import java.util.Map;

public abstract class AbstractTransformer {

    private ResourceIdMapRepository idMapRepository = new ResourceIdMapRepository();
    protected static final ParserPool PARSER_POOL = new ParserPool();

    public void transform(ResourceByExchangeBatch resourceWrapper, Map<Class, AbstractCsvWriter> writers) throws Exception {

        //find the source local ID for our EDS ID
        ResourceIdMapByEdsId obj = idMapRepository.getResourceIdMapByEdsId(resourceWrapper.getResourceType(), resourceWrapper.getResourceId());
        String sourceId = obj.getSourceId();

        if (resourceWrapper.getIsDeleted()) {

            transformDeleted(sourceId, writers);

        } else {

            String json = resourceWrapper.getResourceData();
            Resource resource = PARSER_POOL.parse(json);
            transform(resource, sourceId, writers);
        }
    }

    protected abstract void transform(Resource resource, String sourceId, Map<Class, AbstractCsvWriter> writers) throws Exception;
    protected abstract void transformDeleted(String sourceId, Map<Class, AbstractCsvWriter> writers) throws Exception;

}
