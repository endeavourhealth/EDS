package org.endeavourhealth.common.cassandra.ehr;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import org.hl7.fhir.instance.model.Enumerations;

public class EnumRegistry {
    public static void register() {
        CodecRegistry.DEFAULT_INSTANCE.register(new EnumNameCodec<>(EventStoreMode.class));
        CodecRegistry.DEFAULT_INSTANCE.register(new EnumNameCodec<>(Enumerations.AdministrativeGender.class));
    }

}
