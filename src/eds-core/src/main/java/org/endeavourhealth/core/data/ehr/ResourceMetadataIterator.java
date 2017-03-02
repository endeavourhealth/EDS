package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.Row;
import org.endeavourhealth.core.fhirStorage.metadata.ResourceMetadata;
import org.endeavourhealth.common.utility.JsonSerializer;

import java.util.Iterator;
import java.util.function.Consumer;

public class ResourceMetadataIterator<T extends ResourceMetadata> implements Iterator<T> {
    private static final String METADATA_COLUMN_NAME = "resource_metadata";

    private final Iterator<Row> rowIterator;
    private final Class<T> classOfT;

    public ResourceMetadataIterator(Iterator<Row> rowIterator, Class<T> classOfT) {
        this.rowIterator = rowIterator;
        this.classOfT = classOfT;
    }

    @Override
    public boolean hasNext() {
        return rowIterator.hasNext();
    }

    @Override
    public T next() {
        T result = null;

        Row row = rowIterator.next();
        if (row != null) {
            String mataDataString = row.getString(METADATA_COLUMN_NAME);
            if (mataDataString != null) {
                result = JsonSerializer.deserialize(mataDataString, classOfT);
            }
        }

        return result;
    }

    @Override
    public void remove() {
        rowIterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }
}
