package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;

public class EnumRegistry {
    public static void register() {
        CodecRegistry.DEFAULT_INSTANCE.register(new EnumNameCodec<>(EventStoreMode.class));
        CodecRegistry.DEFAULT_INSTANCE.register(new EnumNameCodec<>(PersonSex.class));
    }

}
