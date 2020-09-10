package jp.kogenet.example.contents;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import jp.kogenet.example.cointents.models.Staff;
import jp.kogenet.example.cointents.models.Team;
import jp.kogenet.example.cointents.utils.gson.AnnotationExclusionStrategy;
import jp.kogenet.example.cointents.utils.gson.LocalDateTimeTypeAdapter;
import jp.kogenet.example.cointents.utils.gson.LocalDateTypeAdapter;
import jp.kogenet.example.cointents.utils.gson.ZonedDateTimeTypeAdapter;

public class GsonTests {

    private static final String jsonText = getJsonText();
    private Team team;

    private static String getJsonText() {
        var path = Paths.get("src/test/data/contents", "data.json");
        try {
            return Files.readString(path, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println(e.toString());
            return null;
        }
    }

    private static Team getTeamInstance() {
        var staff = new Staff();
        staff.setName("Bob");
        staff.setNumOfYears(35);
        staff.setPosition(new String[] { "Founder", "CTO", "Writer" });
        @SuppressWarnings("serial")
        Map<String, BigDecimal> salary = new HashMap<>() {
            {
                put("2010", new BigDecimal(10000));
                put("2012", new BigDecimal(12000));
                put("2018", new BigDecimal(14000));
            }
        };
        staff.setSalary(salary);
        staff.setSkills(Arrays.asList("java", "python", "node", "kotlin"));
        staff.setBirthday(LocalDate.of(1970, 4, 1));
        staff.setLastUpdateAt(ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167000000, ZoneId.of("Asia/Tokyo")));

        staff.setLocaleTimeZone(TimeZone.getDefault());

        return Team.create("Avoid project.", LocalDate.of(2020, 9, 1),
                () -> Arrays.asList(staff));
    }

    @BeforeEach
    void setUp() {
        this.team = getTeamInstance();
    }

    @Test
    void testJavaToJson() throws JSONException, IOException {

        // Gson gson = new Gson();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(LocalDate.class,
                        new LocalDateTypeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class,
                        new ZonedDateTimeTypeAdapter().nullSafe())
                // .excludeFieldsWithoutExposeAnnotation() // enable @Expose()
                .setExclusionStrategies(new AnnotationExclusionStrategy())
                // .enableComplexMapKeySerialization()
                // .serializeNulls()
                // .setDateFormat(DateFormat.LONG) // do not work.
                .setFieldNamingPolicy(
                        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                // .setPrettyPrinting()
                .setVersion(1.0) // enable @Since()
                .create();

        final String actual = gson.toJson(team);

        System.out.println(actual);
        JSONAssert.assertEquals(jsonText, actual, JSONCompareMode.STRICT);

    }

    @Test
    void testJsonToJava() {

        // Gson gson = new Gson();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(LocalDate.class,
                        new LocalDateTypeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class,
                        new ZonedDateTimeTypeAdapter().nullSafe())
                // .setExclusionStrategies(new AnnotationExclusionStrategy())
                .setFieldNamingPolicy(
                        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        final Team actual = gson.fromJson(jsonText, Team.class);

        assertNotNull(actual);
        assertNotEquals(this.team, actual);

        assertEquals(this.team.getName(), actual.getName());
        assertEquals(this.team.getStartAt(), actual.getStartAt());
        assertEquals(this.team.getMembers().size(), actual.getMembers().size());

        final Staff expectedStaff = this.team.getMembers().get(0);
        final Staff actualStaff = actual.getMembers().get(0);

        assertEquals(expectedStaff.getName(), actualStaff.getName());
        assertEquals(expectedStaff.getNumOfYears(),
                actualStaff.getNumOfYears());

        assertArrayEquals(expectedStaff.getPosition(),
                actualStaff.getPosition());
        // List<>, Map<> is work probably thanks to toString() method override.
        assertEquals(expectedStaff.getSalary(), actualStaff.getSalary());
        assertEquals(expectedStaff.getSkills(), actualStaff.getSkills());

        assertEquals(expectedStaff.getBirthday(), actualStaff.getBirthday());
        assertEquals(expectedStaff.getLastUpdateAt(),
                actualStaff.getLastUpdateAt()
                        .withZoneSameInstant(ZoneId.of("Asia/Tokyo")));

        assertNull(actualStaff.getLocaleTimeZone());

    }

}
