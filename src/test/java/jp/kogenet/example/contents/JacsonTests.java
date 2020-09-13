package jp.kogenet.example.contents;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import jp.kogenet.example.cointents.models.Staff;
import jp.kogenet.example.cointents.models.Team;

public class JacsonTests {

    ObjectMapper mapper;

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
        staff.setLastUpdateAt(ZonedDateTime.of(2018, 1, 12, 13, 14, 15, 167000000, ZoneId.of("Asia/Tokyo")));

        staff.setLocaleTimeZone(TimeZone.getDefault());

        return Team.create("Avoid project.", LocalDate.of(2020, 9, 1), () -> Arrays.asList(staff));
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                // use java 8 Date and Time APIs.
                .registerModule(new JavaTimeModule()).setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // .enable(SerializationFeature.INDENT_OUTPUT)
        // .setTimeZone(TimeZone.getDefault())
        ;
    }

    @Test
    void testJavaToJson() throws JSONException, IOException {
        final Team team = getTeamInstance();
        final String expected = getJsonText();

        // TODO Want to adjust ZonedDateTime to UTC.
        var d = team.getMembers().get(0).getLastUpdateAt().withZoneSameInstant(ZoneId.of("UTC"));
        team.getMembers().get(0).setLastUpdateAt(d);

        final String actual = mapper.writeValueAsString(team);

        System.out.println(actual);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);

    }

    @Test
    void etstJsonToJava() {

    }
}
