package jp.kogenet.example.cointents.utils.gson;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter for <code>java.time.ZonedDateTime</code>.
 *
 */
public class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {

    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        out.value(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"))));
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        return ZonedDateTime.parse(in.nextString());
    }

}
