package example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.MATCH_ALL;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.EnumSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class ParameterizedEnumSourceTests {

    @ParameterizedTest
    @EnumSource(ChronoUnit.class)
    void testWithEnumSource(TemporalUnit unit) {
        assertNotNull(unit);
    }

    @ParameterizedTest
    @EnumSource
    void testWithEnumSourceWithAutoDetection(ChronoUnit unit) {
        assertNotNull(unit);
    }

    @ParameterizedTest
    @EnumSource(names = {"DAYS", "HOURS"})
    void testWithEnumSourceInclude(ChronoUnit unit) {
        assertTrue(
                EnumSet.of(ChronoUnit.DAYS, ChronoUnit.HOURS).contains(unit));
    }

    @ParameterizedTest
    @EnumSource(mode = EXCLUDE, names = {"ERAS", "FOREVER"})
    void testWithEnumSourceExclude(ChronoUnit unit) {
        assertFalse(
                EnumSet.of(ChronoUnit.ERAS, ChronoUnit.FOREVER).contains(unit));
    }

    @ParameterizedTest
    @EnumSource(mode = MATCH_ALL, names = "^.*DAYS$")
    void testWithEnumSourceRegex(ChronoUnit unit) {
        assertTrue(unit.name().endsWith("DAYS"));
    }

}
