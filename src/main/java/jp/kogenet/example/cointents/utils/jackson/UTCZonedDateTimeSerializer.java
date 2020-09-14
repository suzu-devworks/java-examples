package jp.kogenet.example.cointents.utils.jackson;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class UTCZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

    private static final long serialVersionUID = 1L;

    public UTCZonedDateTimeSerializer() {
        this(null);
    }

    public UTCZonedDateTimeSerializer(Class<ZonedDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator gen,
            SerializerProvider provider) throws IOException {
        gen.writeString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .withZone(ZoneId.of("UTC"))));
    }
}
